terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
  backend "azurerm" {
    resource_group_name  = "kmp-terraform-state"
    storage_account_name = "kmpterraformstate"
    container_name       = "tfstate"
    key                 = "prod.terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}

module "resource_group" {
  source = "../../modules/azure/resource-group"
  
  name     = "kmp-prod-rg"
  location = var.location
  tags     = var.tags
}

module "vnet" {
  source = "../../modules/networking"
  
  name                = "kmp-prod-vnet"
  resource_group_name = module.resource_group.name
  address_space       = ["10.0.0.0/16"]
  subnets = {
    aks = {
      name             = "aks-subnet"
      address_prefixes = ["10.0.1.0/24"]
    }
    ingress = {
      name             = "ingress-subnet"
      address_prefixes = ["10.0.2.0/24"]
    }
  }
}

module "aks" {
  source = "../../modules/kubernetes"
  
  name                = "kmp-prod-aks"
  resource_group_name = module.resource_group.name
  dns_prefix         = "kmp-prod"
  
  default_node_pool = {
    name                = "default"
    node_count          = 3
    vm_size            = "Standard_D4s_v3"
    availability_zones = [1, 2, 3]
  }

  additional_node_pools = {
    memory = {
      name                = "memory"
      node_count          = 2
      vm_size            = "Standard_E4s_v3"
      availability_zones = [1, 2, 3]
    }
  }

  network_profile = {
    network_plugin     = "azure"
    network_policy    = "calico"
    service_cidr      = "10.1.0.0/16"
    dns_service_ip    = "10.1.0.10"
  }
}
