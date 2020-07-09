package com.example.fiubaredditserver

import com.example.fiubaredditserver.model.Post
import com.example.fiubaredditserver.model.User
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.gson.*
import io.ktor.features.*
import io.ktor.locations.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

@Location("/post")
class PostLocation()

@Location("/post/{id}/{action}")
class Vote(val action: String,val id: Int)

@Location("/post/edit/{id}/{content}/{value}")
class EditPost(val id: Int, val content: String, val value:String)

fun main(args: Array<String>) {
    val server = embeddedServer(
        Netty,
        watchPaths = listOf("server"),
        module = Application::serverModule,
        port = 8080)

    server.start(true)
}

@Suppress("unused") // Referenced in application.conf
fun Application.serverModule() {
    install(Locations)

    install(ContentNegotiation) {
        gson {

        }
    }

    val users = mutableListOf<User>()
    val posts = mutableListOf<Post>()
    routing {
        postLocation(posts)

        route ("/") {
            get {
                call.respond(HttpStatusCode.OK,"Fiuba Reddit Server")
            }
        }

        //TODO armar locations para usuarios una vez definido el comportamiento
        route("/user") {
            get { call.respond(users) }

            post {
                users.add(call.receive<User>())
                call.respond("User created ")
            }
            put {

                val receivedUser = call.receive<User>()
                val result =
                    when (users.contains(receivedUser)) {
                        true -> {
                            users[users.indexOf(receivedUser)] = receivedUser
                            call.respond(HttpStatusCode.OK)
                        }
                        false -> call.respond(HttpStatusCode.NotFound, "User not found")
                    }
            }
            delete("/{id}") {
                val receivedId = call.parameters["id"]?.toLongOrNull()
                val result =
                    when (receivedId) {
                        null -> call.respond(HttpStatusCode.BadRequest, "Invalid id: $receivedId")
                        else -> {
                            val user = users.firstOrNull { it.id == receivedId }
                            when (user) {
                                null -> call.respond(HttpStatusCode.NotFound, "User with id: $receivedId not found")
                                else -> {
                                    users.remove(user)
                                    call.respond(HttpStatusCode.OK, "User with id: $receivedId deleted")
                                }
                            }
                        }
                    }
            }
        }

    }
}




