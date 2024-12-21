package com.example.kmp.networking

import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.http.*

fun Application.configureNetworking() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        anyHost()
    }
}
