package com.example.kmp.cicd

class WorkflowGenerator(
    private val cloudProvider: String = "aws",
    private val terraformPath: String = "./infra/terraform",
    private val workflowPath: String = ".github/workflows",
    private val skipMonitoring: Boolean = false
) {
    fun generateWorkflow(): String {
        val workflow = StringBuilder()
        
        workflow.append("""
            name: Build and Deploy
            
            on:
              push:
                branches: [ main ]
              pull_request:
                branches: [ main ]
                
            jobs:
              build-and-deploy:
                runs-on: ubuntu-latest
                steps:
                  - uses: actions/checkout@v3
                  
                  - name: Set up Cloud Provider
                    uses: ${getCloudProviderAction()}
                    
                  - name: Set up Terraform
                    uses: hashicorp/setup-terraform@v2
                    
                  - name: Terraform Init
                    run: |
                      cd ${terraformPath}/${cloudProvider}
                      terraform init
                      
                  - name: Terraform Plan
                    run: |
                      cd ${terraformPath}/${cloudProvider}
                      terraform plan
                      
                  - name: Terraform Apply
                    if: github.ref == 'refs/heads/main'
                    run: |
                      cd ${terraformPath}/${cloudProvider}
                      terraform apply -auto-approve
        """.trimIndent())
        
        if (!skipMonitoring) {
            workflow.append("""
                
                  - name: Apply monitoring configuration
                    run: |
                      kubectl apply -f ./infra/monitoring/
            """.trimIndent())
        }
        
        return workflow.toString()
    }
    
    private fun getCloudProviderAction(): String {
        return when (cloudProvider) {
            "aws" -> "aws-actions/configure-aws-credentials@v2"
            "gcp" -> "google-github-actions/auth@v1"
            "azure" -> "azure/login@v1"
            else -> throw IllegalArgumentException("Unsupported cloud provider: $cloudProvider")
        }
    }
    
    companion object {
        fun setupWorkflow(
            cloudProvider: String = "aws",
            skipMonitoring: Boolean = false
        ): String {
            return WorkflowGenerator(
                cloudProvider = cloudProvider,
                skipMonitoring = skipMonitoring
            ).generateWorkflow()
        }
    }
} 