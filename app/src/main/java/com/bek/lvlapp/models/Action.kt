package com.bek.lvlapp.models

data class Action (val name: String ?= null,val description: String ?= null, val icon: String ?= null, val color: Int ?= null, var xp_give: Int ?= 0, var uid: String ?= null, var skill_uid: String ?= null, var pos: Int ?= null, var created_at: String ?= null, var updated_at: String ?= null) {

}