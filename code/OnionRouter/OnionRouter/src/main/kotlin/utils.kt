import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

fun LocalDateTime.format(): String = format(formatter)

fun String.format(): String = format(formatter)
