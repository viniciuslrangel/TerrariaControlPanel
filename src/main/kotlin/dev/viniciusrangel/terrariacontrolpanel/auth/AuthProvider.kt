package dev.viniciusrangel.terrariacontrolpanel.auth

import io.micronaut.http.HttpRequest
import io.micronaut.security.authentication.*
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import javax.inject.Singleton

@Singleton
class AuthProvider(
        private val db: LoginDatabase
) : AuthenticationProvider {

    override fun authenticate(httpRequest: HttpRequest<*>?, authenticationRequest: AuthenticationRequest<*, *>?): Publisher<AuthenticationResponse> {
        if(authenticationRequest != null) {
            val user = authenticationRequest.identity
            val pass = authenticationRequest.secret
            if(db.validateUser(user, pass) && user is String) {
                return Flowable.just(UserDetails(user, emptyList()))
            }
        }
        return Flowable.just(AuthenticationFailed())
    }

}