import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

fun main() {

    val level = "level1"

    File("input/$level").listFiles { _, filename -> filename.endsWith(".in") }!!.sortedBy { it.name }.forEach {
        val result = processFile(it)
        val joined = result.joinToString("\n")

        println("--- OUTPUT ---")
        println(joined)

        File("input/$level/${it.nameWithoutExtension}.out").apply {
            delete()
            createNewFile()
            writeText(joined)
        }

        println()
    }
}

data class Position(val timestamp: Long, val lat: Float, val lon: Float, val alt: Float)

data class MinMax(
    val minTimestamp: Long,
    val maxTimestamp: Long,
    val minLat: Float,
    val maxLat: Float,
    val minLon: Float,
    val maxLon: Float,
    val minAlt: Float,
    val maxAlt: Float
)

fun processFile(file: File): List<String> {
    println("processing ${file.name}...")

    val text = file.readText()
    val scanner = Scanner(text).apply {
        useDelimiter(Pattern.compile("[\n,]"))
        useLocale(Locale.ROOT)
    }
    val count = scanner.nextInt()
    scanner.nextLine()

    val positions = (0 until count).map {
        val pos = Position(scanner.nextLong(), scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat())
        scanner.nextLine()
        pos
    }


    val minMax = positions.fold(
        MinMax(
            Long.MAX_VALUE,
            Long.MIN_VALUE,
            Float.MAX_VALUE,
            Float.MIN_VALUE,
            Float.MAX_VALUE,
            Float.MIN_VALUE,
            Float.MAX_VALUE,
            Float.MIN_VALUE
        )
    ) { acc, it ->
        MinMax(
            min(acc.minTimestamp, it.timestamp),
            max(acc.maxTimestamp, it.timestamp),
            min(acc.minLat, it.lat),
            max(acc.maxLat, it.lat),
            min(acc.minLon, it.lon),
            max(acc.maxLon, it.lon),
            min(acc.minAlt, it.alt),
            max(acc.maxAlt, it.alt)
        )
    }

    return listOf(
        "${minMax.minTimestamp} ${minMax.maxTimestamp}",
        "${minMax.minLat} ${minMax.maxLat}",
        "${minMax.minLon} ${minMax.maxLon}",
        "${minMax.maxAlt}"
    )
}
