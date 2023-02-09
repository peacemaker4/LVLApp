package com.bek.lvlapp.models

data class Todo (val task: String ?= null, val check: Boolean ?= null, var archived: Boolean ?= null, var uid: String ?= null, var pos: Int ?= null, var created_at: String ?= null, var updated_at: String ?= null) {

}