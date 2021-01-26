package com.example

import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.cache.normalized.CacheKey
import com.apollographql.apollo.cache.normalized.CacheKeyResolver
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.InputStream

class MockWebServerTestRule : BeforeEachCallback, AfterEachCallback {

  lateinit var mockWebServer: MockWebServer
    private set

  override fun beforeEach(context: ExtensionContext?) {
    mockWebServer = MockWebServer()

    mockWebServer.dispatcher = object : Dispatcher() {
      override fun dispatch(request: RecordedRequest): MockResponse {
        val body = MockResponse().setBody(textFromFile("response.json"))
        println("Dispatching ${request.getHeader("X-APOLLO-OPERATION-NAME")}")
        return body

      }
    }

    println("Starting web server")
    mockWebServer.start()
  }

  override fun afterEach(context: ExtensionContext?) {
    println("Closing web server")
    mockWebServer.close()
  }
}

internal class IdBasedCacheKeyResolver : CacheKeyResolver() {

  override fun fromFieldRecordSet(field: ResponseField, recordSet: Map<String, Any>): CacheKey =
    (recordSet["id"] as? String).asCacheKey()

  override fun fromFieldArguments(field: ResponseField, variables: Operation.Variables): CacheKey =
    (field.resolveArgument("id", variables) as? String).asCacheKey()

  private fun String?.asCacheKey(): CacheKey =
    takeIf { !it.isNullOrBlank() }
      ?.let { CacheKey.from(it) }
      ?: CacheKey.NO_KEY
}

fun textFromFile(filename: String): String =
  getResourceStream(filename)
    .bufferedReader()
    .use { it.readText() }

private object ResourceUtils

private fun getResourceStream(filename: String): InputStream =
  checkNotNull(ResourceUtils::class.java.classLoader.getResourceAsStream(filename)) {
    "No file called \"$filename\" in resources"
  }
