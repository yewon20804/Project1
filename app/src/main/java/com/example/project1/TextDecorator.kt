import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.example.project1.R
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.CalendarDay

class TextDecorator(context: Context, private val currentMonth: Int) : DayViewDecorator {
    private val color = ContextCompat.getColor(context, R.color.gray00)  //색상 설정

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return day?.month == currentMonth
    }

    override fun decorate(view: DayViewFacade?) {
        view?.addSpan(ForegroundColorSpan(color))
    }
}