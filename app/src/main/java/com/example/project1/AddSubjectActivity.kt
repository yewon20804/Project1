package com.example.project1

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.max

class AddSubjectActivity : AppCompatActivity() {

    private var selectedDay: Int = -1
    private var selectedColor: Int = 0xFFFF3B30.toInt()
    private lateinit var etSubjectName: EditText
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private var selectedStartHour = 9
    private var selectedStartMinute = 0
    private var selectedEndHour = 10
    private var selectedEndMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        etSubjectName = findViewById(R.id.etSubjectName)
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        val btnSave = findViewById<TextView>(R.id.btnSaveSubject)

        // 요일 버튼 연결
        val buttons = listOf<View>(
            findViewById(R.id.btnMon),
            findViewById(R.id.btnTue),
            findViewById(R.id.btnWed),
            findViewById(R.id.btnThu),
            findViewById(R.id.btnFri)
        )

        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {
                selectedDay = index
                buttons.forEach {
                    it.backgroundTintList = ColorStateList.valueOf(0xFF444444.toInt())
                }
                button.backgroundTintList = ColorStateList.valueOf(0xFF8888FF.toInt())
            }
        }

        // 시작 시간 선택
        tvStartTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedStartHour = hour
                selectedStartMinute = minute
                tvStartTime.text = String.format("%02d:%02d", hour, minute)
            }, selectedStartHour, selectedStartMinute, false).show()
        }

        // 종료 시간 선택
        tvEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedEndHour = hour
                selectedEndMinute = minute
                tvEndTime.text = String.format("%02d:%02d", hour, minute)
            }, selectedEndHour, selectedEndMinute, false).show()
        }

        // 색상 선택
        val colorViews = listOf(
            findViewById<View>(R.id.colorRed),
            findViewById<View>(R.id.colorOrange),
            findViewById<View>(R.id.colorYellow),
            findViewById<View>(R.id.colorGreen),
            findViewById<View>(R.id.colorSky),
            findViewById<View>(R.id.colorBlue),
            findViewById<View>(R.id.colorPurple)
        )
        val colorValues = listOf(
            0xFFFF3B30.toInt(), 0xFFFF922C.toInt(), 0xFFFFFF1D.toInt(),
            0xFF1CD282.toInt(), 0xFF20DEF0.toInt(), 0xFF415AFE.toInt(), 0xFFBD59FF.toInt()
        )
        colorViews.forEachIndexed { i, view ->
            view.setOnClickListener {
                selectedColor = colorValues[i]
                colorViews.forEachIndexed { j, v ->
                    v.backgroundTintList = ColorStateList.valueOf(colorValues[j])
                    v.background = null
                }
                view.background = resources.getDrawable(R.drawable.bg_color_circle_selected, null)
            }
        }

        // 저장 버튼
        btnSave.setOnClickListener {
            val name = etSubjectName.text.toString()
            if (name.isBlank() || selectedDay == -1) {
                Toast.makeText(this, "과목명과 요일을 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startTotalMin = selectedStartHour * 60 + selectedStartMinute
            val endTotalMin = selectedEndHour * 60 + selectedEndMinute
            val span = max(1, (endTotalMin - startTotalMin) / 60)

            val subject = Subject(
                name = name,
                day = selectedDay,
                color = selectedColor,
                startHour = selectedStartHour,
                startMinute = selectedStartMinute,
                endHour = selectedEndHour,
                endMinute = selectedEndMinute,
                span = span
            )

            val result = Intent().apply {
                putExtra("subject", subject)
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }

    }
}
