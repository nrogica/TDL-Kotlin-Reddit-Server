package com.example.fiubaredditserver.model

data class Post(
    //var author: User,
    var postId : Int,
    var title: String?,
    var text: String?,
    var image: String,
    var score: Int = 0
) {

    //val comments: ArrayList<Comment> = ArrayList<Comment>()
    val comments: CommentListWrapper = CommentListWrapper()

    fun addComments(commentsToAdd : List<Comment>) {
        //commentsToAdd.map { comments.addIfNotExists(it) }
        commentsToAdd.map { comment -> comments.addIfNotExistsOrEmpty(comment) }
    }

    /*fun ArrayList<Comment>.addIfNotExists(comment : Comment) {
        if (comment in this) {
            this.add(comment)
        }
    }*/


}




