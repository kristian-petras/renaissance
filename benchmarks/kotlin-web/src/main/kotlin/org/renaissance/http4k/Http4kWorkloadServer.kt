package org.renaissance.http4k

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.ApacheServer
import org.http4k.server.Helidon
import org.http4k.server.Jetty
import org.http4k.server.ServerConfig
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.renaissance.common.workload.WorkloadServer
import org.renaissance.common.model.Product
import org.renaissance.common.model.WorkloadConfiguration
import org.renaissance.common.utility.Utility.generateProduct
import org.renaissance.http4k.Lens.productLens
import org.renaissance.http4k.Lens.productsLens
import java.util.concurrent.ConcurrentHashMap

internal class Http4kWorkloadServer(serverEngine: ServerConfig, initialProductCount: Int) : WorkloadServer {
    private val server = app().asServer(serverEngine)
    private val products: MutableMap<String, Product> = ConcurrentHashMap<String, Product>().apply {
        repeat(initialProductCount) {
            val product = generateProduct()
            put(product.id, product)
        }
    }

    private fun app(): HttpHandler = routes(
        "/product" bind Method.GET to { productsLens(products.values.toTypedArray(), Response(Status.OK)) },
        "/product/{id}" bind Method.GET to {
            when (val id = it.path("id")) {
                null -> Response(Status.BAD_REQUEST)
                !in products -> Response(Status.NOT_FOUND)
                else -> {
                    val product = products[id] ?: error("Invariant error: Product $it should be present")
                    productLens(product, Response(Status.OK))
                }
            }
        },
        "/product" bind Method.POST to {
            val product = productLens(it)
            products[product.id] = product
            Response(Status.CREATED)
        }
    )

    override fun port(): Int = server.port()

    override fun start() {
        server.start()
    }

    override fun stop() {
        server.stop()
        products.clear()
    }

    companion object {
        fun create(config: WorkloadConfiguration): Http4kWorkloadServer {
            val server = when (config.serverEngine) {
                "apache" -> ApacheServer(config.port)
                "jetty" -> Jetty(config.port)
                "undertow" -> Undertow(config.port)
                "helidon" -> Helidon(config.port)
                else -> error(
                    "Unsupported server engine: ${config.serverEngine}. " +
                            "Supported engines are: apache, jetty, undertow, helidon."
                )
            }
            return Http4kWorkloadServer(server, config.initialProductCount)
        }
    }
}