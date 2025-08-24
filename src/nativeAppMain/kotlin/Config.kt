import kotlinx.serialization.Serializable
import com.akuleshov7.ktoml.Toml
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.serialization.decodeFromString
import platform.posix.getenv
import util.fileExists
import util.readFile

@Serializable
data class SheetConfig(
    val id: String,
    val index: Int = -1,
    val headerRow: Int = -1,
    val titleColumn: Int = -1
)

@Serializable
data class GitConfig(
    val repoUrl: String,
    val branch: String = "main",
    val token: String,
    val authorName: String = "Sheets Tracker",
    val authorEmail: String = "sheets-tracker@example.com"
)

@Serializable
data class WebhookConfig(
    val url: String,
    val enabled: Boolean = true
)

@Serializable
data class GeneralConfig(
    val dataPath: String = "./data",
    val intervalMinutes: Int = 15
)

@Serializable
data class AppConfig(
    val sheet: SheetConfig,
    val git: GitConfig,
    val webhook: WebhookConfig? = null,
    val general: GeneralConfig
)

object ConfigLoader {
    fun loadConfig(configFile: String? = null): AppConfig {
        val configFilePath = configFile ?: "config.toml"
        
        val config = if (fileExists(configFilePath)) {
            val tomlContent = readFile(configFilePath)
            Toml.decodeFromString<AppConfig>(tomlContent)
        } else {
            loadFromEnv()
        }

        return overrideWithEnv(config)
    }

    private fun loadFromEnv(): AppConfig {
        val sheetId = getEnv("GOOGLE_SHEET_ID")
            ?: throw IllegalStateException("GOOGLE_SHEET_ID not found")
        val repoUrl = getEnv("GIT_REPO_URL")
            ?: throw IllegalStateException("GIT_REPO_URL not found")
        val gitToken = getEnv("GIT_TOKEN")
            ?: throw IllegalStateException("GIT_TOKEN not found")

        return AppConfig(
            sheet = SheetConfig(
                id = sheetId,
                index = getEnv("GOOGLE_SHEET_INDEX")?.toIntOrNull() ?: -1,
                headerRow = getEnv("HEADER_ROW")?.toIntOrNull() ?: -1,
                titleColumn = getEnv("TITLE_COLUMN")?.toIntOrNull() ?: -1
            ),
            git = GitConfig(
                repoUrl = repoUrl,
                token = gitToken,
                branch = getEnv("GIT_BRANCH") ?: "main",
                authorName = getEnv("GIT_AUTHOR_NAME") ?: "Sheets Tracker",
                authorEmail = getEnv("GIT_AUTHOR_EMAIL") ?: "sheets-tracker@example.com"
            ),
            webhook = getEnv("WEBHOOK_URL")?.let {
                WebhookConfig(url = it, enabled = getEnv("WEBHOOK_ENABLED") != "false")
            },
            general = GeneralConfig(
                dataPath = getEnv("DATA_PATH") ?: "./data",
                intervalMinutes = getEnv("INTERVAL_MINUTES")?.toIntOrNull() ?: 15
            )
        )
    }

    private fun overrideWithEnv(config: AppConfig): AppConfig {
        return config.copy(
            sheet = config.sheet.copy(
                id = getEnv("GOOGLE_SHEET_ID") ?: config.sheet.id,
                index = getEnv("GOOGLE_SHEET_INDEX")?.toIntOrNull() ?: config.sheet.index,
                headerRow = getEnv("HEADER_ROW")?.toIntOrNull() ?: config.sheet.headerRow,
                titleColumn = getEnv("TITLE_COLUMN")?.toIntOrNull() ?: config.sheet.titleColumn
            ),
            git = config.git.copy(
                repoUrl = getEnv("GIT_REPO_URL") ?: config.git.repoUrl,
                token = getEnv("GIT_TOKEN") ?: config.git.token,
                branch = getEnv("GIT_BRANCH") ?: config.git.branch,
                authorName = getEnv("GIT_AUTHOR_NAME") ?: config.git.authorName,
                authorEmail = getEnv("GIT_AUTHOR_EMAIL") ?: config.git.authorEmail
            ),
            webhook = getEnv("WEBHOOK_URL")?.let {
                WebhookConfig(
                    url = it,
                    enabled = getEnv("WEBHOOK_ENABLED") != "false"
                )
            } ?: config.webhook,
            general = config.general.copy(
                dataPath = getEnv("DATA_PATH") ?: config.general.dataPath,
                intervalMinutes = getEnv("INTERVAL_MINUTES")?.toIntOrNull() ?: config.general.intervalMinutes
            )
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    fun getEnv(name: String): String? {
        return getenv(name)?.toKString()
    }

}
