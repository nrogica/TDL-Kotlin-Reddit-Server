package com.example.fiubaredditserver.model

data class Comment(
        val postId: Int,
        val commentId: Int,
        val userName: String,
        var text: String?,
        var image: String?,
        var score: Int = 0
) {}