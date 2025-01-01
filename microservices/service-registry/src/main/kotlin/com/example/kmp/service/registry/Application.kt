package com.example.kmp.service.registry

import com.example.kmp.service.registry.registry.DynamicServiceRegistry

fun main() {
    val port = System.getenv("SERVER_PORT")?.toInt() ?: 8761
    DynamicServiceRegistry().start(port)
}
