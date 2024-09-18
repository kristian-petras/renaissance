package org.renaissance

import kotlinx.coroutines.runBlocking
import org.renaissance.Benchmark.Configuration
import org.renaissance.Benchmark.Group
import org.renaissance.Benchmark.Licenses
import org.renaissance.Benchmark.Name
import org.renaissance.Benchmark.Parameter
import org.renaissance.Benchmark.Repetitions
import org.renaissance.Benchmark.Summary
import org.renaissance.BenchmarkResult.Validators
import org.renaissance.common.model.WorkloadConfiguration
import org.renaissance.common.utility.Utility.toWorkloadConfiguration
import org.renaissance.common.workload.WorkloadClient
import org.renaissance.common.workload.WorkloadGenerator
import org.renaissance.common.workload.WorkloadServer
import org.renaissance.http4k.Http4kWorkloadClient
import org.renaissance.http4k.Http4kWorkloadServer
import org.renaissance.ktor.KtorWorkloadClient
import org.renaissance.ktor.KtorWorkloadServer

@Name("kotlin-web")
@Group("kotlin")
@Summary("Runs the web server and tests the throughput of the server by sending requests to the server.")
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
    name = "workload_selection_seed",
    defaultValue = "42",
    summary = "Seed used to generate random workloads."
)
@Parameter(
    name = "client_framework",
    defaultValue = "http4k",
    summary = "Framework used to send requests to the server. Supported frameworks are 'http4k' and 'ktor'."
)
@Parameter(
    name = "client_engine",
    defaultValue = "apache",
    summary = "Client engine used to send requests to the server.\n" +
            "Supported engines for http4k are 'apache', 'jetty', 'okhttp' and 'helidon'.\n" +
            "Supported engines for ktor are 'apache', 'jetty', 'okhttp' and 'cio'."
)
@Parameter(
    name = "server_framework",
    defaultValue = "http4k",
    summary = "Framework used to process requests. Supported frameworks are 'http4k' and 'ktor'."
)
@Parameter(
    name = "server_engine",
    defaultValue = "jetty",
    summary = "Server engine used to process requests.\n" +
            "Supported engines for http4k are 'apache', 'jetty', 'undertow' and 'helidon'.\n" +
            "Supported engines for ktor are 'apache', 'jetty', 'tomcat' and 'cio'."
)
@Configuration(
    name = "test",
    settings = [
        "max_threads = 2",
        "workload_count = 100",
    ]
)
@Configuration(name = "jmh")
internal class WorkloadBenchmark : Benchmark {
    private lateinit var server: WorkloadServer
    private lateinit var client: WorkloadClient

    private lateinit var workloadGenerator: WorkloadGenerator
    private lateinit var configuration: WorkloadConfiguration

    override fun run(context: BenchmarkContext): BenchmarkResult = runBlocking {
        val workloadSummary = workloadGenerator.runWorkload()
        Validators.simple("Workload count", configuration.workloadCount.toLong(), workloadSummary.workloadCount)
    }

    override fun setUpBeforeEach(context: BenchmarkContext) {
        configuration = context.toWorkloadConfiguration()
        server = configuration.toWorkloadServer()
        server.start()

        // If the port value is 0, the server allocates an empty port which has to be saved to allow client requests.
        configuration = configuration.copy(port = server.port())
        client = configuration.toWorkloadClient()

        workloadGenerator = WorkloadGenerator(client, configuration)
    }

    override fun tearDownAfterEach(context: BenchmarkContext) {
        server.stop()
    }

    private fun WorkloadConfiguration.toWorkloadClient(): WorkloadClient = when (clientFramework) {
        "http4k" -> Http4kWorkloadClient.create(this)
        "ktor" -> KtorWorkloadClient.create(this)
        else -> throw IllegalArgumentException("Unsupported client framework: $clientFramework")
    }

    private fun WorkloadConfiguration.toWorkloadServer(): WorkloadServer = when (serverFramework) {
        "http4k" -> Http4kWorkloadServer.create(this)
        "ktor" -> KtorWorkloadServer.create(this)
        else -> throw IllegalArgumentException("Unsupported server framework: $serverFramework")
    }
}