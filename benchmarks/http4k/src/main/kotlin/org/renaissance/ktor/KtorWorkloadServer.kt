package org.renaissance.ktor

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.renaissance.common.workload.WorkloadServer
import org.renaissance.common.model.Product
import java.util.concurrent.ConcurrentHashMap

class KtorWorkloadServer(port: Int) : WorkloadServer {
    private val products: MutableMap<String, Product> = ConcurrentHashMap<String, Product>()
    private val server = embeddedServer(Netty, port = port) {
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

    override fun port(): Int = server.environment.connectors.first().port

    override fun start() {
        server.start(wait = false)
    }

    override fun stop() {
        server.stop()
        products.clear()
    }
}