package pt.isel.ps.anonichat.domain.utils

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * Reads the content of a file
 * @param filePath The file path
 * @return The file content
 */
fun readFile(filePath: String): String {
    val bufferedReader = BufferedReader(FileInputStream(filePath).bufferedReader())
    val text = bufferedReader.readText()
    bufferedReader.close()
    return text
}

/**
 * Writes text to a file
 * @param filePath The file path
 * @param text The text to write
 */
fun writeFile(filePath: String, text: String) {
    
    val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(filePath)))
    bufferedWriter.write(text)
    bufferedWriter.close()
}