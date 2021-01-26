package com.example

import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class Bug : TestBase() {

  @RepeatedTest(1500)
  internal fun `immediately observing query from cache`(): Unit = runBlocking {
    val firstJob = launch {
      println("Making network only query")
      apollo.query(FooQuery())
        .toBuilder()
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .watcher()
        .toFlow()
        .first()
        .let { println("Network data fetched: $it") }
    }

    val data = withTimeoutOrNull(5.seconds) {
      println("Making cache only query")
      apollo.query(FooQuery())
        .toBuilder()
        .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
        .build()
        .watcher()
        .toFlow()
        .first { it.data != null }
    }

    println("Cache only query finished with data $data")
    if (data == null) {
      println("No data, querying again:")
      apollo.query(FooQuery())
        .toBuilder()
        .responseFetcher(ApolloResponseFetchers.CACHE_ONLY)
        .build()
        .watcher()
        .toFlow()
        .first()
        .let { println("Data from cache only query: $it") }

      error("No data!")
    }

    firstJob.cancel()
  }
}

fun println(str: String) = kotlin.io.println("[${System.currentTimeMillis() / 1000}] $str")
