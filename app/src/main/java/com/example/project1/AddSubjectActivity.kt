package com.example.project1

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class AddSubjectActivity : AppCompatActivity() {

    private val selectedDays = mutableListOf<Int>()
    private lateinit var etSubjectName: EditText
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var allDayButtons: List<TextView>
    private lateinit var colorViews: List<View>
    private lateinit var btnDelete: LinearLayout

    private var selectedStartHour = 9
    private var selectedStartMinute = 0
    private var selectedEndHour = 10
    private var selectedEndMinute = 0

    private var selectedColorIndex = 0
    private var selectedColor: Int = 0xFFFF3B30.toInt()

    private var editingSubject: Subject? = null

    private val colorValues = listOf(
        0xFFFF3B30.toInt(), 0xFFFF922C.toInt(), 0xFFFFFF1D.toInt(),
        0xFF1CD282.toInt(), 0xFF20DEF0.toInt(), 0xFF415AFE.toInt(), 0xFFBD59FF.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subject)

        etSubjectName = findViewById(R.id.etSubjectName)
        tvStartTime = findViewById(R.id.tvStartTime)
        tvEndTime = findViewById(R.id.tvEndTime)
        btnDelete = findViewById(R.id.btnDeleteSubject)

        val btnSave = findViewById<TextView>(R.id.btnSaveSubject)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        allDayButtons = listOf(
            findViewById(R.id.btnMon),
            findViewById(R.id.btnTue),
            findViewById(R.id.btnWed),
            findViewById(R.id.btnThu),
            findViewById(R.id.btnFri)
        )

        colorViews = listOf(
            findViewById(R.id.colorRed),
            findViewById(R.id.colorOrange),
            findViewById(R.id.colorYellow),
            findViewById(R.id.colorGreen),
            findViewById(R.id.colorSky),
            findViewById(R.id.colorBlue),
            findViewById(R.id.colorPurple)
        )

        // 요일 버튼 클릭 리스너
        allDayButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (selectedDays.contains(index)) {
                    selectedDays.remove(index)
                    button.backgroundTintList = ColorStateList.valueOf(0xFF444444.toInt())
                } else {
                    selectedDays.add(index)
                    button.backgroundTintList = ColorStateList.valueOf(0xFFB4BDB6.toInt())
                }
            }
        }

        // 색상 선택 리스너
        colorViews.forEachIndexed { i, view ->
            view.setOnClickListener {
                selectedColorIndex = i
                selectedColor = colorValues[i]
                updateColorUI()
            }
        }
        updateColorUI()

        // 시간 선택
        tvStartTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedStartHour = hour
                selectedStartMinute = minute
                tvStartTime.text = formatTime(hour, minute)
            }, selectedStartHour, selectedStartMinute, false).show()
        }

        tvEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hour, minute ->
                selectedEndHour = hour
                selectedEndMinute = minute
                tvEndTime.text = formatTime(hour, minute)
            }, selectedEndHour, selectedEndMinute, false).show()
        }

        // 저장
        btnSave.setOnClickListener {
            val name = etSubjectName.text.toString().trim()
            if (name.isEmpty() || selectedDays.isEmpty()) {
                Toast.makeText(this, "과목명과 요일을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val subject = Subject(
                name = name,
                days = selectedDays.toList(),
                color = selectedColor,
                startHour = selectedStartHour,
                startMinute = selectedStartMinute,
                endHour = selectedEndHour,
                endMinute = selectedEndMinute
            )

            val result = Intent().apply {
                putExtra("subject", subject)
                if (editingSubject != null) putExtra("action", "edit")
            }

            setResult(Activity.RESULT_OK, result)
            finish()
        }

        // 삭제
        btnDelete.setOnClickListener {
            val result = Intent().apply {
                putExtra("subject", editingSubject)
                putExtra("action", "delete")
            }
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        // 뒤로가기
        btnBack.setOnClickListener { finish() }

        // 수정모드일 경우 초기값 세팅
        editingSubject = intent.getSerializableExtra("subject") as? Subject
        editingSubject?.let { subject ->
            etSubjectName.setText(subject.name)
            selectedColor = subject.color
            selectedColorIndex = colorValues.indexOf(subject.color).takeIf { it >= 0 } ?: 0
            selectedStartHour = subject.startHour
            selectedStartMinute = subject.startMinute
            selectedEndHour = subject.endHour
            selectedEndMinute = subject.endMinute
            selectedDays.clear()
            selectedDays.addAll(subject.days)

            tvStartTime.text = formatTime(selectedStartHour, selectedStartMinute)
            tvEndTime.text = formatTime(selectedEndHour, selectedEndMinute)

            // 요일 선택 상태 반영
            allDayButtons.forEachIndexed { index, button ->
                if (selectedDays.contains(index)) {
                    button.backgroundTintList = ColorStateList.valueOf(0xFFB4BDB6.toInt())
                } else {
                    button.backgroundTintList = ColorStateList.valueOf(0xFF444444.toInt())
                }
            }

            updateColorUI()
            btnDelete.visibility = View.VISIBLE
        }
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "오전" else "오후"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        return "$amPm ${String.format("%d:%02d", displayHour, minute)}"
    }

    private fun updateColorUI() {
        colorViews.forEachIndexed { j, colorView ->
            val drawable = ContextCompat.getDrawable(this, R.drawable.bg_color_circle)?.mutate() as GradientDrawable
            drawable.setColor(colorValues[j])
            if (j == selectedColorIndex) {
                drawable.setStroke(4.dpToPx(), Color.WHITE)
            } else {
                drawable.setStroke(0, Color.TRANSPARENT)
            }
            colorView.background = drawable
        }
    }

    private fun Int.dpToPx(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
