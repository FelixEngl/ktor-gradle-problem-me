package org.example

import io.ktor.events.EventDefinition
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.html.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.title

val ServerInitializedEvent = EventDefinition<Server>()

class Server : AutoCloseable {
    fun Application.main(){

        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown"
            exitCodeSupplier = { 0 }
        }

        registerOwnEvents()
        initialize(environment.config)
        configureRouting()
        environment.monitor.raise(ServerInitializedEvent, this@Server)
    }

    private fun initialize(cfg: ApplicationConfig){
        println("cfg-Class: ${cfg::class.qualifiedName}")
        try {
            cfg.config("doesnotexist")
        } catch (e: ApplicationConfigurationException) {
            println("Does not exists works.")
        } catch (e: com.typesafe.config.ConfigException.Missing){
            println("This one was not specified!")
            println("Name of Exception: ${e::class.qualifiedName}")
            e.printStackTrace()
        } catch (e: Exception){
            println("Why does it not recognize the ApplicationConfigurationException?")
            println("Name of Exception: ${e::class.qualifiedName}")
            e.printStackTrace()
        }
    }

    private fun Application.registerOwnEvents(){
        environment.monitor.subscribe(ApplicationStopped){
            close()
        }
    }

    private fun Application.configureRouting(){
        routing {
            get("/"){
                call.respondHtml(HttpStatusCode.OK) {
                    head { title { +"I'm an example!" } }
                    body { h1 { +"Body." } }
                }
            }
        }
    }

    override fun close() {
        println("Close Called!")
    }
}