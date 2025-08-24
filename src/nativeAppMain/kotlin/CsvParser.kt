object CsvParser {
    fun parse(csvData: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val chars = csvData.toCharArray()
        var currentRow = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false
        var i = 0
        
        while (i < chars.size) {
            val char = chars[i]
            
            when {
                char == '"' && !inQuotes -> {
                    inQuotes = true
                }
                char == '"' && inQuotes -> {
                    if (i + 1 < chars.size && chars[i + 1] == '"') {
                        // Escaped quote - add literal quote to field
                        currentField.append('"')
                        i++ // Skip the second quote
                    } else {
                        // End of quoted field
                        inQuotes = false
                    }
                }
                char == ',' && !inQuotes -> {
                    // Field separator
                    currentRow.add(currentField.toString())
                    currentField.clear()
                }
                char == '\n' && !inQuotes -> {
                    // End of row (only if not inside quotes)
                    currentRow.add(currentField.toString())
                    if (currentRow.isNotEmpty() || currentField.isNotEmpty()) {
                        rows.add(currentRow.toList())
                    }
                    currentRow.clear()
                    currentField.clear()
                }
                char == '\r' && !inQuotes -> {
                    // Handle \r\n line endings - skip \r if not in quotes
                    if (i + 1 < chars.size && chars[i + 1] == '\n') {
                        // Will be handled by the \n case
                    } else {
                        // Standalone \r - treat as line ending
                        currentRow.add(currentField.toString())
                        if (currentRow.isNotEmpty() || currentField.isNotEmpty()) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                        currentField.clear()
                    }
                }
                else -> {
                    // Regular character (including newlines inside quotes)
                    currentField.append(char)
                }
            }
            i++
        }
        
        // Handle the last field/row
        currentRow.add(currentField.toString())
        if (currentRow.isNotEmpty() || currentField.isNotEmpty()) {
            rows.add(currentRow.toList())
        }
        
        return rows
    }
}