package org.renaissance.ktor

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.apache.Apache
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.jetty.Jetty
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import org.renaissance.common.workload.WorkloadClient
import org.renaissance.common.model.Product
import org.renaissance.common.model.WorkloadConfiguration

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

    companion object {
        fun create(config: WorkloadConfiguration): KtorWorkloadClient {
            val client = when (config.clientEngine) {
                "apache" -> HttpClient(Apache) { install(ContentNegotiation) { json() } }
                "jetty" -> HttpClient(Jetty) { install(ContentNegotiation) { json() } }
                "okhttp" -> HttpClient(OkHttp) { install(ContentNegotiation) { json() } }
                "cio" -> HttpClient(CIO) { install(ContentNegotiation) { json() } }
                else -> error(
                    "Unsupported client engine: ${config.clientEngine}. " +
                            "Supported engines are: apache, jetty, okhttp, cio."
                )
            }
            return KtorWorkloadClient(client, config.host, config.port)
        }
    }
}