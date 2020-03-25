package com.github.clojj.ktor_quickstart

import arrow.core.Option
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.ContentNegotiation
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import kotlinx.css.*
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import java.util.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


val snippets: MutableList<Snippet> = Collections.synchronizedList(mutableListOf(
    Snippet("snippet 000"),
    Snippet("hello"),
    Snippet("world")
))

open class SimpleJWT(secret: String) {
    private val algorithm: Algorithm = Algorithm.HMAC256(secret)
    val verifier: JWTVerifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

class User(val name: String, val password: String)

object UserRepo {
    private val users: Map<String, User> =
        listOf(
            User("u1", "test"),
            User("u2", "u2")
        )
            .associateBy { it.name }
            .toMutableMap()

    fun lookupUser(name: String) = Option.fromNullable(users[name])
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient(Apache) {
    }
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT) // Pretty Prints the JSON
        }
    }

    val simpleJwt = SimpleJWT("my-super-secret-47")

    install(Authentication) {
        jwt {
            verifier(simpleJwt.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    routing {

        post("/login-register") {
            val loginRegister = call.receive<LoginRegister>()
            val user = UserRepo.lookupUser(loginRegister.user)
            user.fold({
                call.respond(HttpStatusCode.Unauthorized, "Unknown user")
            }) {
                if (it.password != loginRegister.password) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid password")
                } else {
                    call.respond(mapOf("token" to simpleJwt.sign(it.name)))
                }
            }
        }

        route("/snippets") {
            get {
                call.respond(
                    snippetsDto(
                        snippets
                    )
                )
            }

            authenticate {
                post {
                    val postSnippet = call.receive<PostSnippet>()
                    snippets += Snippet(
                        postSnippet.snippet.text
                    )
                    call.respond(mapOf("OK" to true) + snippetsDto(
                        snippets
                    )
                    )
                }
            }
        }

        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }
    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
