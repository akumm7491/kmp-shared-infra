package com.example.kmp.template.routes

import com.example.kmp.template.model.DemoRequest
import com.example.kmp.template.service.DemoService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureApiRoutes() {
    val demoService by inject<DemoService>()

    post("/demo") {
        val request = call.receive<DemoRequest>()
        val response = demoService.processRequest(request)
        call.respond(response)
    }

    get("/demo/requests") {
        val history = demoService.getRequestHistory()
        call.respond(history)
    }

    get("/demo/responses") {
        val history = demoService.getResponseHistory()
        call.respond(history)
    }
} 