package org.renaissance.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.renaissance.common.workload.WorkloadClient
import org.renaissance.common.model.Product

internal class KtorWorkloadClient(
    private val client: HttpClient,
    override val host: String,
    override val port: Int
) : WorkloadClient {
    override suspend fun getProducts(): List<Product> {
        return client.get(url("product")).body<List<Product>>()
    }

    override suspend fun getProduct(id: String): Product {
        return client.get(url("product/$id")).body<Product>()
    }

    override suspend fun postProduct(product: Product) {
        client.post(url("product")) {
            contentType(ContentType.Application.Json)
            setBody(product)
        }
    }
}