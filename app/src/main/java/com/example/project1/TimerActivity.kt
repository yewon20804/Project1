package com.example.project1

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

// 과목 리스트 보이는 클래스
class TimerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TimerAdapter
    private val subjectList = mutableListOf<SubjectTimer>()
    private lateinit var dbHelper: SubjectStatDBHelper

    companion object {
        private const val REQUEST_CODE_TIMER = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timer_main)

        dbHelper = SubjectStatDBHelper(this)

        subjectList.clear()
        val storedSubjects = dbHelper.getAllStats()

        // SubjectStat → SubjectTimer로 변환
        subjectList.addAll(storedSubjects.map { stat ->
            SubjectTimer(stat.name, stat.color)
        })

        recyclerView = findViewById(R.id.timerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 더미 데이터
//        subjectList.add(SubjectTimer("모바일앱프로그래밍", Color.parseColor("#3DDC84")))
//        subjectList.add(SubjectTimer("자료구조", Color.parseColor("#F47C2F")))
//        subjectList.add(SubjectTimer("2D콘텐츠프로그래밍", Color.parseColor("#FFFF1D")))
//        subjectList.add(SubjectTimer("GUI디자인 심화", Color.parseColor("#415AFE")))

        // 클릭 시 타이머 액티비티 실행
        adapter = TimerAdapter(subjectList) { subject ->
            val intent = Intent(this, TimerDetailActivity::class.java)
            intent.putExtra("subjectName", subject.name)
            intent.putExtra("subjectColor", subject.color)
            startActivityForResult(intent, REQUEST_CODE_TIMER)
        }

        // 통계 버튼
//        val statButton = findViewById<Button>(R.id.statisticsButton)
//        statButton.setOnClickListener {
//            val intent = Intent(this, StatisticsActivity::class.java)
//            startActivity(intent)
//        }

        // 과목 추가 버튼 클릭 연결
        val addBtn = findViewById<FloatingActionButton>(R.id.addSubjectButton)
        addBtn.setOnClickListener {
            showAddSubjectBottomSheet()
        }

        // 네비게이션 바
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_timer
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_schedule -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_timer -> {
                    // 현재 화면이 Timer면 아무 것도 하지 않음
                    true
                }
                R.id.nav_stats -> {
                    startActivity(Intent(this, StatisticsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        recyclerView.adapter = adapter
    }

    // 타이머 종료 후 결과 받아서 시간 누적 함수
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_TIMER && resultCode == RESULT_OK && data != null) {
            val name = data.getStringExtra("subjectName") ?: return
            val addedTime = data.getIntExtra("studyTime", 0)

            subjectList.find { it.name == name }?.let {
                it.timeInSeconds += addedTime
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    // 과목 추가 함수
    private fun showAddSubjectBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_subject, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)

        val editName = view.findViewById<EditText>(R.id.editSubjectName)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnConfirm = view.findViewById<TextView>(R.id.btnConfirm)
        val colorContainer = view.findViewById<LinearLayout>(R.id.colorContainer)

        // 색상 목록 정의
        val colors = listOf(
            Color.parseColor("#F24C4F"), Color.parseColor("#FF922C"),
            Color.parseColor("#FFFF1D"), Color.parseColor("#1CD282"),
            Color.parseColor("#20DEF0"), Color.parseColor("#415AFE"),
            Color.parseColor("#BD59FF")
        )

        var selectedColor = colors[0]

        // 색상 버튼 추가
        colors.forEach { color ->
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(70, 70).apply {
                    setMargins(12, 0, 17, 0)
                }
                background = ContextCompat.getDrawable(this@TimerActivity, R.drawable.bg_color_circle)
                background.setTint(color)
                setOnClickListener {
                    selectedColor = color
                }
            }
            colorContainer.addView(circle)
        }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnConfirm.setOnClickListener {
            val name = editName.text.toString().trim()
            if (name.isNotBlank()) {
                dbHelper.upsertStat(name, selectedColor, 0, 0)
                subjectList.add(SubjectTimer(name, selectedColor))
                adapter.notifyItemInserted(subjectList.size - 1)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}