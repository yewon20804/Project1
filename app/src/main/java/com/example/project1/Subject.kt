package com.example.project1

import java.io.Serializable

data class Subject(
    val name: String,
    val day: Int,
    val color: Int,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val span: Int
) : Serializable


