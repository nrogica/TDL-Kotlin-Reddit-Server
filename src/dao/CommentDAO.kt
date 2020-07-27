package com.example.fiubaredditserver.dao

import org.jetbrains.exposed.sql.Table


object CommentDAO : Table() {
    //val postId = reference("post",PostDAO)
    //val userName
    var commentId = integer("commentId").autoIncrement()
    var postId = reference("postId", PostDAO.postId)
    var text = varchar("text", length = 500)
    var image = varchar("image", length = 500)
    var score = integer("score").default(0)
    override val primaryKey = PrimaryKey(postId, commentId)
}
