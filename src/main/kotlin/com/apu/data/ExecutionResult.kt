package com.apu.data

import kotlinx.serialization.Serializable

@Serializable
data class ExecutionResult(
    val message: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(message: String) = ExecutionResult(message = message)
        fun error(error: String) = ExecutionResult(error = error)
    }
}
