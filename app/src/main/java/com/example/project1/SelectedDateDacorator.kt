package com.example.project1

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay
import androidx.core.content.ContextCompat

class SelectedDateDecorator(
    private val context: Context,
    private var selectedDate: CalendarDay?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == selectedDate
    }

    override fun decorate(view: DayViewFacade) {
        view.setSelectionDrawable(ColorDrawable(Color.TRANSPARENT))

        val drawable = ContextCompat.getDrawable(context, R.drawable.selected_day)
        view.setBackgroundDrawable(drawable!!)
    }

    fun setDate(date: CalendarDay?) {
        selectedDate = date
    }
}