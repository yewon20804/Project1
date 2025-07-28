package com.example.project1

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.project1.AddSubjectActivity
import com.example.project1.R
import com.example.project1.Subject



class MainActivity : AppCompatActivity() {

    private lateinit var timetableGrid: GridLayout
    private val subjectList = mutableListOf<Subject>()

    private val addSubjectLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val subject = getSubjectFromResult(result.data)
                subject?.let {
                    subjectList.add(it)
                    addSubjectToGrid(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timetableGrid = findViewById(R.id.timetableGrid)

        val btnAdd = findViewById<ImageView>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddSubjectActivity::class.java)
            addSubjectLauncher.launch(intent)
        }
    }

    private fun getSubjectFromResult(data: Intent?): Subject? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            data?.getSerializableExtra("subject", Subject::class.java)
        } else {
            @Suppress("DEPRECATION")
            data?.getSerializableExtra("subject") as? Subject
        }
    }


    private fun addSubjectToGrid(subject: Subject) {
        val inflater = LayoutInflater.from(this)
        val cell = inflater.inflate(R.layout.item_timetable_cell, timetableGrid, false) as TextView

        cell.text = subject.name
        cell.setBackgroundColor(subject.color)

        val startTotalMin = subject.startHour * 60 + subject.startMinute
        val endTotalMin = subject.endHour * 60 + subject.endMinute

        val column = subject.day
        val row = (startTotalMin - 9 * 60) / 15
        val rowSpan = (endTotalMin - startTotalMin) / 15

        val params = GridLayout.LayoutParams(
            GridLayout.spec(row, rowSpan, 1f),
            GridLayout.spec(column + 1, 1f)  // +1: 왼쪽 시간 라벨 열 제외
        ).apply {
            width = 0
            height = 0
            setGravity(Gravity.FILL)
        }

        cell.layoutParams = params
        timetableGrid.addView(cell)
    }


}
