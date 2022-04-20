package org.example

import com.typesafe.config.ConfigFactory
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import kotlin.test.Test
import kotlin.test.assertEquals

class Test {
    companion object {

        val server = Server()

        val tesApp = TestApplication {
            environment {
                config = HoconApplicationConfig(
                    ConfigFactory.parseMap(
                        mapOf(
                            "ktor" to mapOf(
                                "deployment" to mapOf(
                                    "port" to 8080
                                )
                            )
                        )
                    )
                )
            }
            application {
                this@application.environment.monitor.subscribe(ServerInitializedEvent) {
                    println("Initialized. Yay!")
                }
                server.apply { main() }
            }
        }

        @Suppress("unused")
        @AfterAll
        @JvmStatic
        fun teardown() {
            tesApp.stop()
            server.close()
        }
    }

    private inline fun withClient(crossinline block: suspend (client: HttpClient) -> Unit){
        runBlocking {
            val client = tesApp.createClient {
                install(ContentNegotiation) {
                    json()
                }
            }
            try {
                block(client)
            } finally {
                // TODO:
                //  Error:
                //  Cannot access 'io.ktor.utils.io.core.Closeable' which is a supertype of
                //  'io.ktor.client.HttpClient'. Check your module classpath for missing or
                //  conflicting dependencies
                client.close()
            }
        }
    }

    @Test
    fun exampleTest(){
        withClient { client ->
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }
}