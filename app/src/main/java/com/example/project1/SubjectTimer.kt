package com.example.project1

// 과목 색상 시간을 담은 클래스
data class SubjectTimer(
    val name: String,
    val color: Int,
    var timeInSeconds: Int = 0,
    var totalTime: Int = 0
)