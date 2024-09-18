package org.renaissance.ktor

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.tomcat.Tomcat
import kotlinx.coroutines.runBlocking
import org.renaissance.common.workload.WorkloadServer
import org.renaissance.common.model.Product
import org.renaissance.common.model.WorkloadConfiguration
import org.renaissance.common.utility.Utility.generateProduct
import java.util.concurrent.ConcurrentHashMap

internal class KtorWorkloadServer(port: Int, serverEngine: String, initialProductCount: Int) : WorkloadServer {
    private val products: MutableMap<String, Product> = ConcurrentHashMap<String, Product>().apply {
        repeat(initialProductCount) {
            val product = generateProduct()
            put(product.id, product)
        }
    }

    private val engine = when (serverEngine) {
        "netty" -> Netty
        "jetty" -> Jetty
        "tomcat" -> Tomcat
        "cio" -> CIO
        else -> error(
            "Unsupported server engine: ${serverEngine}. " +
                    "Supported engines are: netty, jetty, tomcat, cio."
        )
    }

    private val server = embeddedServer(engine, port = port) {
        install(ContentNegotiation) {
            json()
        }
        routing {
            get("/product") {
                call.respond(HttpStatusCode.OK, products.values)
            }
            get("/product/{id}") {
                when (val id = call.parameters["id"]) {
                    null -> call.respond(HttpStatusCode.BadRequest)
                    !in products -> call.respond(HttpStatusCode.NotFound)
                    else -> {
                        val product = products[id] ?: error("Invariant error: Product $id should be present")
                        call.respond(product)
                    }
                }
            }
            post("/product") {
                val product = call.receive<Product>()
                products[product.id] = product
                call.respond(HttpStatusCode.Created)
            }
        }
    }

    override fun port(): Int = runBlocking { server.resolvedConnectors().first().port }

    override fun start() {
        server.start(wait = false)
    }

    override fun stop() {
        server.stop()
        products.clear()
    }

    companion object {
        fun create(config: WorkloadConfiguration): KtorWorkloadServer =
            KtorWorkloadServer(config.port, config.serverEngine, config.initialProductCount)
    }
}