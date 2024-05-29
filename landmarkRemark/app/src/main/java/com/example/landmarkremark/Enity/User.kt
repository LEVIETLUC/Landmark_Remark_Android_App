package com.example.landmarkremark.Enity

class User {
    private var id: String? = null
    private var username: String? = null
    private var password: String? = null

    constructor() {}

    constructor(username: String, password: String) {
        this.username = username
        this.password = password
    }

    constructor(id: String, username: String, password: String) {
        this.id = id
        this.username = username
        this.password = password
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getUsername(): String? {
        return username
    }

    fun setUsername(username: String) {
        this.username = username
    }

    fun getPassword(): String? {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }


}