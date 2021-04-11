import java.io.*
import java.net.*
import java.util.*
import java.math.BigInteger
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    if (args.isEmpty() || !args[0].matches("server|client".toRegex())) {
        println("""Please provide "client / server" as command line argument.""")
        return
    }
    val host: String = args.getOrElse(1) { "127.0.0.1" }
    val port: Int = (args.getOrElse(2) { "8080" }).toInt()
    when (args[0]) {
        "server" -> {
            runServer(host, port)
        }
        "client" -> {
            runClient(host, port)
        }
    }
}

fun runClient(host: String, port: Int) = try {
    Socket(host, port).use { socket ->
        println("Connected to $host:$port!")

        val input = BufferedReader(InputStreamReader(socket.getInputStream()))
        val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

        println("Please enter N")
        val scanner = Scanner(System.`in`)
        while (true) {
            val line = scanner.nextLine()
            if (line.isBlank())
                break

            val inputInt: Int = try {
                line.toInt()
            } catch (e: Exception) {
                println("You did not enter a number, try again carefully")
                continue
            }

            println("You entered: $inputInt")
            output.write("$inputInt\n")
            output.flush()
            println("Server result: ${input.readLine()}")
        }
    }
    println("Disconnected")

} catch (e: IllegalArgumentException) {
    println("Invalid host or port")
} catch (e: UnknownHostException) {
    println("Invalid host or port")
} catch (e: Exception) {
    e.printStackTrace()
}

fun runServer(host: String, port: Int) {
    val serverSocket = ServerSocket()
    try {
        serverSocket.bind(InetSocketAddress(host, port))
    } catch (e: IOException) {
        println("Socket bind to $host:$port failed")
        return
    }
    try {
        while (true) {
            println("Waiting next client")
            val socket = serverSocket.accept()
            println("Client connected!")
            thread { handleClient(socket) }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun handleClient(socket: Socket) {
    socket.use {
        try {
            val input = BufferedReader(InputStreamReader((socket.getInputStream())))
            val output = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))

            while (true) {
                val request: Long = try {
                    input.readLine().toLong()
                } catch (e: Exception) {
                    output.write("You did not enter a number, try again carefully")
                    continue
                }
                println("Client request: $request")
                val result = fib(request)
                println("Sending my result: $result")
                output.write("$result\n")
                output.flush()
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }
    }
    println("Client disconnected")
}

fun mXm(m1: Array<Array<BigInteger>>, m2: Array<Array<BigInteger>>): Array<Array<BigInteger>> {
    val mReturn = Array(2) { Array(2) { BigInteger("0") } }
    mReturn[0][0] = (m1[0][0].multiply(m2[0][0]) + m1[0][1].multiply(m2[0][1]))
    mReturn[0][1] = (m1[0][0].multiply(m2[0][1]) + m1[0][1].multiply(m2[1][1]))
    mReturn[1][1] = (m1[0][1].multiply(m2[0][1]) + m1[1][1].multiply(m2[1][1]))
    return mReturn
}

fun binPow(m: Array<Array<BigInteger>>, n: Long): Array<Array<BigInteger>> {
    if (n == 0L) {
        val matrix = Array(2) { Array(2) { BigInteger("0") } }
        matrix[0] = arrayOf(BigInteger("1"), BigInteger("0"))
        matrix[1] = arrayOf(BigInteger("0"), BigInteger("1"))
        return matrix
    }
    return if (n % 2 == 1L) {
        mXm(binPow(m, n - 1), m)
    } else {
        val matrix = binPow(m, n / 2)
        mXm(matrix, matrix)
    }
}

fun fib(n: Long): BigInteger? {
    if (n <= 0) return null
    if (n == 1L) return BigInteger("0")
    var matrix = Array(2) { Array(2) { BigInteger("0") } }
    matrix[0][1] = BigInteger("1")
    matrix[1][1] = BigInteger("1")
    matrix = binPow(matrix, n - 2 )
    return matrix[1][1]
}