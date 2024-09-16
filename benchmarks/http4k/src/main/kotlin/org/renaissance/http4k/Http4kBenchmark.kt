package org.renaissance.http4k

import kotlinx.coroutines.runBlocking
import org.http4k.client.OkHttp
import org.renaissance.Benchmark
import org.renaissance.Benchmark.*
import org.renaissance.BenchmarkContext
import org.renaissance.BenchmarkResult
import org.renaissance.BenchmarkResult.Validators
import org.renaissance.License
import org.renaissance.http4k.workload.WorkloadClient
import org.renaissance.common.model.WorkloadConfiguration
import org.renaissance.common.utility.Utility.toWorkloadConfiguration
import org.renaissance.http4k.workload.WorkloadServer

@Name("http4k")
@Group("kotlin")
@Summary("Runs the http4k server and tests the throughput of the server by sending requests to the server.")
@Licenses(License.APACHE2)
@Repetitions(20)
@Parameter(
    name = "host",
    defaultValue = "localhost",
    summary = "Host of the server."
)
@Parameter(
    name = "port",
    defaultValue = "0",
    summary = "Port of the server."
)
@Parameter(
    name = "read_workload_repeat_count",
    defaultValue = "5",
    summary = "Number of read requests to generate."
)
@Parameter(
    name = "write_workload_repeat_count",
    defaultValue = "5",
    summary = "Number of write requests to generate."
)
@Parameter(
    name = "ddos_workload_repeat_count",
    defaultValue = "5",
    summary = "Number of ddos requests to generate."
)
@Parameter(
    name = "mixed_workload_repeat_count",
    defaultValue = "5",
    summary = "Number of mixed requests to generate."
)
@Parameter(
    name = "workload_count",
    defaultValue = "450",
    summary = "Number of workloads to generate. Each workload consists of read, write, ddos and mixed requests."
)
@Parameter(
    name = "max_threads",
    defaultValue = "\$cpu.count",
    summary = "Maximum number of threads to use for the executor of the requests."
)
@Parameter(
    name = "workload_selector_seed",
    defaultValue = "42",
    summary = "Seed used to generate random workloads."
)
@Configuration(name = "jmh")
class Http4kBenchmark : Benchmark {
    private lateinit var server: WorkloadServer
    private lateinit var client: WorkloadClient
    private lateinit var configuration: WorkloadConfiguration

    override fun run(context: BenchmarkContext): BenchmarkResult = runBlocking {
        val workloadSummary = client.workload()
        Validators.simple("Workload count", configuration.workloadCount.toLong(), workloadSummary.workloadCount)
    }

    override fun setUpBeforeEach(context: BenchmarkContext) {
        configuration = context.toWorkloadConfiguration()
        server = configuration.toWorkloadServer()
        server.start()

        // If port value is 0, server allocates an empty port which has to be saved to allow client requests.
        configuration = configuration.copy(port = server.port())
        client = configuration.toWorkloadClient()
    }

    override fun tearDownAfterEach(context: BenchmarkContext) {
        server.stop()
    }

    private fun WorkloadConfiguration.toWorkloadClient(): WorkloadClient =
        WorkloadClient(OkHttp(), this)

    private fun WorkloadConfiguration.toWorkloadServer(): WorkloadServer =
        WorkloadServer(port)
}




