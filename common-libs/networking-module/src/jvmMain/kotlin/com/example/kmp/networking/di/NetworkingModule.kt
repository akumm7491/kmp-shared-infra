package com.example.kmp.networking.di

import com.example.kmp.monitoring.LogProvider
import com.example.kmp.monitoring.MetricsProvider
import com.example.kmp.networking.NetworkingConfig
import com.example.kmp.networking.KtorNetworking
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun networkingModule(configure: NetworkingConfig.() -> Unit) = module {
    single { 
        NetworkingConfig().apply(configure)
    }
    
    single<LogProvider> { 
        get<NetworkingConfig>().logProvider ?: error("LogProvider not configured")
    }
    
    single<MetricsProvider> {
        get<NetworkingConfig>().metricsProvider ?: error("MetricsProvider not configured")
    }
    
    singleOf(::KtorNetworking)
}
