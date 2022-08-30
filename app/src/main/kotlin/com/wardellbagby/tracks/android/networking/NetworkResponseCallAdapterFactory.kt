package com.wardellbagby.tracks.android.networking

import android.util.Log
import com.wardellbagby.tracks.android.networking.NetworkResult.Failure
import com.wardellbagby.tracks.android.networking.NetworkResult.Success
import com.wardellbagby.tracks.models.DefaultServerResponse
import com.wardellbagby.tracks.models.ServerResponse
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

typealias DefaultNetworkResult = NetworkResult<DefaultServerResponse>

sealed interface NetworkResult<out T> {
  data class Success<T>(val response: T) : NetworkResult<T>
  data class Failure(val message: String? = null) : NetworkResult<Nothing>
}

class UnknownNetworkingFailureException(
  override val message: String? = null
) : Exception()

fun <T> NetworkResult<T>.asFailureWhen(condition: (T) -> Boolean): NetworkResult<T> {
  return if (this is Success && condition(response)) {
    Failure()
  } else {
    this
  }
}

fun <T, R> NetworkResult<T>.mapResponse(mapper: (T) -> R): NetworkResult<R> {
  return when (this) {
    is Failure -> this
    is Success -> {
      runCatching { mapper(response) }
        .fold(
          onSuccess = {
            Success(it)
          },
          onFailure = {
            Log.e("NetworkResponse - mapResponse", "Failed to map NetworkResponse", it)
            Failure(message = null)
          }
        )
    }
  }
}

fun <T> Response<T>.errorMessage(call: Call<T>): String =
  "${code()} ${message()} - ${call.request().url()}"

private class NetworkResultCall<T>(
  private val delegate: Call<T>,
  private val successBodyType: Type
) : Call<NetworkResult<T>> {

  override fun enqueue(callback: Callback<NetworkResult<T>>) = synchronized(this) {
    delegate.enqueue(object : Callback<T> {
      override fun onResponse(call: Call<T>, response: Response<T>) {
        val body = response.body()
        if (response.isSuccessful && body != null) {
          if (body is ServerResponse) {
            if (body.success) {
              callback.onResponse(
                this@NetworkResultCall,
                Response.success(Success(body))
              )
            } else {
              callback.onResponse(
                this@NetworkResultCall,
                Response.success(Failure(message = body.errorDetailMessage))
              )
            }
          } else {
            callback.onResponse(
              this@NetworkResultCall,
              Response.success(Failure(message = null))
            )
          }
        } else {
          val exception = UnknownNetworkingFailureException(message = response.errorMessage(call))
          Log.e("NetworkResponse", "Network failure", exception)

          callback.onResponse(
            this@NetworkResultCall,
            Response.success(Failure(message = null))
          )
        }
      }

      override fun onFailure(call: Call<T>, throwable: Throwable) {
        Log.e("NetworkResponse", "Network failure", throwable)
        callback.onResponse(
          this@NetworkResultCall,
          Response.success(Failure(message = null))
        )
      }
    })
  }

  override fun isExecuted(): Boolean = synchronized(this) {
    delegate.isExecuted
  }

  override fun clone() = NetworkResultCall(delegate.clone(), successBodyType)

  override fun isCanceled(): Boolean = synchronized(this) {
    delegate.isCanceled
  }

  override fun cancel() = synchronized(this) {
    delegate.cancel()
  }

  override fun execute(): Response<NetworkResult<T>> {
    error("Don't use execute. No, I don't care that you're on a background thread.")
  }

  override fun request(): Request = delegate.request()

  override fun timeout(): Timeout = delegate.timeout()
}

class NetworkResponseAdapter<S : Any>(
  private val responseType: Type
) : CallAdapter<S, Call<NetworkResult<S>>> {

  override fun responseType(): Type = responseType

  override fun adapt(call: Call<S>): Call<NetworkResult<S>> {
    return NetworkResultCall(call, responseType)
  }
}

class NetworkResultAdapterFactory
private constructor() : CallAdapter.Factory() {

  companion object {
    fun create(): NetworkResultAdapterFactory = NetworkResultAdapterFactory()
  }

  override fun get(
    returnType: Type,
    annotations: Array<Annotation>,
    retrofit: Retrofit
  ): CallAdapter<*, *>? {
    if (returnType !is ParameterizedType) {
      // This can't be a Call<NetworkResponse> type without a generic parameter.
      return null
    }

    val responseUpperType = getParameterUpperBound(0, returnType)

    if (getRawType(responseUpperType) != NetworkResult::class.java) {
      return null
    }

    if (responseUpperType !is ParameterizedType) {
      // This can't be a NetworkResponse<*> type without a generic parameter.
      return null
    }

    val responseType = getParameterUpperBound(0, responseUpperType)

    return NetworkResponseAdapter<Any>(responseType)
  }
}