package dev.teamnight.aegis.libaegis.kratos

interface FlowState {
    val flowId: String
}

interface FlowStateStep<D> : FlowState {
    val data: D
}

interface FlowStateCreated<D> : FlowStateStep<D>

interface FlowStateUpdated<D> : FlowStateStep<D>

interface FlowStateCompleted<R> : FlowState {
    val result: R
}