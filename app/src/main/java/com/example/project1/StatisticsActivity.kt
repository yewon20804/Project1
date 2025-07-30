package com.example.project1

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

// 통계화면 액티비티
class StatisticsActivity : AppCompatActivity() {

    private lateinit var statRecyclerView: RecyclerView
    private lateinit var dbHelper: SubjectStatDBHelper
    private lateinit var statAdapter: StatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        dbHelper = SubjectStatDBHelper(this)

        statRecyclerView = findViewById(R.id.statRecyclerView)
        statRecyclerView.layoutManager = LinearLayoutManager(this)

        val statList = loadStatData()
        statAdapter = StatAdapter(statList)
        statRecyclerView.adapter = statAdapter

        // 네비게이션 바
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_stats

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_schedule -> {
//                    startActivity(Intent(this, ScheduleActivity::class.java))
                    overridePendingTransition(0, 0)
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
                    // 현재 화면
                    true
                }
                else -> false
            }
        }
    }

    private fun loadStatData(): List<SubjectStat> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM subject_stats", null)

        val list = mutableListOf<SubjectStat>()

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val color = cursor.getInt(cursor.getColumnIndexOrThrow("color"))
            val time = cursor.getLong(cursor.getColumnIndexOrThrow("study_time"))
            val puzzle = cursor.getInt(cursor.getColumnIndexOrThrow("puzzle_count"))

            list.add(SubjectStat(name, color, time, puzzle))
        }
        cursor.close()
        return list
    }
}