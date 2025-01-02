package com.udoynet

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import java.io.BufferedReader
import java.io.InputStreamReader

class PingModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "PingModule"
    }

    @ReactMethod
    fun ping(host: String, promise: Promise) {
        try {
            // Execute the ping command
            val process = Runtime.getRuntime().exec("ping -c 4 $host")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val outputLines = mutableListOf<String>()
            var line: String?

            // Read the output of the ping command
            while (reader.readLine().also { line = it } != null) {
                outputLines.add(line!!)
            }
            reader.close()
            process.waitFor()

            // Format the output
            val formattedOutput = formatPingOutput(outputLines, host)
            promise.resolve(formattedOutput)
        } catch (e: Exception) {
            promise.reject("Ping Error", e)
        }
    }

    private fun formatPingOutput(outputLines: List<String>, host: String): String {
        val result = StringBuilder()
        result.append("ping $host\n\n")

        val replyLines = outputLines.filter { it.contains("bytes from") || it.contains("icmp_seq") }
        if (replyLines.isNotEmpty()) {
            result.append("Pinging $host with 32 bytes of data:\n")
            replyLines.forEach { line ->
                result.append(formatReplyLine(line)).append("\n")
            }
        }

        val statisticsLine = outputLines.find { it.contains("packets transmitted") }
        statisticsLine?.let {
            result.append("\nPing statistics for $host:\n")
            result.append(formatStatisticsLine(it)).append("\n")
        }

        val rttLine = outputLines.find { it.contains("rtt") }
        rttLine?.let {
            result.append("Approximate round trip times in milli-seconds:\n")
            result.append(formatRTTLine(it))
        }

        return result.toString()
    }

    private fun formatReplyLine(line: String): String {
        // Example raw reply: "64 bytes from 8.8.8.8: icmp_seq=1 ttl=109 time=49 ms"
        val regex = Regex("bytes from ([^:]+):.*time=([0-9.]+) ms")
        val match = regex.find(line)
        return if (match != null) {
            val ip = match.groupValues[1]
            val time = match.groupValues[2]
            "Reply from $ip: bytes=32 time=${time}ms TTL=109"
        } else {
            line
        }
    }

    private fun formatStatisticsLine(line: String): String {
        // Example raw line: "4 packets transmitted, 4 received, 0% packet loss, time 3002ms"
        val regex = Regex("(\\d+) packets transmitted, (\\d+) received, (\\d+)% packet loss")
        val match = regex.find(line)
        return if (match != null) {
            val sent = match.groupValues[1]
            val received = match.groupValues[2]
            val loss = match.groupValues[3]
            "    Packets: Sent = $sent, Received = $received, Lost = ${sent.toInt() - received.toInt()} ($loss% loss)"
        } else {
            line
        }
    }

    private fun formatRTTLine(line: String): String {
        // Example raw line: "rtt min/avg/max/mdev = 49.163/49.567/50.031/0.370 ms"
        val regex = Regex("rtt min/avg/max/mdev = ([0-9.]+)/([0-9.]+)/([0-9.]+)/([0-9.]+) ms")
        val match = regex.find(line)
        return if (match != null) {
            val min = match.groupValues[1]
            val max = match.groupValues[3]
            val avg = match.groupValues[2]
            "    Minimum = ${min}ms, Maximum = ${max}ms, Average = ${avg}ms"
        } else {
            line
        }
    }
}
