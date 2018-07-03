package com.mastercard.gateway.ats.domain

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Order
import org.simpleframework.xml.Root

import java.math.BigDecimal
import java.math.BigInteger

@Root(name = "SaleItemType")
@Order(elements = ["productCode", "amount", "unitMeasure", "unitPrice", "quantity", "taxCode", "additionalProductCode", "additionalProductInfo", "typeMovement", "saleChannel", "vatRate"])
data class SaleItemType(
        @field:Element(name = "ProductCode", required = true) var productCode: BigInteger,
        @field:Element(name = "Amount", required = true) var amount: BigDecimal,
        @field:Attribute(name = "ItemID", required = true) var itemID: String) {

    @field:Element(name = "UnitMeasure", required=false)
    var unitMeasure: UnitOfMeasureCode? = null
    @field:Element(name = "UnitPrice", required=false)
    var unitPrice: BigDecimal? = null
    @field:Element(name = "Quantity", required=false)
    var quantity: BigDecimal? = null
    @field:Element(name = "TaxCode", required=false)
    var taxCode: String? = null
    @field:Element(name = "AdditionalProductCode", required=false)
    var additionalProductCode: Long? = null
    @field:Element(name = "AdditionalProductInfo", required=false)
    var additionalProductInfo: String? = null
    @field:Element(name = "TypeMovement", required=false)
    var typeMovement: String? = null
    @field:Element(name = "SaleChannel", required=false)
    var saleChannel: String? = null
    @field:Element(name = "VATRate", required=false)
    var vatRate: BigDecimal? = null

}


