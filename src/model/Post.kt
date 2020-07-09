package com.example.fiubaredditserver.model

data class Post(
        //var author: User,
    var title: String?,
    var text: String?,
    var image: String
) {
    //TODO sacar el companion object una vez implementada la persistencia
    companion object {
        var postId = 1
    }
    var score: Long = 0
    val comments: ArrayList<Post> = ArrayList<Post>()
    var id = postId++

    fun vote(action: String) {
        when (action) {
            "upvote" -> { score++ }
            "downvote" -> { score-- }
            }
        }
}

