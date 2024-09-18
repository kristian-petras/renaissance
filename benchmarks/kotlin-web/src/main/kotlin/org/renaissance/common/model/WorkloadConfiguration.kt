package org.renaissance.common.model

internal data class WorkloadConfiguration(
    val host: String,
    val port: Int,
    val readWorkloadRepeatCount: Int,
    val writeWorkloadRepeatCount: Int,
    val ddosWorkloadRepeatCount: Int,
    val mixedWorkloadRepeatCount: Int,
    val workloadCount: Int,
    val maxThreads: Int,
    val workloadSelectionSeed: Long,
    val clientFramework: String,
    val clientEngine: String,
    val serverFramework: String,
    val serverEngine: String
)