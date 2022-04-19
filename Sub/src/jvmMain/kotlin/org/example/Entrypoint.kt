package org.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun Application.main(){
    Server().apply { main() }
}

fun main(){
    embeddedServer(Netty, port = 8080, host = "localhost") {
        main()
    }.start(true)
}