import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    try {
        val runOnce = args.contains("--run-once") || args.contains("-o")

        val configDir = parseConfigDir(args) ?: ConfigLoader.getEnv("CONFIG_DIR")
        
        val config = ConfigLoader.loadConfig(configDir)
        val tracker = SheetsTracker(config)

        runBlocking {
            tracker.run(runOnce)
        }
    } catch (e: Exception) {
        println("Fatal error: ${e.message}")
        kotlin.system.exitProcess(1)
    }
}

private fun parseConfigDir(args: Array<String>): String? {
    for (i in args.indices) {
        when (args[i]) {
            "--config", "-c" -> {
                return if (i + 1 < args.size) args[i + 1] else null
            }
        }
    }
    return null
}