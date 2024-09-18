package org.renaissance.common.utility

import org.renaissance.BenchmarkContext
import org.renaissance.common.model.WorkloadConfiguration

internal object Utility {
    fun BenchmarkContext.toWorkloadConfiguration(): WorkloadConfiguration = WorkloadConfiguration(
        host = parameter("host").value(),
        port = parameter("port").value().toInt(),
        readWorkloadRepeatCount = parameter("read_workload_repeat_count").value().toInt(),
        writeWorkloadRepeatCount = parameter("write_workload_repeat_count").value().toInt(),
        ddosWorkloadRepeatCount = parameter("ddos_workload_repeat_count").value().toInt(),
        mixedWorkloadRepeatCount = parameter("mixed_workload_repeat_count").value().toInt(),
        workloadCount = parameter("workload_count").value().toInt(),
        maxThreads = parameter("max_threads").value().toInt(),
        workloadSelectionSeed = parameter("workload_selection_seed").value().toLong(),
        clientFramework = parameter("client_framework").value(),
        clientEngine = parameter("client_engine").value(),
        serverFramework = parameter("server_framework").value(),
        serverEngine = parameter("server_engine").value()
    )
}
