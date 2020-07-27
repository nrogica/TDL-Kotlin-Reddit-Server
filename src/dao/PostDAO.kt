package com.example.fiubaredditserver.dao

import com.example.fiubaredditserver.model.Comment
import org.jetbrains.exposed.sql.Table


object PostDAO : Table() {
    val postId = integer("id").autoIncrement()
    var title = varchar("title", length = 200)
    var text = varchar("text", length = 500)
    var image = varchar("image", length = 500).nullable()
    var score = integer("score").default(0)
    override val primaryKey = PrimaryKey(postId)
}
