package dev.viniciusrangel.terrariacontrolpanel.controller

import dev.viniciusrangel.terrariacontrolpanel.server.ServerHandler
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.cookie.CookieConfiguration
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.micronaut.session.Session
import io.micronaut.session.SessionStore
import io.micronaut.session.http.CookieHttpSessionIdGenerator
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Secured(SecurityRule.IS_ANONYMOUS)
@ServerWebSocket("/ws/{token}")
@Controller("/ws")
class WebSocketController(
        private val server: ServerHandler
) {

    private val logged = mutableMapOf<String, Authentication>()

    private val loginToken = mutableMapOf<String, Authentication>()

//    private token

    @Get("/token")
    @Secured(SecurityRule.IS_AUTHENTICATED)
    @Produces(MediaType.TEXT_PLAIN)
    fun token(auth: Authentication): String {
        val uuid = UUID.randomUUID().toString()
        loginToken[uuid] = auth
        GlobalScope.launch {
            delay(10000)
            loginToken.remove(uuid)
        }
        return uuid
    }

    @OnOpen
    fun onOpen(
            token: String,
            session: WebSocketSession
    ) {
        logged[session.id] = loginToken.remove(token) ?: return session.close()
    }

    @OnMessage
    fun onMessage(
            msg: String,
            session: WebSocketSession
    ) {
        server.write(
                if (msg.startsWith('/')) {
                    msg.drop(1)
                } else {
                    val auth = logged[session.id]!!
                    "say [PANEL][${auth.name}] $msg"
                }
        )
    }

}