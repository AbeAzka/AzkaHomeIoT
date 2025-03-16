package com.indodevstudio.azka_home_iot.Decorator

import android.content.Context
import android.graphics.drawable.ColorDrawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class SingleEventDecorator(
    private val context: Context,
    private val date: CalendarDay,
    private val color: Int
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day == date
    }

    override fun decorate(view: DayViewFacade) {
        val drawable = ColorDrawable(color)
        view.setBackgroundDrawable(drawable)
    }
}
