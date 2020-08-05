package com.example.fiubaredditserver.service

import com.example.fiubaredditserver.dao.CommentDAO
import com.example.fiubaredditserver.model.Comment
import com.example.fiubaredditserver.toComment
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class CommentService {
    private fun toComment(row: ResultRow) : Comment {
        var newComment = Comment(row[CommentDAO.postId],
                row[CommentDAO.commentId],
                "",
                row[CommentDAO.text],
                row[CommentDAO.image],
                row[CommentDAO.score])
        return newComment
    }

    suspend fun getAllComments() : List<Comment> = newSuspendedTransaction {
        CommentDAO.selectAll().map { toComment(it) }
    }

    fun addComment(pid: Int, ntext: String, nimage: String) {
        transaction {
            CommentDAO.insert {
                it[postId] = pid
                it[text] = ntext
                it[image] = nimage
            }
        }

    }
}