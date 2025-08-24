import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    try {
        val runOnce = args.contains("--run-once") || args.contains("-o")

        val configFile = parseConfigFile(args) ?: ConfigLoader.getEnv("CONFIG_FILE")
        
        val config = ConfigLoader.loadConfig(configFile)
        val tracker = SheetsTracker(config)

        runBlocking {
            tracker.run(runOnce)
        }
    } catch (e: Exception) {
        println("Fatal error: ${e.message}")
        kotlin.system.exitProcess(1)
    }
}

private fun parseConfigFile(args: Array<String>): String? {
    for (i in args.indices) {
        when (args[i]) {
            "--config", "-c" -> {
                return if (i + 1 < args.size) args[i + 1] else null
            }
        }
    }
    return null
}