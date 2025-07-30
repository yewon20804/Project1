package com.example.project1

import TextDecorator
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.example.project1.data.ScheduleDB
import com.example.project1.data.ScheduleEntity
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale


class CalendarActivity : AppCompatActivity() {

    private lateinit var selectedDate: String
    private lateinit var scheduleContainer: LinearLayout
    private lateinit var db: ScheduleDB
    private lateinit var calendarView: MaterialCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        //초기화
        calendarView = findViewById(R.id.calendarView)
        scheduleContainer = findViewById(R.id.scheduleContainer)
        db = ScheduleDB.getDatabase(this)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.nav_calendar

        //초기 날짜 설정
        calendarView.setSelectedDate(CalendarDay.today())
        val currentMonth = calendarView.currentDate.month
        calendarView.addDecorator(TextDecorator(this@CalendarActivity, currentMonth))

        //일정 있는 날 아이콘 추가
        decorateEventDates()

        //상단 화살표 색상 변경
        calendarView.post {
            val whiteColor = ContextCompat.getColor(this, R.color.gray00)  //색상 변경 시 여기 수정
            for (i in 0 until calendarView.childCount) {
                val child = calendarView.getChildAt(i)
                if (child is ViewGroup) {
                    for (j in 0 until child.childCount) {
                        val subChild = child.getChildAt(j)
                        if (subChild is ImageButton) {
                            subChild.setColorFilter(whiteColor)
                        }
                    }
                }
            }
        }

