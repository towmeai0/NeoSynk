package com.ayudevices.neosynkparent.data.model

data class CategoryData(
    val percentage: Double,
    val completed: List<String>,
    val pending: List<String>
)