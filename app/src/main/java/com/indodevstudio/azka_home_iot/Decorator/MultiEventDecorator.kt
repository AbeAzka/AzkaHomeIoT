package com.indodevstudio.azka_home_iot.Decorator

import android.graphics.drawable.GradientDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class MultiEventDecorator(
    private val dates: Set<CalendarDay>,
    private val colors: List<Int>
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade?) {
        val drawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors.toIntArray())
        drawable.cornerRadius = 50f // Biar lebih bulat
        view?.setBackgroundDrawable(drawable)
    }
}