        //네비게이션 바 코드 Paste
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_schedule -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_calendar -> {
                    // 현재 화면이므로 이동 없음
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

        //날짜 클릭 시 일정 목록 출력
        calendarView.setOnDateChangedListener { _, date, selected ->
            val clickedDate = "${date.year}-${date.month + 1}-${date.day}"

            //선택된 날짜 재클릭 시 선택 해제
            if (::selectedDate.isInitialized && selectedDate == clickedDate) {
                selectedDate = ""
                calendarView.clearSelection()
                scheduleContainer.removeAllViews()
                calendarView.removeDecorators()
                decorateEventDates()
                val currentMonth = calendarView.currentDate.month
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, currentMonth))
            } else {  //다른 날짜 선택
                selectedDate = clickedDate
                renderScheduleSection()
                calendarView.removeDecorators()
                calendarView.addDecorator(SelectedDateDecorator(this, date))
                decorateEventDates()
                val currentMonth = calendarView.currentDate.month
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, currentMonth))
            }
        }

        //달 넘겼을 시 달력 디자인 초기화
        calendarView.setOnMonthChangedListener { widget, date ->
            calendarView.removeDecorators()
            calendarView.addDecorator(TextDecorator(this@CalendarActivity, date.month))
            decorateEventDates()

            //선택된 날짜가 있으면 다시 강조
            if (::selectedDate.isInitialized && selectedDate.isNotEmpty()) {
                val parts = selectedDate.split("-")
                val selected = CalendarDay.from(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                calendarView.addDecorator(SelectedDateDecorator(this@CalendarActivity, selected))
            }
        }
    }

    //선택한 날짜 일정 화면 출력 함수
    private fun renderScheduleSection() {
        scheduleContainer.removeAllViews()

        val date = selectedDate ?: return

        //일정 조회 (비동기)
        CoroutineScope(Dispatchers.IO).launch {
            //db 조회
            val schedules = db.scheduleDao().getSchedulesByDate(date)
            //시간 순으로 정렬
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sortedSchedules = schedules.sortedBy { format.parse(it.time) }


            withContext(Dispatchers.Main) {
                //일정 목록용 공통 배경 박스 생성
                val itemBackground = GradientDrawable().apply {
                    setColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray60))
                    cornerRadius = 12 * resources.displayMetrics.density  //곡률 설정
                }

                //일정 추가 버튼 배경 박스
                val addButtonBackground = GradientDrawable().apply {
                    setColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray60))
                    cornerRadius = 12 * resources.displayMetrics.density  //곡률 설정
                }

                //일정 카드 생성
                for (schedule in sortedSchedules) {
                    //일정 레이아웃
                    val itemLayout = LinearLayout(this@CalendarActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        background = itemBackground
                        setPadding(40, 24, 24, 24)
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            bottomMargin = 16  //카드 사이 간격
                        }
                    }

                    //색상 표시 박스
                    val colorSize = (20 * resources.displayMetrics.density).toInt()
                    val colorBoxBackground = GradientDrawable().apply {
                        setColor(Color.parseColor(schedule.color))
                        cornerRadius = 6 * resources.displayMetrics.density
                    }
                    val colorBox = View(this@CalendarActivity).apply {
                        layoutParams = LinearLayout.LayoutParams(colorSize, colorSize).apply {
                            setMargins(0, 8, 24, 0)
                        }
                        background = colorBoxBackground
                    }

                    //일정 내용, 시간 감싸는 레이아웃
                    val textLayout = LinearLayout(this@CalendarActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    //일정 내용 텍스트뷰
                    val contentText = TextView(this@CalendarActivity).apply {
                        text = schedule.content
                        textSize = 16f
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray00))
                    }

                    //시간 텍스트뷰
                    val timeText = TextView(this@CalendarActivity).apply {
                        text = schedule.time
                        textSize = 12f
                        setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray10))
                    }

                    //일정 내용, 시간 텍스트뷰 레이아웃에 추가
                    textLayout.addView(contentText)
                    textLayout.addView(timeText)

                    //일정 삭제 버튼
                    val deleteButton = Button(this@CalendarActivity).apply {
                        text = "X"
                        textSize = 12f
                        background = null
                        setPadding(24, 24, 24, 24)
                        setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray10))
                        setOnClickListener {
                            //버튼 클릭 시 DB에서 값 삭제 후 UI 갱신
                            CoroutineScope(Dispatchers.IO).launch {
                                db.scheduleDao().deleteSchedule(schedule)
                                withContext(Dispatchers.Main) {
                                    renderScheduleSection()
                                    decorateEventDates()
                                }
                            }
                        }
                    }

                    //전체 요소들 레이아웃에 추가
                    itemLayout.addView(colorBox)
                    itemLayout.addView(textLayout)
                    itemLayout.addView(deleteButton)
                    //일정 목록에 다시 레이아웃 추가
                    scheduleContainer.addView(itemLayout)
                }

                //일정 추가 버튼
                val addButton = Button(this@CalendarActivity).apply {
                    text = "+ 일정 추가"
                    textSize = 16f
                    background = addButtonBackground
                    setTextColor(ContextCompat.getColor(context, R.color.gray10))
                    setPadding(0, 24, 0, 24)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = 24
                    }
                    //버튼 클릭 시 모달 표시
                    setOnClickListener {
                        selectedDate?.let { date ->
                            showAddScheduleBottomSheet(date)
                        }
                    }
                }
                //목록 아래 추가
                scheduleContainer.addView(addButton)
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, calendarView.currentDate.month))
            }
        }
    }

    private fun showAddScheduleBottomSheet(selectedDate: String) {
        val bottomSheet = AddScheduleBottomSheetFragment(
            date = selectedDate,
            onScheduleAdded = {
                loadSchedulesForDate(selectedDate)
            }
        )
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

    private fun loadSchedulesForDate(date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val scheduleList = db.scheduleDao().getSchedulesByDate(date)
            withContext(Dispatchers.Main) {
                updateScheduleUI(scheduleList)
            }
        }
    }

    private fun updateScheduleUI(scheduleList: List<ScheduleEntity>) {
        scheduleContainer.removeAllViews()

        val sortedList = scheduleList.sortedBy { it.time }

        for (schedule in sortedList) {
            val cardView = createScheduleCard(schedule)
            scheduleContainer.addView(cardView)
        }
        addAddButton()
    }

    private fun createScheduleCard(schedule: ScheduleEntity): View {
        val itemLayout = LinearLayout(this@CalendarActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            val background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray60))
                cornerRadius = 12 * resources.displayMetrics.density
            }
            this.background = background
            setPadding(40, 24, 24, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 16
            }
        }

        val colorSize = (20 * resources.displayMetrics.density).toInt()
        val colorBox = View(this@CalendarActivity).apply {
            layoutParams = LinearLayout.LayoutParams(colorSize, colorSize).apply {
                setMargins(0, 8, 24, 0)
            }
            background = GradientDrawable().apply {
                setColor(Color.parseColor(schedule.color))
                cornerRadius = 6 * resources.displayMetrics.density
            }
        }

        val textLayout = LinearLayout(this@CalendarActivity).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val contentText = TextView(this@CalendarActivity).apply {
            text = schedule.content
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray00))
        }

        val timeText = TextView(this@CalendarActivity).apply {
            text = schedule.time
            textSize = 12f
            setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray10))
        }

        textLayout.addView(contentText)
        textLayout.addView(timeText)

        val deleteButton = Button(this@CalendarActivity).apply {
            text = "X"
            textSize = 12f
            background = null
            setPadding(24, 24, 24, 24)
            setTextColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray10))
            setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    db.scheduleDao().deleteSchedule(schedule)
                    withContext(Dispatchers.Main) {
                        renderScheduleSection()
                        decorateEventDates()
                    }
                }
            }
        }

        itemLayout.addView(colorBox)
        itemLayout.addView(textLayout)
        itemLayout.addView(deleteButton)

        return itemLayout
    }

    private fun addAddButton() {
        val addButton = Button(this@CalendarActivity).apply {
            text = "+ 일정 추가"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, R.color.gray10))
            background = GradientDrawable().apply {
                setColor(ContextCompat.getColor(this@CalendarActivity, R.color.gray60))
                cornerRadius = 12 * resources.displayMetrics.density
            }
            setPadding(0, 24, 0, 24)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 24
            }
            setOnClickListener {
                selectedDate?.let { date ->
                    showAddScheduleBottomSheet(date)
                }
            }
        }
        scheduleContainer.addView(addButton)
    }


    //일정이 있는 날짜에 아이콘 표시 함수
    private fun decorateEventDates() {
        CoroutineScope(Dispatchers.IO).launch {
            //DB 데이터 조회
            val allSchedules = db.scheduleDao().getAllSchedules()
            //일정이 있는 날짜들만 필터링
            val eventDates = allSchedules
                .map { it.date }
                .distinct()
                .mapNotNull { dateStr ->
                    val parts = dateStr.split("-")
                    if (parts.size == 3) {
                        CalendarDay.from(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    } else null
                }.toHashSet()  //데코레이터로 넘기기 전 형식 변환

            withContext(Dispatchers.Main) {
                calendarView.removeDecorators()
                //일정 있는 날짜에 퍼즐 아이콘 표시
                calendarView.addDecorator(EventDecorator(this@CalendarActivity, eventDates))

                //선택된 날짜에 테두리 표시
                if (::selectedDate.isInitialized && selectedDate.isNotEmpty()) {
                    val parts = selectedDate.split("-")
                    val selected = CalendarDay.from(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    calendarView.addDecorator(SelectedDateDecorator(this@CalendarActivity, selected))
                }
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, calendarView.currentDate.month))
            }
        }
    }
}