package com.bek.lvlapp.models

import java.time.LocalDateTime

data class Quote(val author: String ?= null, val quote: String ?= null, var last_updated: String ?= null) {

}