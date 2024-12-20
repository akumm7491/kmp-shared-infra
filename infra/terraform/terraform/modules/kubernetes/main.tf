terraform {
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.0"
    }
  }
}

variable "environment" {
  type        = string
  description = "Environment name (e.g., dev, staging, prod)"
}

variable "region" {
  type        = string
  description = "Cloud provider region"
}

variable "cluster_name" {
  type        = string
  description = "Name of the Kubernetes cluster"
}

# Kubernetes namespace
resource "kubernetes_namespace" "microservices" {
  metadata {
    name = "microservices-${var.environment}"
    
    labels = {
      environment = var.environment
      managed-by  = "terraform"
    }
  }
}

# Helm release for monitoring stack
resource "helm_release" "monitoring" {
  name       = "monitoring"
  namespace  = kubernetes_namespace.microservices.metadata[0].name
  repository = "https://prometheus-community.github.io/helm-charts"
  chart      = "kube-prometheus-stack"
  version    = "45.7.1"

  set {
    name  = "grafana.enabled"
    value = "true"
  }

  depends_on = [kubernetes_namespace.microservices]
}
