package com.example.fiubaredditserver

import com.example.fiubaredditserver.dao.CommentDAO
import com.example.fiubaredditserver.dao.PostDAO
import com.example.fiubaredditserver.model.Comment
import com.example.fiubaredditserver.model.Post
import com.example.fiubaredditserver.service.CommentService
import com.example.fiubaredditserver.service.PostService
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
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


suspend fun toPost(row: ResultRow) : Post {
    var newPost = Post(row[PostDAO.postId], row[PostDAO.title], row[PostDAO.text], "")
    val comments = getCommentsForPostId(newPost.postId)
    newPost.addComments(comments)
    return newPost
}

fun toComment(row: ResultRow) : Comment {
    var newComment = Comment(row[CommentDAO.postId],
                             row[CommentDAO.commentId],
                             "",
                             row[CommentDAO.text],
                             row[CommentDAO.image],
                             row[CommentDAO.score])
    return newComment
}

suspend fun getCommentsForPostId (postId : Int) : List<Comment> = newSuspendedTransaction {
    CommentDAO.select { CommentDAO.postId eq postId }.map { toComment(it)}
}

fun Route.postLocation(postService: PostService, commentService: CommentService) {

    get<PostLocation> {

        //call.respond(getAllPosts())
        call.respond(postService.getAllPosts())
    }

    get<GetComments> {
        //call.respond(getAllComments())
        call.respond(commentService.getAllComments())
    }

    post<PostLocation> {
        val parameters = call.receive<Parameters>()

        val title = parameters["title"].orEmpty()
        val text = parameters["text"].orEmpty()
        val image = parameters["image"].orEmpty()

        checkParameters(call, title, text,image)
        postService.addPost(title, text, image)
        call.respond(HttpStatusCode.OK,"Post creado")
    }

    post<AddComent> {
        val parameters = call.receive<Parameters>()

        val pid = it.postId
        val ntext : String = parameters["text"].orEmpty()
        val nimage = parameters["image"].orEmpty()
        commentService.addComment(pid, ntext, nimage)
        call.respond(HttpStatusCode.OK,"Se agrega comentario para el Post con id: ${pid}")
    }

    put<Vote> {
       vote -> run {
            val postId = vote.id
            val action = vote.action

            if (postId !is Int)
                call.respond(HttpStatusCode.BadRequest,"$postId no es un id de post valido")

            if (action !is String || !isValidVote(action.toLowerCase())) {
                call.respond(HttpStatusCode.BadRequest,"$action no es una accion valida para votar")
            }

            //var post = posts.firstOrNull { actual -> actual.postId == postId}
            var post = postService.getPostById(postId)

            if (post != null) {
                postService.vote(post, action.toLowerCase())
                call.respond(HttpStatusCode.OK,"ok")
            } else {
                call.respond(HttpStatusCode.NotFound, postNotFoundMessage(postId))
            }

        }
    }
/*

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
            var post = posts.firstOrNull { actual -> actual.postId == postId}

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
*/
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

