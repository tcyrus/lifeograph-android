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

import android.support.annotation.NonNull
import java.util.ArrayList
import java.util.TreeMap

open class Tag : DiaryElementChart {

    @JvmOverloads constructor(diary: Diary, name: String, ctg: Category?, chart_type: Int = ChartPoints.DEFAULT) :
            super(diary, name, DiaryElement.ES_VOID, chart_type) {
        this.m_ptr2category = ctg
        ctg?.add(this)
    }

    /*
    @JvmOverloads constructor(diary: Diary) : super(diary, DiaryElement.ES_VOID, "")
    */

    internal var m_ptr2category: Category?

    var category: Category?
        get() = m_ptr2category
        set(ctg) {
            m_ptr2category?.remove(this)
            ctg?.add(this)
            m_ptr2category = ctg
        }

    // THEMES
    val theme: Theme
        get() = m_theme ?: Theme.System.get()

    val hasOwnTheme: Boolean
        get() = m_theme != null

    val ownTheme: Theme
        get() {
            if (m_theme == null) {
                m_theme = Theme()

                for (entry in mEntries.keys)
                    entry.update_theme()
            }

            return m_theme!!
        }

    internal val isBoolean: Boolean
        get() = m_chart_type and ChartPoints.VALUE_TYPE_MASK == ChartPoints.BOOLEAN
    @JvmField internal var mEntries: TreeMap<Entry, Double> = TreeMap(DiaryElement.compare_elems_by_date)
    private var m_theme: Theme? = null
    @JvmField @NonNull internal var unit = ""

    class Category(diary: Diary, name: String) : DiaryElement(diary, name, DiaryElement.ES_EXPANDED) {

        var expanded: Boolean
            get() = m_status and DiaryElement.ES_EXPANDED != 0
            set(expanded) = set_status_flag(DiaryElement.ES_EXPANDED, expanded)

        // CONTENTS
        @JvmField internal var mTags: MutableList<Tag> = ArrayList()

        init {
            m_name = name
        }

        override fun get_type(): DiaryElement.Type {
            return DiaryElement.Type.TAG_CTG
        }

        override fun get_size(): Int {
            return mTags.size
        }

        override fun get_icon(): Int {
            return R.mipmap.ic_tag
        }

        override fun get_info_str(): String {
            return mTags.size.toString() + " entries"
        }

        fun add(tag: Tag) {
            mTags.add(tag)
        }

        fun remove(tag: Tag) {
            mTags.remove(tag)
        }
    }

    override fun get_type(): DiaryElement.Type {
        return DiaryElement.Type.TAG
    }

    override fun get_size(): Int {
        return mEntries.size
    }

    override fun get_icon(): Int {
        return if (hasOwnTheme) R.mipmap.ic_theme_tag else R.mipmap.ic_tag
    }

    override fun get_info_str(): String {
        return _size.toString() + " entries"
    }

    override fun getListStrSecondary(): String {
        return "Tag with $_size entries"
    }

    fun get_name_and_value(entry: Entry, flag_escape: Boolean, flag_unit: Boolean): String {
        val result = StringBuilder(if (flag_escape) escape_name(m_name) else m_name)

        if (!isBoolean) {
            result.append(" = ").append(get_value(entry))

            // addressing Java-shortcomings
            if (result.toString().endsWith(".0") || result.toString().endsWith(",0"))
                result.delete(result.length - 2, result.length)

            if (flag_unit && !unit.isEmpty())
                result.append(" ").append(unit)
        }

        return result.toString()
    }

    @JvmOverloads
    fun add_entry(entry: Entry, value: Double? = 1.0) {
        mEntries[entry] = value!!
    }

    fun remove_entry(entry: Entry) {
        mEntries.remove(entry)
    }

    fun create_own_theme_duplicating(theme: Theme): Theme {
        m_theme = Theme(theme)

        for (entry in mEntries.keys)
            entry.update_theme()

        return m_theme!!
    }

    fun reset_theme() {
        m_theme?.let {
            m_theme = null

            for (entry in mEntries.keys)
                entry.update_theme()
        }
    }

    // PARAMETRIC TAG SYSTEM PROPERTIES
    internal fun get_value(entry: Entry): Double {
        return if (mEntries.containsKey(entry))
            mEntries[entry]!!
        else
            -404.0
    }

    internal override fun create_chart_data(): ChartPoints? {
        if (mEntries.isEmpty())
            return null

        val cp = ChartPoints(m_chart_type)
        cp.unit = unit

        // order from old to new: d/v_before > d/v_last > d/v
        var d_before = Date(Date.NOT_SET)
        var d_last = Date(Date.NOT_SET)
        var d: Date
        var v_before = 0.0
        var v_last = 0.0
        var v: Double
        var no_of_entries = 0

        // LAMBDA: auto add_value = [ & ]() -> ... see below

        for ((key, value) in mEntries.descendingMap()) {
            d = key._date

            if (d.is_ordinal)
                break

            if (cp.start_date == 0L)
                cp.start_date = d.m_date
            if (!d_last.is_set)
                d_last = d

            v = if (isBoolean) 1.0 else value

            if (cp.calculate_distance(d, d_last) > 0) {
                // add_value() = due to lack of lambdas:
                val flag_sustain = m_chart_type and ChartPoints.VALUE_TYPE_MASK == ChartPoints.AVERAGE
                if (flag_sustain && no_of_entries > 1)
                    v_last /= no_of_entries.toDouble()

                if (cp.values.isEmpty()) {
                    // first value is being entered i.e. v_before is not set
                    cp.add(0, false, 0.0, v_last)
                } else
                    cp.add(cp.calculate_distance(d_last, d_before),
                            flag_sustain, v_before, v_last)

                v_before = v_last
                v_last = v
                d_before = d_last
                d_last = d
                no_of_entries = 1
            } else {
                v_last += v
                no_of_entries++
            }
        }

        //add_value() = due to lack of lambdas:
        run {
            val flag_sustain = m_chart_type and ChartPoints.VALUE_TYPE_MASK == ChartPoints.AVERAGE
            if (flag_sustain && no_of_entries > 1)
                v_last /= no_of_entries.toDouble()

            if (cp.values.isEmpty())
                // first value is being entered i.e. v_before is not set
                cp.add(0, false, 0.0, v_last)
            else
                cp.add(cp.calculate_distance(d_last, d_before),
                        flag_sustain, v_before, v_last)

            // NOTE: last assignments in lambda were not necessary here
        }

        //TODO: Diary.d.fill_up_chart_points( cp );

        return cp
    }

    companion object {

        @JvmStatic internal fun escape_name(name: String): String {
            val result = StringBuilder()

            for (c in name) {
                if (c == '=' || c == '\\')
                    result.append('\\')
                result.append(c)
            }

            return result.toString()
        }
    }
}
