package com.configcat

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

fun randomSdkKey(): String = "${randomSdkKeySegment()}/${randomSdkKeySegment()}"

fun randomSdkKeySegment(): String =
    (1..22)
        .map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }
        .joinToString("")

suspend fun awaitUntil(
    timeout: Duration = 5.seconds,
    condTarget: suspend () -> Boolean,
): Boolean {
    val timeSource = TimeSource.Monotonic
    val deadline = timeSource.markNow() + timeout
    while (!condTarget()) {
        delay(200)
        if (deadline.hasPassedNow()) {
            throw Exception("Test await timed out.")
        }
    }
    return deadline.hasPassedNow()
}
