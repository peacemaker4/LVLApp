package com.bek.lvlapp.models

import com.squareup.moshi.Json

data class ApiModel(
    val id: String,
    @Json(name = "img_src") val imgSrcUrl: String
)