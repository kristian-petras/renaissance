package org.renaissance.common.workload

import kotlinx.coroutines.*
import org.renaissance.common.model.Product
import org.renaissance.common.model.WorkloadConfiguration
import org.renaissance.common.model.WorkloadSummary
import org.renaissance.common.model.WorkloadType
import java.util.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Client used to generate workloads for the http4k server.
 * The client sends requests to the server based on the workload type.
 * @param client HttpHandler used to send requests to the server.
 * @param configuration WorkloadConfiguration used to generate the workload.
 */
internal class WorkloadGenerator(
    private val client: WorkloadClient,
    private val configuration: WorkloadConfiguration
) {
    private val getProductsCounter = AtomicLong(0)
    private val getProductCounter = AtomicLong(0)
    private val postProductCounter = AtomicLong(0)

    private val readCounter = AtomicLong(0)
    private val writeCounter = AtomicLong(0)
    private val ddosCounter = AtomicLong(0)
    private val mixedCounter = AtomicLong(0)

    private val workloadCounter = AtomicLong(0)

    private val dispatcher = Dispatchers.IO.limitedParallelism(configuration.maxThreads, "Workload")

    /**
     * Starts the workload on the server based on [configuration].
     * Each workload consists of read, write, ddos and mixed requests.
     * The number of workloads is determined by [WorkloadConfiguration.workloadCount].
     * The number of requests for each workload type is determined by the corresponding configuration value.
     * Random workload is generated for each iteration based on the seed in [WorkloadConfiguration.workloadSelectorSeed].
     * @return WorkloadResult containing number of requests per type used for validation.
     */
    suspend fun runWorkload(): WorkloadSummary = coroutineScope {
        val random = Random(configuration.workloadSelectorSeed)
        withContext(dispatcher) {
            range(configuration.workloadCount).flatMap {
                when (random.nextWorkload()) {
                    WorkloadType.READ -> range(configuration.readWorkloadRepeatCount).map { async { client.readWorkload() } }
                    WorkloadType.WRITE -> range(configuration.writeWorkloadRepeatCount).map { async { client.writeWorkload() } }
                    WorkloadType.DDOS -> range(configuration.ddosWorkloadRepeatCount).map { async { client.ddosWorkload() } }
                    WorkloadType.MIXED -> range(configuration.mixedWorkloadRepeatCount).map { async { client.mixedWorkload() } }
                }.also { workloadCounter.incrementAndGet() }
            }.awaitAll()

            WorkloadSummary(
                getProductsCount = getProductsCounter.get(),
                getProductCount = getProductCounter.get(),
                postProductCount = postProductCounter.get(),
                readCount = readCounter.get(),
                writeCount = writeCounter.get(),
                ddosCount = ddosCounter.get(),
                mixedCount = mixedCounter.get(),
                workloadCount = workloadCounter.get()
            )
        }
    }

    /**
     * Read workload gets all products and then iterates over each one and gets the specific product.
     */
    private suspend fun WorkloadClient.readWorkload() {
        val products = getProducts()
        products.forEach { product ->
            getProduct(product.id)
            getProductCounter.incrementAndGet()
        }
        readCounter.incrementAndGet()
    }

    /**
     * Write workload creates a new product.
     */
    private suspend fun WorkloadClient.writeWorkload() {
        val product = generateProduct()
        postProduct(product)

        postProductCounter.incrementAndGet()
        writeCounter.incrementAndGet()
    }

    /**
     * DDOS workload reads all products 10 times in a row.
     */
    private suspend fun WorkloadClient.ddosWorkload() {
        repeat(10) {
            getProducts()
        }

        getProductsCounter.addAndGet(10)
        ddosCounter.incrementAndGet()
    }

    /**
     * Mixed workload reads all products, then creates a new product and fetches it afterward.
     */
    private suspend fun WorkloadClient.mixedWorkload() {
        getProducts()
        val product = generateProduct()
        postProduct(product)
        getProduct(product.id)

        getProductsCounter.incrementAndGet()
        postProductCounter.incrementAndGet()
        getProductCounter.incrementAndGet()
        mixedCounter.incrementAndGet()
    }

    /**
     * Helper function to generate a random workload type.
     */
    private fun Random.nextWorkload() = WorkloadType.entries[nextInt(WorkloadType.entries.size)]

    /**
     * Helper function to generate a new product with random id.
     */
    private fun generateProduct(): Product {
        val id = UUID.randomUUID().toString()
        val name = "Product $id"
        return Product(id, name)
    }

    private fun range(end: Int) = (1..end)
}