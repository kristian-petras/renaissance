package org.renaissance.common.workload

import org.renaissance.common.model.Product

internal interface WorkloadClient {
    val host: String
    val port: Int

    suspend fun getProducts(): List<Product>
    suspend fun getProduct(id: String): Product
    suspend fun postProduct(product: Product)

    /**
     * Helper function to generate a URL from the configuration.
     */
    fun url(endpoint: String) = "http://$host:$port/$endpoint"
}