package com.pharmachain.ai.core.common.result

sealed interface NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : NetworkResult<Nothing>
    data object Loading : NetworkResult<Nothing>
}
