package util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.refTo
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fwrite
import platform.posix.mkdir

fun fileExists(path: String): Boolean {
    return access(path, F_OK) == 0
}

@OptIn(ExperimentalForeignApi::class)
fun readFile(path: String): String {
    val file = fopen(path, "r") ?: return ""
    val content = StringBuilder()
    val buffer = ByteArray(1024)

    while (true) {
        val bytesRead = fread(buffer.refTo(0), 1u, buffer.size.toULong(), file).toInt()
        if (bytesRead <= 0) break
        content.append(buffer.decodeToString(0, bytesRead))
    }

    fclose(file)
    return content.toString()
}

@OptIn(ExperimentalForeignApi::class)
fun writeFile(path: String, content: String) {
    val file = fopen(path, "w") ?: return
    val bytes = content.encodeToByteArray()
    fwrite(bytes.refTo(0), 1u, bytes.size.toULong(), file)
    fclose(file)
}

fun ensureDirectoryExists(path: String) {
    if (!fileExists(path)) {
        mkdir(path, 0x1FFu) // 0777 permissions
    }
}
