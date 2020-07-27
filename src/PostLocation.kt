package com.example.fiubaredditserver

import com.example.fiubaredditserver.dao.CommentDAO
import com.example.fiubaredditserver.dao.PostDAO
import com.example.fiubaredditserver.model.Comment
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
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction


suspend fun toPost(row: ResultRow) : Post {
    var newPost = Post(row[PostDAO.postId], row[PostDAO.title], row[PostDAO.text], "asdaurl")
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

suspend fun getAllPosts() : List<Post> = newSuspendedTransaction {
    PostDAO.selectAll().map { toPost(it) }
}

suspend fun getAllComments() : List<Comment> = newSuspendedTransaction {
    CommentDAO.selectAll().map { toComment(it) }
}

suspend fun getPost(postId: Int) : Post = newSuspendedTransaction {
    var query = PostDAO.select { PostDAO.postId eq postId }
    query.map { toPost(it) }.first()
}

fun Route.postLocation(posts: MutableList<Post>) {

    get<PostLocation> {

        call.respond(getAllPosts())
    }

    get<GetComments> {
        call.respond(getAllComments())
    }

    post<PostLocation> {
        val parameters = call.receive<Parameters>()

        val title = parameters["title"]
        val text = parameters["text"]
        val image = parameters["image"]

        checkParameters(call, title, text,image)

        val newPost  = Post(1,title,text, image!!)
        posts.add(newPost)
        call.respond(HttpStatusCode.OK,"Post creado con id: ${newPost.postId}")
    }

    post<AddComent> {
        val parameters = call.receive<Parameters>()

        val pid = it.postId
        val ntext : String = parameters["text"].orEmpty()
        val nimage = parameters["image"].orEmpty()

        transaction {
            CommentDAO.insert {
                it[postId] = pid.toInt()
                it[text] = ntext
                it[image] = nimage
            }
        }

        call.respond(HttpStatusCode.OK,"Se agrega comentario para el Post con id: ${pid}")
        //val newComment = Comment(postId.toInt(), "",text,image)






        /*        val postId: Int,
        val userName: String,
        var text: String?,
        var image: String?*/
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

            var post = posts.firstOrNull { actual -> actual.postId == postId}

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

