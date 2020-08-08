package com.example.fiubaredditserver.model

class CommentListWrapper {

    val comments: ArrayList<Comment> = ArrayList<Comment>()
    var middleCommentId : Int = 0

    fun addIfNotExistsOrEmpty(comment: Comment) {
        if (comments.isEmpty() || (comment !in comments)) {
            comments.add(comment)
        }
        middleCommentId = findMiddleCommentId();
    }

    private fun findMiddleCommentId() : Int {
        if (comments.size == 1) {
            return comments.first().postId
        } else {
            return comments.get((comments.size - 1) / 2).postId
        }
    }
}