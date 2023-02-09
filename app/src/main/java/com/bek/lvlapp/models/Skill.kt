package com.bek.lvlapp.models

data class Skill (val name: String ?= null, val icon: String ?= null, val color: Int ?= null, val level: Int ?= 1, val xp: Int ?= 0, var uid: String ?= null, var pos: Int ?= null, var created_at: String ?= null, var updated_at: String ?= null) {

}