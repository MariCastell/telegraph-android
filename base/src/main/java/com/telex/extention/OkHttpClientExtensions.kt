package com.telex.extention

import com.telex.model.source.local.ProxyServer
import java.net.Authenticator
import java.net.InetSocketAddress
import java.net.PasswordAuthentication
import java.net.Proxy
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import okhttp3.OkHttpClient

/**
 * @author Sergey Petrov
 */
fun OkHttpClient.Builder.withDefaults(): OkHttpClient.Builder {
    readTimeout(30, TimeUnit.SECONDS)
    connectTimeout(30, TimeUnit.SECONDS)
    writeTimeout(60, TimeUnit.SECONDS)
    return this
}

fun OkHttpClient.Builder.withProxy(proxyServer: ProxyServer?): OkHttpClient.Builder {
    val proxy = if (proxyServer != null && proxyServer.enabled) proxyServer else null
    if (proxy != null) {
        Authenticator.setDefault(object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(proxy.user, proxy.password?.toCharArray())
            }
        })

        proxy(Proxy(Proxy.Type.valueOf(proxy.type.name), InetSocketAddress.createUnresolved(proxy.host, proxy.port)))
    } else {
        Authenticator.setDefault(null)
    }
    return this
}

fun OkHttpClient.Builder.ignoreSSL(): OkHttpClient.Builder {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        @Throws(CertificateException::class)
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        }

        @Throws(CertificateException::class)
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        }
    })

    val sslContext = SSLContext.getInstance("SSL").apply {
        init(null, trustAllCerts, SecureRandom())
    }
    sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}
