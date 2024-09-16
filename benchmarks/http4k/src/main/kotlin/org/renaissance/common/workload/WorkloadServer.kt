package org.renaissance.common.workload

internal interface WorkloadServer {
    fun start()
    fun stop()
    fun port(): Int
}