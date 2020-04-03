import java.io.File
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.*
import kotlin.time.measureTime

fun main() {

    val level = "level5"

    File("input/$level").listFiles { _, filename -> filename.endsWith("3.in") }!!.sortedBy { it.name }.forEach {
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
    val id: Int,
    val origin: String,
    val destination: String,
    val takeoff: Int,
    val coordinates: Map<Int, Position>
) {

    val landing = takeoff + coordinates.keys.last()

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

    override fun toString(): String {
        return "Flight(id=$id, origin='$origin', destination='$destination')"
    }
}

data class TransferWindow(val flighta: Int, val flightb: Int, val delay: Int, val windowStart: Int, val windowEnd: Int)

data class Position(
    val lat: Double,
    val lon: Double,
    val alt: Double
)

data class Vector(val x: Double, val y: Double, val z: Double)

infix fun Vector.distance(other: Vector): Double {
    return sqrt((x - other.x).pow(2) + (y - other.y).pow(2) + (z - other.z).pow(2))
}

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

    return Flight(flightId, origin, destination, takeoff, positions)
}

fun processFile(file: File): List<String> {
    println("processing ${file.name}...")

    val scanner = scanner(file.readText())

    val transferRange = scanner.nextDouble()

    val count = scanner.nextInt()
    val flights = (0 until count).map {
        loadFlight(scanner.nextInt())
    }

    val combinations = sequence {
        flights.forEach { a ->
            flights.forEach { b ->
                if (a != b && a.destination != b.destination)
                    yield(a to b)
            }
        }
    }.toList()

    val intersections = combinations.parallelStream().map { (a, b) ->
        sequence {
            val duration = measureTime {

                val delayRange = max(0, (a.takeoff - b.landing))..min((a.landing - b.takeoff), 3600)
                delayRange.map { delay ->
                    var windowStart: Int? = null
                    var windowEnd: Int? = null

                    val bothInAir = if (a.takeoff > b.takeoff + delay) {
                        if (a.landing < b.landing + delay) {
                            a.takeoff..a.landing
                        } else {
                            a.takeoff..(b.landing + delay)
                        }
                    } else {
                        if (a.landing < b.landing + delay) {
                            (b.takeoff + delay)..a.landing
                        } else {
                            (b.takeoff + delay)..(b.landing + delay)
                        }
                    }

                    bothInAir.forEach { timestamp ->
                        val positionA = a.getApproximatedPosition(timestamp)

                        if (positionA.alt > 6000) {
                            val positionB = b.getApproximatedPosition(timestamp - delay)
                            if (positionB.alt > 6000) {
                                val distance = positionA.toVector() distance positionB.toVector()
                                if (1000 <= distance && distance <= transferRange) {
                                    if (windowStart == null) {
                                        windowStart = timestamp
                                    }
                                    windowEnd = timestamp
                                } else if (windowStart != null) {
                                    yield(TransferWindow(a.id, b.id, delay, windowStart!!, windowEnd!!))
                                    windowStart = null
                                    windowEnd = null
                                }
                            } else if (windowStart != null) {
                                yield(TransferWindow(a.id, b.id, delay, windowStart!!, windowEnd!!))
                                windowStart = null
                                windowEnd = null
                            }
                        } else if (windowStart != null) {
                            yield(TransferWindow(a.id, b.id, delay, windowStart!!, windowEnd!!))
                            windowStart = null
                            windowEnd = null
                        }
                    }
                }
            }
            println("$a to $b: $duration")
        }.toList()
    }.collect(Collectors.toList()).flatten()

    return intersections
        .groupBy { Triple(it.flighta, it.flightb, it.delay) }
        .map {
            val windowString = it.value.joinToString(" ") {
                if (it.windowStart == it.windowEnd)
                    it.windowStart.toString()
                else
                    "${it.windowStart}-${it.windowEnd}"
            }
            "${it.key.first} ${it.key.second} ${it.key.third} $windowString"
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
