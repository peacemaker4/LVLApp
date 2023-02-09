package com.bek.lvlapp.models

data class User(val uid: String ?= null, val username: String ?= null, val email: String ?= null, var created_at: String ?= null, var updated_at: String ?= null) {

}