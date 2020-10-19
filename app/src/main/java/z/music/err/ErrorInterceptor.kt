package z.music.err

import okhttp3.Interceptor
import okhttp3.Response

class ErrorInterceptor : Interceptor {
    companion object {
        private val TAG = ErrorInterceptor::class.simpleName
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val string = response.peekBody(response.body()?.contentLength() ?: 0).string()
        if (string.contains("error")) {
            response.close()
            throw TokenExpiredException()
        }
        return response
    }
}