package com.bek.lvlapp.models

data class Skill (var name: String ?= null, var icon: String ?= null, var color: Int ?= null, val level: Int ?= 1, val xp: Int ?= 0, var uid: String ?= null, var pos: Int ?= null, var created_at: String ?= null, var updated_at: String ?= null) {

}