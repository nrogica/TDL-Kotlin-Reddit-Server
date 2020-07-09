package com.example.fiubaredditserver

import com.example.fiubaredditserver.model.Post
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.locations.put
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route

fun Route.postLocation(posts: MutableList<Post>) {

    get<PostLocation> {
        call.respond(posts)
    }

    post<PostLocation> {
        val parameters = call.receive<Parameters>()

        val title = parameters["title"]
        val text = parameters["text"]
        val image = parameters["image"]

        checkParameters(call, title, text,image)

        val newPost  = Post(title,text, image!!)
        posts.add(newPost)
        call.respond(HttpStatusCode.OK,"Post creado con id: ${newPost.id}")
    }

    put<Vote> {
       vote -> run {
            val postId = vote.id
            val action = vote.action

            if (!(postId is Int))
                call.respond(HttpStatusCode.BadRequest,"$postId no es un id de post valido")

            if (!(action is String) || !isValidVote(action.toLowerCase())) {
                call.respond(HttpStatusCode.BadRequest,"$action no es una accion valida para votar")
            }

            var post = posts.firstOrNull { actual -> actual.id == postId}

            if (post != null) {
                post.vote(action.toLowerCase())
                call.respond(HttpStatusCode.OK,"ok")
            } else {
                call.respond(HttpStatusCode.NotFound, postNotFoundMessage(postId))
            }

        }
    }
    put<EditPost> {
            edit -> run {
        val postId = edit.id
        val content = edit.content
        val newValue = edit.value

        if (!(postId is Int))
            call.respond(HttpStatusCode.BadRequest,"$postId no es un id de post valido")

        if (!(isValidContent(content))) {
            call.respond(HttpStatusCode.BadRequest,"Content: $content invalido")
        } else {
            var post = posts.firstOrNull { actual -> actual.id == postId}

            if (post != null) {
                if (content.equals("text",true)) post.text = newValue
                if (content.equals("image",true)) post.image = newValue
                call.respond(HttpStatusCode.OK,"ok")
            } else {
                call.respond(HttpStatusCode.NotFound, postNotFoundMessage(postId))
            }
        }
        }
    }

}

fun isValidContent(content: String): Boolean {
    return (content.equals("text",true) || content.equals("image", true))

}

fun isValidVote(action: String): Boolean {
    return (action.equals("upvote") || action.equals("downvote"))
}

fun postNotFoundMessage(id: Int) : String {
    return "No se encuentra post con id: $id"
}

suspend fun checkParameters(
    call: ApplicationCall,
    title: String?,
    text: String?,
    image: String?
) {
    if (text == null) {
        call.respond(HttpStatusCode.BadRequest,"No se indica parametro text")
    }

    if (image.isNullOrBlank()) {
        call.respond(HttpStatusCode.BadRequest,"Error en parametro url")
    }
}

