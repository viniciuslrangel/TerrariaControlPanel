package dev.viniciusrangel.terrariacontrolpanel.controller

import dev.viniciusrangel.terrariacontrolpanel.server.ServerHandler
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.cookie.CookieConfiguration
import io.micronaut.http.simple.cookies.SimpleCookie
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.AuthenticationUserDetailsAdapter
import io.micronaut.security.filters.SecurityFilter
import io.micronaut.security.rules.SecurityRule
import io.micronaut.session.Session
import io.micronaut.session.SessionStore
import io.micronaut.session.http.CookieHttpSessionIdGenerator
import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.ServerWebSocket

@Controller("/server")
@Secured(SecurityRule.IS_AUTHENTICATED)
class ServerController(
        private val server: ServerHandler
) {

    @Get("/start")
    fun start() {
        server.start()
    }

    @Get("/stop{?force}")
    fun stop(@QueryValue(defaultValue = "false") force: Boolean) {
        if(force) {
            server.forceStop()
        } else {
            server.write("exit")
        }
    }

    @Get("/status")
    @Produces(MediaType.APPLICATION_JSON)
    fun isOpen(): Map<String, Boolean> {
        return mapOf(
                "open" to server.isRunning
        )
    }

}