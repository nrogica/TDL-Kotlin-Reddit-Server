package com.example.fiubaredditserver.service

import com.example.fiubaredditserver.dao.CommentDAO
import com.example.fiubaredditserver.dao.PostDAO
import com.example.fiubaredditserver.model.Comment
import com.example.fiubaredditserver.model.Post
import com.example.fiubaredditserver.toComment
import javafx.geometry.Pos
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class PostService {

    private suspend fun getCommentsForPostId (postId : Int) : List<Comment> = newSuspendedTransaction {
        CommentDAO.select { CommentDAO.postId eq postId }.map { toComment(it) }
    }

    private suspend fun toPost(row: ResultRow) : Post {
        var newPost = Post(row[PostDAO.postId], row[PostDAO.title], row[PostDAO.text], "", row[PostDAO.score])
        val comments = getCommentsForPostId(newPost.postId)
        newPost.addComments(comments)
        return newPost
    }


    suspend fun getAllPosts() : List<Post> = newSuspendedTransaction {
        PostDAO.selectAll().map { toPost(it) }
    }

    suspend fun addPost(postTitle: String, postText: String, postImage: String) = newSuspendedTransaction {
        transaction {
            PostDAO.insert {
                it[title] = postTitle
                it[text] =  postText
                it[image] = postImage
            }
        }
    }

    suspend fun getPostById(id: Int) : Post? {
        val res = transaction {
            PostDAO.select { PostDAO.postId eq id }.firstOrNull()
        }
        if (res != null) {
            return toPost(res)
        }
        return res
    }

    fun vote(post: Post, action: String) {
        var nscore : Int
        if (action.equals("upvote")) {
            nscore = 1 + post.score
        } else {
            nscore = -1 + post.score
        }
        transaction {
            PostDAO.update({ PostDAO.postId eq post.postId }) {
               it.set(PostDAO.score, nscore)
            }
        }

    }

}