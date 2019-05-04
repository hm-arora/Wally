package com.source.wally

data class Bucket(
    var name: String,
    var firstImageContainedPath: String? = null,
    var totalImages: Int = 0,
    var colorCode: Int? = null
)