package com.example.project1

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var timetableWrapper: ConstraintLayout
    private val subjectList = mutableListOf<Subject>()

    private val addSubjectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val subject = getSubjectFromResult(result.data)
                val action = result.data?.getStringExtra("action")

                when (action) {
                    "edit" -> {
                        subject?.let {
                            subjectList.removeIf { s -> isSameSubject(s, it) }
                            subjectList.add(it)
                            drawAllSubjects()
                        }
                    }
                    "delete" -> {
                        subject?.let {
                            subjectList.removeIf { s -> isSameSubject(s, it) }
                            drawAllSubjects()
                        }
                    }
                    else -> {
                        subject?.let {
                            subjectList.add(it)
                            drawAllSubjects()
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timetableWrapper = findViewById(R.id.timetableWrapper)
        val btnAdd = findViewById<ImageView>(R.id.btnAdd)

        generateAllGuidelines()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddSubjectActivity::class.java)
            addSubjectLauncher.launch(intent)
        }

        // 네비게이션 바 _ 추가 했습니다 !
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_schedule

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_schedule -> {
                    // 현재 화면
                    true
                }
                R.id.nav_calendar -> {
//                    startActivity(Intent(this, CalendarActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_timer -> {
                    startActivity(Intent(this, TimerActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun generateAllGuidelines() {
        val columnCount = 5
        val rowCount = 36  // 15분 단위 × 9시간 = 36

        for (i in 0..columnCount + 1) {
            val guideline = View(this).apply {
                id = View.generateViewId()
                tag = "guideline_col_$i"
            }
            timetableWrapper.addView(guideline)
            val cs = ConstraintSet()
            cs.clone(timetableWrapper)
            val percent = i / (columnCount + 1).toFloat()
            cs.create(guideline.id, ConstraintSet.VERTICAL_GUIDELINE)
            cs.setGuidelinePercent(guideline.id, percent)
            cs.applyTo(timetableWrapper)
        }

        for (i in 0..rowCount) {
            val guideline = View(this).apply {
                id = View.generateViewId()
                tag = "guideline_row_$i"
            }
            timetableWrapper.addView(guideline)
            val cs = ConstraintSet()
            cs.clone(timetableWrapper)
            val percent = i / rowCount.toFloat()
            cs.create(guideline.id, ConstraintSet.HORIZONTAL_GUIDELINE)
            cs.setGuidelinePercent(guideline.id, percent)
            cs.applyTo(timetableWrapper)
        }
    }

    private fun drawAllSubjects() {
        for (view in timetableWrapper.children.toList()) {
            if (view.tag == "subjectCell") {
                timetableWrapper.removeView(view)
            }
        }

        for (subject in subjectList) {
            drawSubject(subject)
        }
    }

    private fun drawSubject(subject: Subject) {
        for (dayIndex in subject.days) {
            val cell = TextView(this).apply {
                id = View.generateViewId()
                text = subject.name
                setTextColor(Color.WHITE)
                textSize = 12f
                gravity = Gravity.CENTER
                tag = "subjectCell"
                setPadding(0, 10, 0, 10)

                val drawable = ContextCompat.getDrawable(context, R.drawable.bg_cell_rounded)?.mutate()
                drawable?.setTint(subject.color)
                background = drawable

                layoutParams = ConstraintLayout.LayoutParams(0, 0).apply {
                    topMargin = 12.dpToPx()
                    bottomMargin = 2.dpToPx()
                }

                setOnClickListener {
                    val intent = Intent(this@MainActivity, AddSubjectActivity::class.java).apply {
                        putExtra("subject", subject)
                    }
                    addSubjectLauncher.launch(intent)
                }
            }

            timetableWrapper.addView(cell)

            val cs = ConstraintSet()
            cs.clone(timetableWrapper)

            val startGuidelineId = getGuidelineIdForColumn(dayIndex + 1)
            val endGuidelineId = getGuidelineIdForColumn(dayIndex + 2)

            val startRowGuidelineId = getGuidelineIdForTime(subject.startHour, subject.startMinute)
            val endRowGuidelineId = getGuidelineIdForTime(subject.endHour, subject.endMinute)

            cs.connect(cell.id, ConstraintSet.START, startGuidelineId, ConstraintSet.START)
            cs.connect(cell.id, ConstraintSet.END, endGuidelineId, ConstraintSet.START)
            cs.connect(cell.id, ConstraintSet.TOP, startRowGuidelineId, ConstraintSet.TOP)
            cs.connect(cell.id, ConstraintSet.BOTTOM, endRowGuidelineId, ConstraintSet.BOTTOM)

            cs.applyTo(timetableWrapper)
        }
    }

    private fun getSubjectFromResult(data: Intent?): Subject? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data?.getSerializableExtra("subject", Subject::class.java)
        } else {
            @Suppress("DEPRECATION")
            data?.getSerializableExtra("subject") as? Subject
        }
    }

    private fun getGuidelineIdForColumn(index: Int): Int {
        return timetableWrapper.children.find {
            it.tag == "guideline_col_$index"
        }?.id ?: View.NO_ID
    }

    private fun getGuidelineIdForTime(hour: Int, minute: Int): Int {
        val rowIndex = ((hour - 9) * 60 + minute) / 15
        return timetableWrapper.children.find {
            it.tag == "guideline_row_$rowIndex"
        }?.id ?: View.NO_ID
    }

    private fun isSameSubject(s1: Subject, s2: Subject): Boolean {
        return s1.name == s2.name &&
                s1.startHour == s2.startHour &&
                s1.startMinute == s2.startMinute &&
                s1.endHour == s2.endHour &&
                s1.endMinute == s2.endMinute &&
                s1.days == s2.days
    }

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}
