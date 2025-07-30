package com.example.project1

import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.project1.data.ScheduleDB
import com.example.project1.data.ScheduleEntity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AddScheduleBottomSheetFragment(
    private val date: String,
    private val onScheduleAdded: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var etContent: EditText
    private lateinit var tvTime: TextView
    private lateinit var colorContainer: LinearLayout
    private lateinit var btnCancel: TextView
    private lateinit var btnDone: TextView

    private var selectedHour = 9
    private var selectedMinute = 0
    private var selectedColorIndex = 0

    private val colorList = listOf("#FF4D4D", "#FF9933", "#FFD700", "#66CC66", "#3399FF", "#9966CC")
    private var selectedColor: String = colorList[0]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_schedule_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // View 참조
        etContent = view.findViewById(R.id.editTextContent2)
        tvTime = view.findViewById(R.id.timePickerView2)
        colorContainer = view.findViewById(R.id.colorButtonContainer2)
        btnCancel = view.findViewById(R.id.btnCancel2)
        btnDone = view.findViewById(R.id.btnDone2)

        // 초기 UI 설정
        updateTimeLabel()
        setupColorButtons()

        // 시간 선택 다이얼로그
        tvTime.setOnClickListener {
            TimePickerDialog(requireContext(), { _, hour, minute ->
                selectedHour = hour
                selectedMinute = minute
                updateTimeLabel()
            }, selectedHour, selectedMinute, false).show()
        }

        // 취소 버튼
        btnCancel.setOnClickListener {
            dismiss()
        }

        // 완료 버튼
        btnDone.setOnClickListener {
            val content = etContent.text.toString().trim()
            if (content.isEmpty()) {
                etContent.error = "일정명을 입력하세요"
                return@setOnClickListener
            }

            val time = "%02d:%02d".format(selectedHour, selectedMinute)
            val schedule = ScheduleEntity(
                date = date,
                content = content,
                time = time,
                color = selectedColor
            )

            CoroutineScope(Dispatchers.IO).launch {
                ScheduleDB.getDatabase(requireContext()).scheduleDao().insertSchedule(schedule)
                // UI 관련 작업은 메인 스레드에서 수행
                launch(Dispatchers.Main) {
                    onScheduleAdded()
                    dismiss()
                }
            }
        }
    }

    // AM/PM 시간 문자열 생성
    private fun updateTimeLabel() {
        val amPm = if (selectedHour < 12) "오전" else "오후"
        val displayHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
        tvTime.text = "$amPm $displayHour:${"%02d".format(selectedMinute)}"
    }

    // 색상 버튼 생성
    private fun setupColorButtons() {
        colorContainer.removeAllViews()

        colorList.forEachIndexed { index, color ->
            val size = resources.getDimensionPixelSize(R.dimen.color_circle_size)
            val margin = resources.getDimensionPixelSize(R.dimen.color_circle_margin)

            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(color))
                setStroke(4, if (index == selectedColorIndex) Color.WHITE else Color.TRANSPARENT)
            }

            val view = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, margin, margin, margin)
                }
                background = drawable
                setOnClickListener {
                    selectedColorIndex = index
                    selectedColor = color
                    setupColorButtons()
                }
            }

            colorContainer.addView(view)
        }
    }
}