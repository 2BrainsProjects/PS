package pt.isel.ps.anonichat.domain.utils

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter

fun readFile(filePath: String): String {
    val bufferedReader = BufferedReader(FileInputStream(filePath).bufferedReader())
    val text = bufferedReader.readText()
    bufferedReader.close()
    return text
}

fun writeFile(filePath: String, text: String) {
    val bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(filePath)))
    bufferedWriter.append(text)
    bufferedWriter.close()
}