package com.example.kmp.networking

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*

class KtorServer(
    private val port: Int = 8080,
    private val host: String = "0.0.0.0",
    private val configure: Application.() -> Unit
) {
    fun start() {
        embeddedServer(Netty, port = port, host = host) {
            configure(this)
        }.start(wait = false)
    }
}
