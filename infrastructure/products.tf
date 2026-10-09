locals {
  # Shown on the product's page in the developer portal. Taken from the product's API spec,
  # so the spec stays the one place to edit it; set apim_product.description to override.
  # Trimmed: a YAML block scalar ends in a newline, which APIM would keep.
  product_description = try(coalesce(
    var.apim_product.description,
    trimspace(local.api_specs[sort(keys(var.apis))[0]].info.description)
  ), null)
}

module "product" {
  source = "git::https://github.com/hmcts/cnp-module-api-mgmt-product.git?ref=master"

  api_mgmt_rg                   = var.api_mgmt_rg
  api_mgmt_name                 = var.api_mgmt_name
  name                          = var.apim_product.name
  description                   = local.product_description
  subscription_required         = var.apim_product.subscription_required
  subscriptions_limit           = var.apim_product.subscriptions_limit
  approval_required             = var.apim_product.approval_required
  published                     = var.apim_product.published
  product_access_control_groups = var.apim_product.product_access_control_groups
  product_policy                = ""
}
