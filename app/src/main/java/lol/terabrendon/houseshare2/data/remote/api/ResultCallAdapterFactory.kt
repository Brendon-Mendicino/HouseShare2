package lol.terabrendon.houseshare2.data.remote.api

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import lol.terabrendon.houseshare2.data.remote.util.convertResponse
import lol.terabrendon.houseshare2.domain.error.RemoteError
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


private abstract class CallDelegate<TIn, TOut>(
    protected val proxy: Call<TIn>,
) : Call<TOut> {
    override fun execute(): Response<TOut> = throw NotImplementedError()
    final override fun enqueue(callback: Callback<TOut>) = enqueueImpl(callback)
    final override fun clone(): Call<TOut> = cloneImpl()

    override fun cancel() = proxy.cancel()
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
    override fun isExecuted() = proxy.isExecuted
    override fun isCanceled() = proxy.isCanceled

    abstract fun enqueueImpl(callback: Callback<TOut>)
    abstract fun cloneImpl(): Call<TOut>
}

private class ResultCall<T : Any>(proxy: Call<T>) : CallDelegate<T, Result<T, RemoteError>>(proxy) {

    override fun enqueueImpl(callback: Callback<Result<T, RemoteError>>) =
        proxy.enqueue(object : Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                val result = convertResponse(response)

                callback.onResponse(this@ResultCall, Response.success(result))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                val result = when (t) {
                    is IOException -> RemoteError.NoConnection
                    else -> throw t
                }

                callback.onResponse(this@ResultCall, Response.success(Err(result)))
            }
        })

    override fun cloneImpl() = ResultCall(proxy.clone())
}

class ResultCallAdapter(
    private val type: Type,
) : CallAdapter<Type, Call<Result<Type, RemoteError>>> {
    override fun responseType() = type
    override fun adapt(call: Call<Type>): Call<Result<Type, RemoteError>> = ResultCall(call)
}

class ResultCallAdapterFactory private constructor() : CallAdapter.Factory() {
    companion object {
        fun create(): ResultCallAdapterFactory = ResultCallAdapterFactory()
    }

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation?>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) {
            return null
        }

        val callType = getParameterUpperBound(0, returnType as ParameterizedType)
        if (getRawType(callType) != Result::class.java) {
            return null
        }

        val resultType = getParameterUpperBound(0, callType as ParameterizedType)
        return ResultCallAdapter(resultType)
    }
}