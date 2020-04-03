import java.io.File
import java.util.*
import java.util.regex.Pattern
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun main() {

    val level = "level3"

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

data class Position(
    val lat: Double,
    val lon: Double,
    val alt: Double
)

data class Vector(val x: Double, val y: Double, val z: Double)

data class AirportFlights(val start: String, val destination: String, val count: Int)

const val radius = 6371_000

val Double.rad: Double get() = PI * (this / 180)

fun Position.toVector(): Vector {
    val r = radius + alt
    return Vector(
        cos(lat.rad) * cos(lon.rad) * r,
        cos(lat.rad) * sin(lon.rad) * r,
        sin(lat.rad) * r
    )
}

fun processFile(file: File): List<String> {
    println("processing ${file.name}...")

    val text = file.readText()
    val scanner = Scanner(text).apply {
        useDelimiter(Pattern.compile("[\n,]"))
        useLocale(Locale.ROOT)
    }
    val count = scanner.nextInt()

    val positions = (0 until count).map {
        Position(
            scanner.nextDouble(),
            scanner.nextDouble(),
            scanner.nextDouble()
        )
    }

    val vectors = positions.map {
        it.toVector()
    }

    return vectors.map {
        "${it.x} ${it.y} ${it.z}"
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
