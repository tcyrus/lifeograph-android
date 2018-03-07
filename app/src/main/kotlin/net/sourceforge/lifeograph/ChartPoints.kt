/***********************************************************************************
 *
 * Copyright (C) 2012-2016 Ahmet Öztürk (aoz_2@yahoo.com)
 *
 * This file is part of Lifeograph.
 *
 * Lifeograph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lifeograph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lifeograph.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 *
 */


package net.sourceforge.lifeograph


class ChartPoints @JvmOverloads internal constructor(internal var type: Int = MONTHLY or CUMULATIVE) {

    internal val span: Int
        get() = values.size

    internal var value_min: Double? = Double.POSITIVE_INFINITY//java.lang.Double.POSITIVE_INFINITY
    internal var value_max: Double? = Double.NEGATIVE_INFINITY//java.lang.Double.NEGATIVE_INFINITY

    internal var values = java.util.LinkedList<Double>()
    internal var start_date: Long = 0

    internal var chapters: List<Pair<Double, Int>>? = null
    internal var unit = ""

    internal fun calculate_distance(d1: Date, d2: Date): Int {
        when (type and PERIOD_MASK) {
            MONTHLY -> return d1.calculate_months_between(d2.m_date)
            YEARLY -> return Math.abs(d1._year - d2._year)
        }

        return 0 // just to silence the compiler warning
    }

    internal fun calculate_distance_neg(d1: Date, d2: Date): Int {
        when (type and PERIOD_MASK) {
            MONTHLY -> return Date.calculate_months_between_neg(d1.m_date, d2.m_date)
            YEARLY -> return d2._year - d1._year
        }

        return 0 // just to silence the compiler warning
    }

    internal fun push_back(v: Double?) {
        if (v != null) {
            values.addLast(v)
            if (v < value_min!!)
                value_min = v
            if (v > value_max!!)
                value_max = v
        }
    }

    internal fun add(limit: Int, flag_sustain: Boolean, a: Double?, b: Double?) {
        for (i in 1 until limit) {
            if (flag_sustain)
            // interpolation
                push_back(a!! + i * ((b!! - a) / limit))
            else
                push_back(0.0)
        }

        push_back(b)
    }

    internal fun add_plain(d_last: Date, d: Date) {
        if (d.is_ordinal)
            return

        if (start_date == 0L)
            start_date = d.m_date

        if (values.isEmpty())
        // first value is being entered i.e. v_before is not set
            push_back(1.0)
        else if (calculate_distance(d, d_last) > 0)
            add(calculate_distance(d, d_last), false, 0.0, 1.0)
        else {
            val v = values.last + 1
            values[values.size - 1] = v
            if (v < value_min!!)
                value_min = v
            if (v > value_max!!)
                value_max = v
        }

        d_last.m_date = d.m_date
    }

    companion object {
        const internal val MONTHLY = 0x1
        const internal val YEARLY = 0x2
        const internal val PERIOD_MASK = 0xf

        const internal val BOOLEAN = 0x10
        const internal val CUMULATIVE = 0x20
        const internal val AVERAGE = 0x30
        const internal val VALUE_TYPE_MASK = 0xf0

        const internal val DEFAULT = MONTHLY or BOOLEAN
    }
}
