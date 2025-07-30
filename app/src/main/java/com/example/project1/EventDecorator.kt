package com.example.project1
import EventIconSpan
import android.content.Context
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay

class EventDecorator(
    private val context: Context,
    private val dates: HashSet<CalendarDay>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(EventIconSpan(context))
    }
}