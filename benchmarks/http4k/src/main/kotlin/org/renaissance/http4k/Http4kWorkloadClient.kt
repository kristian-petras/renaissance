package org.renaissance.http4k

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.renaissance.common.model.Product
import org.renaissance.common.workload.WorkloadClient

internal class Http4kWorkloadClient(
    private val client: HttpHandler,
    override val host: String,
    override val port: Int
) : WorkloadClient {
    override suspend fun getProducts(): List<Product> {
        return Lens.productsLens(client(Request(Method.GET, url("product")))).toList()
    }

    override suspend fun getProduct(id: String): Product {
        return Lens.productLens(client(Request(Method.GET, url("product/$id"))))
    }

    override suspend fun postProduct(product: Product) {
        client(Lens.productLens(product, Request(Method.POST, url("product"))))
    }
}