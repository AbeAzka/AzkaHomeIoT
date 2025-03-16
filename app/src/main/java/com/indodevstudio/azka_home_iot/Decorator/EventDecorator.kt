package com.indodevstudio.azka_home_iot.Decorator

import android.content.Context
import android.graphics.drawable.GradientDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class EventDecorator(
    private val context: Context,
    private val date: CalendarDay,
    private val colors: List<Int>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, colors.toIntArray()
        )
        view.setBackgroundDrawable(gradientDrawable)
    }
}


/*class com.indodevstudio.azka_home_iot.Decorator.EventDecorator(private val color: Int, private val date: CalendarDay) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.setBackgroundDrawable(ColorDrawable(color))
    }
}*/
