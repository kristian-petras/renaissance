package org.renaissance.common.model

data class WorkloadConfiguration(
    val host: String,
    val port: Int,
    val readWorkloadRepeatCount: Int,
    val writeWorkloadRepeatCount: Int,
    val ddosWorkloadRepeatCount: Int,
    val mixedWorkloadRepeatCount: Int,
    val workloadCount: Int,
    val maxThreads: Int,
    val workloadSelectorSeed: Long,
)