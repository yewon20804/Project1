package com.example.project1

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

// 타이버 작동 액티비티
class TimerDetailActivity : AppCompatActivity() {

    private lateinit var subjectName: String
    private lateinit var subjectNameText: TextView
    private lateinit var timerText: TextView
    private lateinit var puzzleImage: ImageView
    private lateinit var playButton: ImageButton
    private lateinit var pauseButton: ImageButton

    private var isRunning = false
    private var timeLeftInMillis: Long = 5 * 1000L // 25분으로 변경해야함 현재는 5초
    private var timer: CountDownTimer? = null

    private var elapsedTime: Long = 0L
    private var subjectColor: Int = 0xFF888888.toInt()

    // 퍼즐 및 누적 시간 저장용 맵
    companion object {
        val puzzleCountMap = mutableMapOf<String, Int>()
        val studyTimeMap = mutableMapOf<String, Long>()
    }

    private var startTimeMillis: Long = 0L
    private var isCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer_detail)

        // View 연결
        subjectNameText = findViewById(R.id.subjectNameText)
        timerText = findViewById(R.id.timerText)
        puzzleImage = findViewById(R.id.puzzleImage)
        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // 과목 이름과 색상 받기
        subjectName = intent.getStringExtra("subjectName") ?: "과목 없음"
        subjectColor = intent.getIntExtra("subjectColor", 0xFF888888.toInt())

        // 표시
        subjectNameText.text = subjectName
        puzzleImage.setColorFilter(subjectColor, PorterDuff.Mode.SRC_IN)

        // 초기 타이머 텍스트 표시
        updateTimerText()

        // 재생 버튼
        playButton.setOnClickListener {
            if (!isRunning) startTimer()
        }

        // 일시정지 버튼
        pauseButton.setOnClickListener {
            if (isRunning) pauseTimer()
        }

        // 뒤로가기 버튼
        backButton.setOnClickListener {
            if (isRunning) {
                timer?.cancel()
                isRunning = false

                if (startTimeMillis > 0L) {
                    saveElapsedTime()
                    Log.d("시간확인", "중간 종료 - 누적 시간: ${(studyTimeMap[subjectName] ?: 0L) / 1000}초")

                    val elapsedSeconds = elapsedTime / 1000L

                    // 데베에 저장 (퍼즐 X)
                    val dbHelper = SubjectStatDBHelper(this@TimerDetailActivity)
//                    dbHelper.upsertStat(subjectName, subjectColor, elapsedTime, 0)
                    dbHelper.upsertStat(subjectName, subjectColor, elapsedSeconds, 0)
                }
            }
            finish()
        }
    }

    private fun startTimer() {
        startTimeMillis = System.currentTimeMillis()

        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerText()
            }

            override fun onFinish() {
                isRunning = false
                isCompleted = true // 타이머 완료 표시

                saveElapsedTime()       // 누적 시간 저장
                increasePuzzleCount()   // 퍼즐 조각 +1

                // 데베에 저장
                val dbHelper = SubjectStatDBHelper(this@TimerDetailActivity)

                val elapsedSeconds = elapsedTime / 1000L

//                dbHelper.upsertStat(subjectName, subjectColor, elapsedTime, 1)
                dbHelper.upsertStat(subjectName, subjectColor, elapsedSeconds, 1)

                val resultIntent = Intent()
                resultIntent.putExtra("subjectName", subjectName)
                resultIntent.putExtra("studyTime", 5 ) // 25분으로 변경해야함 현재는 5초

                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }.start()
        isRunning = true
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
    }

    private fun updateTimerText() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerText.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun saveElapsedTime() {
        elapsedTime = System.currentTimeMillis() - startTimeMillis
        val current = studyTimeMap[subjectName] ?: 0L
        studyTimeMap[subjectName] = current + elapsedTime
    }

    private fun increasePuzzleCount() {
        val current = puzzleCountMap[subjectName] ?: 0
        puzzleCountMap[subjectName] = current + 1
        // 디버깅
        Log.d("퍼즐확인", "과목: $subjectName, 퍼즐 개수: ${puzzleCountMap[subjectName] ?: 0}")
    }
}