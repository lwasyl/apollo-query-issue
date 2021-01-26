package com.example

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.lru.EvictionPolicy
import com.apollographql.apollo.cache.normalized.lru.LruNormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
import okhttp3.OkHttpClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension

abstract class TestBase {
  @RegisterExtension
  @JvmField
  protected val mockWebServer = MockWebServerTestRule()

  protected lateinit var apollo: ApolloClient

  @BeforeEach
  internal fun setUp() {
    val inMemoryCache = LruNormalizedCacheFactory(EvictionPolicy.NO_EVICTION)
    val sqlCache = SqlNormalizedCacheFactory("jdbc:sqlite:")
    apollo = ApolloClient.builder()
      .normalizedCache(inMemoryCache.chain(sqlCache), IdBasedCacheKeyResolver(), false)
      .serverUrl(mockWebServer.mockWebServer.url("/"))
      .okHttpClient(OkHttpClient.Builder().build())
      .build()
  }
}
