terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.0"
    }
  }
  backend "s3" {
    bucket = "kmp-terraform-state-dev"
    key    = "dev/terraform.tfstate"
    region = "us-west-2"
  }
}

provider "aws" {
  region = "us-west-2"
}

module "vpc" {
  source = "../../modules/networking"
  
  environment = "dev"
  vpc_cidr    = "10.0.0.0/16"
}

module "eks" {
  source = "../../modules/kubernetes"
  
  environment  = "dev"
  cluster_name = "kmp-dev"
  vpc_id       = module.vpc.vpc_id
  subnet_ids   = module.vpc.private_subnet_ids
}

module "monitoring" {
  source = "../../modules/monitoring"
  
  environment  = "dev"
  cluster_name = module.eks.cluster_name
}
