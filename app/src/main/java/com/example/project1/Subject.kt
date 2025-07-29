package com.example.project1

import java.io.Serializable

data class Subject(
    val name: String,
    val days: List<Int>,
    val color: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
) : Serializable


