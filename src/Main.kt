import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() {

    val level = "level4"

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

fun scanner(text: String) = Scanner(text).apply {
    useDelimiter(Pattern.compile("[\n, ]"))
    useLocale(Locale.ROOT)
}

data class Flight(
    val origin: String,
    val destination: String,
    val takeoff: Int,
    val coordinates: Map<Int, Position>
) {

    fun getApproximatedPosition(timestamp: Int): Position {
        val timestampOffset = timestamp - takeoff

        if (coordinates.containsKey(timestampOffset)) return coordinates.getValue(timestampOffset)

        var prevT = 0
        var nextT = 0
        for (key in coordinates.keys) {
            if (key > timestampOffset) {
                nextT = key
                break
            }
            prevT = key
        }
        val prev = coordinates.getValue(prevT)
        val next = coordinates.getValue(nextT)

        val factor = (timestampOffset - prevT).toDouble() / (nextT - prevT)
        return Position(
            prev.lat + (next.lat - prev.lat) * factor,
            prev.lon + (next.lon - prev.lon) * factor,
            prev.alt + (next.alt - prev.alt) * factor
        )
    }
}

data class Position(
    val lat: Double,
    val lon: Double,
    val alt: Double
)

data class Vector(val x: Double, val y: Double, val z: Double)

data class AirportFlights(val start: String, val destination: String, val count: Int)

data class Request(val flightId: Int, val timestamp: Int)

val Double.rad: Double get() = PI * (this / 180)
fun Position.toVector(): Vector {
    val r = 6371_000 + alt
    return Vector(
        cos(lat.rad) * cos(lon.rad) * r,
        cos(lat.rad) * sin(lon.rad) * r,
        sin(lat.rad) * r
    )
}

fun loadFlight(flightId: Int): Flight {
    val scanner = scanner(File("input/usedFlights/${flightId}.csv").readText())

    val origin = scanner.next()
    val destination = scanner.next()
    val takeoff = scanner.nextInt()

    val count = scanner.nextInt()
    val positions = (0 until count).map {
        scanner.nextInt() to Position(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble())
    }.toMap()

    return Flight(origin, destination, takeoff, positions)
}

fun processFile(file: File): List<String> {
    println("processing ${file.name}...")

    val scanner = scanner(file.readText())
    val count = scanner.nextInt()

    val request = (0 until count).map {
        Request(scanner.nextInt(), scanner.nextInt())
    }

    return request.map {
        val flight = loadFlight(it.flightId)
        val position = flight.getApproximatedPosition(it.timestamp)
        return@map "${position.lat} ${position.lon} ${position.alt}"
    }

    /*
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
    */
}
