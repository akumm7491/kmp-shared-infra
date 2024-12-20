terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 4.0"
    }
  }
  backend "gcs" {
    bucket = "kmp-terraform-state-prod"
    prefix = "prod"
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

module "vpc" {
  source = "../../modules/networking"
  
  environment = "prod"
  network_name = "kmp-prod-vpc"
  subnet_cidr = "10.0.0.0/16"
  region = var.region
}

module "gke" {
  source = "../../modules/kubernetes"
  
  environment = "prod"
  cluster_name = "kmp-prod"
  network_id = module.vpc.network_id
  subnet_id = module.vpc.subnet_id
  node_pools = {
    general = {
      machine_type = "n2-standard-4"
      min_count   = 3
      max_count   = 10
    }
    memory = {
      machine_type = "n2-highmem-4"
      min_count   = 2
      max_count   = 5
    }
  }
}

module "monitoring" {
  source = "../../modules/monitoring"
  
  environment = "prod"
  cluster_name = module.gke.cluster_name
  enable_cloud_monitoring = true
}
