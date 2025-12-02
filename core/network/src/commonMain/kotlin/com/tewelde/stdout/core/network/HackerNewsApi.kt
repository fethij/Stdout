package com.tewelde.stdout.core.network

import com.tewelde.stdout.core.network.model.NetworkComment
import com.tewelde.stdout.core.network.model.NetworkStory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@Inject
@SingleIn(AppScope::class)
class HackerNewsApi {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getStories(type: String): List<Long> {
        return client.get("https://hacker-news.firebaseio.com/v0/$type.json").body()
    }

    suspend fun getStory(id: Long): NetworkStory {
        return client.get("https://hacker-news.firebaseio.com/v0/item/$id.json").body()
    }

    suspend fun getComment(id: Long): NetworkComment {
        return client.get("https://hacker-news.firebaseio.com/v0/item/$id.json").body()
    }
}
