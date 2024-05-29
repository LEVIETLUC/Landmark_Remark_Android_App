package com.example.landmarkremark.Enity

import java.io.Serializable

class Notes : Serializable {
    private var id: String? = null
    private var createdAt: String? = null
    private var userId: String? = null
    private var note: String? = null
    private var lat: Double? = null
    private var long: Double? = null

    constructor() {}

    constructor(id: String, createdAt: String, userId: String, note: String, lat: Double, long: Double) {
        this.id = id
        this.createdAt = createdAt
        this.userId = userId
        this.note = note
        this.lat = lat
        this.long = long
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getCreatedAt(): String? {
        return createdAt
    }

    fun setCreatedAt(createdAt: String) {
        this.createdAt = createdAt
    }

    fun getUserId(): String? {
        return userId
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun getNote(): String? {
        return note
    }

    fun setNote(note: String) {
        this.note = note
    }

    fun getLat(): Double? {
        return lat
    }

    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun getLong(): Double? {
        return long
    }

    fun setLong(long: Double) {
        this.long = long
    }


}