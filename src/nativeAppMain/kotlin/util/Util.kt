package util

import AppConfig
import platform.posix.system
import kotlin.collections.iterator

fun generateDetailedMessages(config: AppConfig, previousData: String?, currentData: String): List<String> {
    if (previousData == null ||
        config.sheet.headerRow < 0 ||
        config.sheet.titleColumn < 0) {
        return emptyList()
    }

    val previousRows = CsvParser.parse(previousData)
    val currentRows = CsvParser.parse(currentData)

    if (previousRows.size <= config.sheet.headerRow ||
        currentRows.size <= config.sheet.headerRow) {
        return emptyList()
    }

    val headers = currentRows[config.sheet.headerRow]
    val messages = mutableListOf<String>()

    val previousRowsMap = previousRows.drop(config.sheet.headerRow + 1).associateBy { row ->
        if (row.size > config.sheet.titleColumn) row[config.sheet.titleColumn] else ""
    }

    val currentRowsMap = currentRows.drop(config.sheet.headerRow + 1).associateBy { row ->
        if (row.size > config.sheet.titleColumn) row[config.sheet.titleColumn] else ""
    }

    for ((title, currentRow) in currentRowsMap) {
        if (title.isBlank()) continue

        val previousRow = previousRowsMap[title]
        if (previousRow == null) {
            messages.add("New entry added: $title")
            continue
        }

        for (colIndex in currentRow.indices) {
            if (colIndex >= headers.size || colIndex >= previousRow.size) continue

            val currentValue = currentRow[colIndex]
            val previousValue = previousRow[colIndex]

            if (currentValue != previousValue) {
                val columnTitle = headers[colIndex]
                messages.add("$columnTitle of $title was updated to $currentValue from $previousValue")
            }
        }
    }

    for (title in previousRowsMap.keys) {
        if (!currentRowsMap.containsKey(title) && title.isNotBlank()) {
            messages.add("Entry removed: $title")
        }
    }

    return messages
}


fun executeCommand(command: String, workingDir: String, ignoreError: Boolean = false): Int {
    val fullCommand = "cd $workingDir && $command"
    val result = system(fullCommand)

    if (result != 0 && !ignoreError) {
        throw RuntimeException("Command failed: $command (exit code: $result)")
    }

    return result
}