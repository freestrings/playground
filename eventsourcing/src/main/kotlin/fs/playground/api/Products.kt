package fs.playground.api

import fs.playground.product.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class ProductApi(
        @Autowired private val productService: ProductService
) {

    @PostMapping("/product")
    fun create(@RequestParam("productName") productName: String, @RequestParam("stockQty") stockQty: Int): Long? {
        return productService.create(productName, ProductEntityPayload(stockQty))
    }

    @GetMapping("/product/{productId}")
    fun find(@PathVariable productId: Long): Products? {
        return productService.find(productId)
    }

    @PostMapping("/product/{productId}")
    fun buy(@PathVariable productId: Long): String {
        val adjustState = productService.adjustStockQty(productId, -1)
        return when (adjustState) {
            AdustState.SOLDOUT -> {
                throw Exception("soldout")
            }
            else -> {
                adjustState.name
            }
        }
    }

}