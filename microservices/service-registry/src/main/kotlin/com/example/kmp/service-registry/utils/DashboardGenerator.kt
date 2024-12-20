package com.example.kmp.service.registry.utils

import com.example.kmp.service.registry.registry.DynamicServiceRegistry

fun generateDashboardHtml(registry: DynamicServiceRegistry): String {
    val services = registry.getAllServices()
    
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <title>Service Registry Dashboard</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; }
                .service { border: 1px solid #ddd; margin: 10px 0; padding: 15px; border-radius: 4px; }
                .instance { margin-left: 20px; padding: 10px; background: #f5f5f5; }
                .status-up { color: green; }
                .status-down { color: red; }
                .status-starting { color: orange; }
            </style>
        </head>
        <body>
            <h1>Service Registry Dashboard</h1>
            ${
                services.entries.joinToString("") { (serviceName, instances) ->
                    """
                    <div class="service">
                        <h2>$serviceName</h2>
                        ${
                            instances.joinToString("") { instance ->
                                """
                                <div class="instance">
                                    <p><strong>Instance ID:</strong> ${instance.instanceId}</p>
                                    <p><strong>Host:</strong> ${instance.hostName} (${instance.ipAddr})</p>
                                    <p><strong>Status:</strong> <span class="status-${instance.status.name.lowercase()}">${instance.status}</span></p>
                                    <p><strong>Port:</strong> ${instance.port}</p>
                                    ${if (instance.healthCheckUrl != null) "<p><strong>Health Check:</strong> ${instance.healthCheckUrl}</p>" else ""}
                                    ${if (instance.metadata.isNotEmpty()) "<p><strong>Metadata:</strong> ${instance.metadata}</p>" else ""}
                                </div>
                                """
                            }
                        }
                    </div>
                    """
                }
            }
            <script>
                // Auto-refresh every 30 seconds
                setTimeout(() => window.location.reload(), 30000);
            </script>
        </body>
        </html>
    """.trimIndent()
}
