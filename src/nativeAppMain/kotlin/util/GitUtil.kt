package util

import AppConfig

fun initializeGitRepo(config: AppConfig) {
    if (!fileExists("${config.general.dataPath}/.git")) {
        executeCommand("git init -b ${config.git.branch}", config.general.dataPath)
        executeCommand("git config user.name '${config.git.authorName}'", config.general.dataPath)
        executeCommand("git config user.email '${config.git.authorEmail}'", config.general.dataPath)

        val authenticatedUrl = addTokenToUrl(config.git.repoUrl, config.git.authorName, config.git.token)
        executeCommand("git remote add origin $authenticatedUrl", config.general.dataPath)
    }
}

private fun addTokenToUrl(repoUrl: String, user: String, token: String): String {
    return if (repoUrl.startsWith("https://") && !repoUrl.contains("@")) {
        repoUrl.replace("https://", "https://$user:$token@")
    } else {
        repoUrl
    }
}

fun commitAndPush(config: AppConfig, message: String): Pair<Boolean, String?> {
    return try {
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