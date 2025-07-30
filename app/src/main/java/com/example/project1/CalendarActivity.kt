package com.example.project1

import TextDecorator
import android.content.DialogInterface
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.example.project1.data.ScheduleDB
import com.example.project1.data.ScheduleEntity
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
                Toast.makeText(this, "선택된 날짜: $selectedDate", Toast.LENGTH_SHORT).show()
                calendarView.removeDecorators()
                calendarView.addDecorator(SelectedDateDecorator(this, date))
                decorateEventDates()
                val currentMonth = calendarView.currentDate.month
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, currentMonth))
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
                            showAddScheduleDialog(date)
                        }
                    }
                }
                //목록 아래 추가
                scheduleContainer.addView(addButton)
                calendarView.addDecorator(TextDecorator(this@CalendarActivity, calendarView.currentDate.month))
            }
        }
    }


    //모달 구성, 표시 함수
    private fun showAddScheduleDialog(date: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_schedule, null)

        val contentInput = dialogView.findViewById<EditText>(R.id.editTextContent)
        val hourEditText = dialogView.findViewById<EditText>(R.id.hourEditText)
        val minuteEditText = dialogView.findViewById<EditText>(R.id.minuteEditText)
        val colorPicker = dialogView.findViewById<Spinner>(R.id.colorPicker)

        val colorList = listOf("#FF4D4D", "#FF9933", "#FFD700", "#66CC66", "#3399FF", "#9966CC")
        val adapter = ColorSpinnerAdapter(colorList)
        colorPicker.adapter = adapter

        //모달 창 생성
        val dialog = AlertDialog.Builder(this)
            .setTitle("$date 일정 추가")
            .setView(dialogView)
            .setNegativeButton("취소", null)
            .create()

        //모달 창 팝업 시
        dialog.setOnShowListener {
            //일정 추가 버튼
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val content = contentInput.text.toString().trim()
                val hourText = hourEditText.text.toString()
                val minuteText = minuteEditText.text.toString()
                val colorIndex = colorPicker.selectedItemPosition

                //입력값 확인
                var isValid = true
                //일정 내용 확인
                if (content.isEmpty()) {
                    contentInput.error = "일정 내용을 입력하세요"
                    isValid = false
                }

                //시간 입력 확인 (미입력 + 정상 범위 체크)
                val hour = hourText.toIntOrNull()
                if (hourText.isEmpty()) {
                    hourEditText.error = "시를 입력하세요"
                    isValid = false
                } else if (hour == null || hour !in 0..23) {
                    hourEditText.error = "0~23 사이의 값을 입력하세요"
                    isValid = false
                }

                //분 입력 확인 (미입력 + 정상 범위 체크)
                val minute = minuteText.toIntOrNull()
                if (minuteText.isEmpty()) {
                    minuteEditText.error = "분을 입력하세요"
                    isValid = false
                } else if (minute == null || minute !in 0..59) {
                    minuteEditText.error = "0~59 사이의 값을 입력하세요"
                    isValid = false
                }

                //제대로 입력 받지 않았을 경우
                if (!isValid) return@setOnClickListener

                //일정 생성 시 필요한 변수 생성
                val time = hourText.padStart(2, '0') + ":" + minuteText.padStart(2, '0')
                val color = colorList[colorIndex]

                val schedule = ScheduleEntity(
                    date = date,
                    content = content,
                    time = time,
                    color = color
                )

                //DB 저장, UI 갱신
                lifecycleScope.launch {
                    db.scheduleDao().insertSchedule(schedule)
                    Toast.makeText(this@CalendarActivity, "일정이 저장되었습니다", Toast.LENGTH_SHORT).show()
                    renderScheduleSection()
                    decorateEventDates()
                }
                dialog.dismiss()
            }
        }
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "추가", null as DialogInterface.OnClickListener?)
        dialog.show()
    }


    //색상 선택 박스 (수정 예정)
    inner class ColorSpinnerAdapter(
        private val colors: List<String>
    ) : ArrayAdapter<String>(this@CalendarActivity, android.R.layout.simple_spinner_item, colors) {

        override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            return createColorBox(position)
        }

        override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
            return createColorBox(position)
        }

        private fun createColorBox(position: Int): View {
            val box = View(context)
            val scale = context.resources.displayMetrics.density
            val width = (120 * scale + 0.5f).toInt()
            val height = (40 * scale + 0.5f).toInt()
            val params = LinearLayout.LayoutParams(width, height)
            params.setMargins(16, 16, 16, 16)
            box.layoutParams = params
            box.setBackgroundColor(android.graphics.Color.parseColor(colors[position]))
            return box
        }
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