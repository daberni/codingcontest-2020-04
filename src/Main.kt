import java.io.File
import java.util.*
import java.util.regex.Pattern

fun main() {

    val level = "level2"

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

data class Flight(
    val timestamp: Long,
    val lat: Float,
    val lon: Float,
    val alt: Float,
    val start: String,
    val destination: String,
    val takeoff: Long
)

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

data class AirportFlights(val start: String, val destination: String, val count: Int)

fun processFile(file: File): List<String> {
    println("processing ${file.name}...")

    val text = file.readText()
    val scanner = Scanner(text).apply {
        useDelimiter(Pattern.compile("[\n,]"))
        useLocale(Locale.ROOT)
    }
    val count = scanner.nextInt()

    val flights = (0 until count).map {
        Flight(
            scanner.nextLong(),
            scanner.nextFloat(),
            scanner.nextFloat(),
            scanner.nextFloat(),
            scanner.next(),
            scanner.next(),
            scanner.nextLong()
        )
    }

    val groups = flights
        .groupBy { it.start to it.destination }
        .map { AirportFlights(it.key.first, it.key.second, it.value.map { it.takeoff }.distinct().count()) }
        .sortedWith(
            compareBy({ it.start }, { it.destination })
        )

    return groups.map {
        "${it.start} ${it.destination} ${it.count}"
    }
}
