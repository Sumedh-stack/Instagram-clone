package com.chatt.android.photorateapp.Model

import android.widget.ImageView

class User {
    private var username = ""
    private var fullname = ""
    private var bio = ""
    private var image = ""
    private var uid = ""
    constructor()
    constructor(
        uid: String,
        fullname: String,
        username: String,
        email: String,
        bio: String,
        image: String
    ) {
        this.uid = uid
        this.fullname = fullname
        this.username = username
        this.bio = bio
        this.image = image
    }

    fun getUsername(): String {
        return username
    }

    fun setUsername(username:String) {
        this.username = username
    }

    fun getFullname(): String {
        return fullname
    }

    fun setFullname(fullname:String) {
        this.fullname = fullname
    }

    fun getUID(): String {
        return uid
    }

    fun setUID(uid:String) {
        this.uid = uid
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image:String) {
        this.image = image
    }
    fun getBio(): String {
        return bio
    }

    fun setBio(bio:String) {
        this.bio = bio
    }

}