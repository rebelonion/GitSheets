package util

import AppConfig

fun initializeGitRepo(config: AppConfig) {

}

private fun addTokenToUrl(repoUrl: String, user: String, token: String): String {
    return if (repoUrl.startsWith("https://") && !repoUrl.contains("@")) {
        repoUrl.replace("https://", "https://$user:$token@")
    } else {
        repoUrl
    }
}

fun syncToRemoteAndCommit(config: AppConfig, message: String): Pair<Boolean, String?> {
    return try {
        syncToRemote(config)

        executeCommand("git add .", config.general.dataPath)

        val diffResult = executeCommand("git diff --cached --quiet", config.general.dataPath, ignoreError = true)
        if (diffResult == 0) {
            println("No changes to commit")
            return Pair(false, null)
        }

        executeCommand("git commit -m \"$message\"", config.general.dataPath)
        executeCommand("git push origin ${config.git.branch}", config.general.dataPath)

        Pair(true, null)
    } catch (e: Exception) {
        println("Git operation failed: ${e.message}")
        Pair(false, e.message)
    }
}

private fun syncToRemote(config: AppConfig) {
    try {
        val remoteCheckResult = executeCommand("git ls-remote --exit-code origin", config.general.dataPath, ignoreError = true)
        if (remoteCheckResult != 0) {
            println("No remote repository found, skipping sync")
            return
        }

        executeCommand("git fetch origin", config.general.dataPath)

        val upToDateCheck = executeCommand("git diff --quiet HEAD origin/${config.git.branch}", config.general.dataPath, ignoreError = true)
        if (upToDateCheck == 0) {
            println("Already up to date with remote")
            return
        }

        println("Syncing to remote state...")
        executeCommand("git reset --hard origin/${config.git.branch}", config.general.dataPath)
        println("Successfully synced to remote")

    } catch (e: Exception) {
        println("Warning: Failed to sync to remote: ${e.message}")
    }
}

