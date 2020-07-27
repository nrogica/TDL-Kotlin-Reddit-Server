package com.example.fiubaredditserver.model

data class Post(
    //var author: User,
    var postId : Int,
    var title: String?,
    var text: String?,
    var image: String
) {
    var score: Long = 0
    val comments: ArrayList<Comment> = ArrayList<Comment>()

    fun vote(action: String) {
        when (action) {
            "upvote" -> { score++ }
            "downvote" -> { score-- }
            }
        }

    fun addComments(commentsToAdd : List<Comment>) {
        if (comments.isEmpty()) {
            comments.addAll(commentsToAdd)
        } else {
            commentsToAdd.map { comments.addIfNotExists(it) }
        }
    }

    fun ArrayList<Comment>.addIfNotExists(comment: Comment) {
        if (comment !in this) {
            this.add(comment)
        }
    }
}

