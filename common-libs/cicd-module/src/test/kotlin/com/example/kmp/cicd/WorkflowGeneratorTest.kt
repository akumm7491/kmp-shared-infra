package com.example.kmp.cicd

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class WorkflowGeneratorTest : FunSpec({
    context("WorkflowGenerator") {
        test("should generate workflow file with default paths") {
            val generator = WorkflowGenerator()
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("name: Build and Deploy") shouldBe true
            workflow.contains("runs-on: ubuntu-latest") shouldBe true
        }

        test("should generate workflow file with custom paths") {
            val generator = WorkflowGenerator(
                terraformPath = "custom/terraform",
                workflowPath = "custom/workflows"
            )
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("custom/terraform") shouldBe true
        }

        test("should generate workflow file with AWS provider") {
            val generator = WorkflowGenerator(cloudProvider = "aws")
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("aws-actions/configure-aws-credentials") shouldBe true
        }

        test("should generate workflow file with GCP provider") {
            val generator = WorkflowGenerator(cloudProvider = "gcp")
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("google-github-actions/auth") shouldBe true
        }

        test("should generate workflow file with Azure provider") {
            val generator = WorkflowGenerator(cloudProvider = "azure")
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("azure/login") shouldBe true
        }

        test("should generate workflow file with monitoring") {
            val generator = WorkflowGenerator(skipMonitoring = false)
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("Apply monitoring configuration") shouldBe true
        }

        test("should generate workflow file without monitoring") {
            val generator = WorkflowGenerator(skipMonitoring = true)
            val workflow = generator.generateWorkflow()
            
            workflow shouldNotBe null
            workflow.contains("Apply monitoring configuration") shouldBe false
        }

        test("should use companion object to set up workflows") {
            val workflow = WorkflowGenerator.setupWorkflow(
                cloudProvider = "aws",
                skipMonitoring = false
            )
            
            workflow shouldNotBe null
            workflow.contains("aws-actions/configure-aws-credentials") shouldBe true
            workflow.contains("Apply monitoring configuration") shouldBe true
        }
    }
}) 