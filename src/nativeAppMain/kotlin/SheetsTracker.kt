import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import util.ensureDirectoryExists
import util.fileExists
import util.generateDetailedMessages
import util.initializeGitRepo
import util.readFile
import util.syncToRemoteAndCommit
import util.writeFile
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SheetsTracker(private val config: AppConfig) {
    private val client = HttpClient(Curl) {
        install(ContentNegotiation) {
            json()
        }
    }

    private val dataFile = "${config.general.dataPath}/data.csv"
    private val hashFile = "${config.general.dataPath}/data_hash.txt"

    suspend fun run(runOnce: Boolean = false) {
        println("Starting Google Sheets tracker...")
        println("Sheet ID: ${config.sheet.id}")
        println("Repo: ${config.git.repoUrl}")

        ensureDirectoryExists(config.general.dataPath)
        initializeGitRepo(config)

        do {
            try {
                checkAndUpdate()
            } catch (e: Exception) {
                println("Error during check: ${e.message}")
                sendWebhook("Error checking sheet: ${e.message}")
            }

            if (!runOnce) {
                delay(config.general.intervalMinutes * 60 * 1000L)
            }
        } while (!runOnce)

        if (runOnce) {
            println("Single run completed.")
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun checkAndUpdate() {
        println("Checking for sheet changes...")

        val currentData = fetchSheetData()
        if (currentData == null) {
            println("Failed to fetch sheet data")
            return
        }

        val currentHash = calculateHash(currentData)
        val previousHash = loadPreviousHash()

        if (currentHash == previousHash) {
            println("No changes detected")
            return
        }

        println("Changes detected! Updating files...")

        val previousData = if (fileExists(dataFile)) {
            readFile(dataFile)
        } else null

        val detailedMessages = generateDetailedMessages(config, previousData, currentData)

        println("data:\n$currentData")
        writeFile(dataFile, currentData)
        writeFile(hashFile, currentHash)

        val commitMessage = "Update sheet data - ${Clock.System.now()}"

    }

    private suspend fun fetchSheetData(): String? {
        val url =
            "https://docs.google.com/spreadsheets/d/${config.sheet.id}/export?format=csv" + if (config.sheet.index >= 0) {
                "&gid=${config.sheet.index}"
            } else {
                ""
            }

        return try {
            val response = client.get(url)
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                println("HTTP error: ${response.status}")
                null
            }
        } catch (e: Exception) {
            println("Network error: ${e.message}")
            null
        }
    }

    private fun calculateHash(data: String): String {
        return data.hashCode().toString()
    }

    private fun loadPreviousHash(): String? {
        return if (fileExists(hashFile)) {
            readFile(hashFile).trim()
        } else null
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun sendWebhook(message: String) {
        val webhookConfig = config.webhook
        if (webhookConfig?.enabled != true) return

        try {
            client.post(webhookConfig.url) {
                setBody(message)
            }
        } catch (e: Exception) {
            println("Webhook notification failed: ${e.message}")
        }
    }
}