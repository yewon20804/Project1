import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.style.LineBackgroundSpan
import com.example.project1.R

class EventIconSpan(context: Context) : LineBackgroundSpan {

    private val icon: Drawable = context.getDrawable(R.drawable.main)!!
    private val iconSizePx = 90 //아이콘 크기

    override fun drawBackground(
        canvas: Canvas,
        paint: android.graphics.Paint,
        left: Int,
        right: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        lineNumber: Int
    ) {
        val centerX = (left + right) / 2 - iconSizePx / 2  //X축 조정
        val centerY = (top + bottom) / 2 - iconSizePx / 2 + 6  //Y축 조정

        icon.setBounds(
            centerX.toInt(),
            centerY.toInt(),
            centerX.toInt() + iconSizePx,
            centerY.toInt() + iconSizePx
        )
        icon.draw(canvas)
    }
}