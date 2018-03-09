/***********************************************************************************
 *
 * Copyright (C) 2012-2014 Ahmet Öztürk (aoz_2@yahoo.com)
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


import android.graphics.Color

open class Theme {

    open fun is_system(): Boolean = false

    @JvmField protected var font: String = ""
    @JvmField protected var color_base: Int = 0
    @JvmField protected var color_text: Int = 0
    @JvmField protected var color_heading: Int = 0
    @JvmField protected var color_subheading: Int = 0
    @JvmField protected var color_highlight: Int = 0

    class System : Theme() {

        override fun is_system(): Boolean = true

        init {
            color_base = Color.WHITE
            color_text = Color.BLACK
            color_heading = Color.BLUE
            color_subheading = Color.parseColor("#F066FC")
            color_highlight = Color.parseColor("#FFF955")
        }

        companion object {
            @JvmStatic
            fun get(): System {
                // initialize if not already initialized:
                if (!(::system.isInitialized)) {
                    system = System()
                }
                return system
            }

            private lateinit var system: System
        }
    }

    constructor()

    constructor(theme: Theme) {
        font = theme.font
        color_base = theme.color_base
        color_text = theme.color_text
        color_heading = theme.color_heading
        color_subheading = theme.color_subheading
        color_highlight = theme.color_highlight
    }

    companion object {
        // CONSTANT COLORS
        @JvmField val s_color_match1 = Color.parseColor("#33FF33")
        @JvmField val s_color_match2 = Color.parseColor("#009900")
        //@JvmField val s_color_link1 = Color.parseColor( "#3333FF" ); LATER
        //@JvmField val s_color_link2 = Color.parseColor( "#000099" );
        //@JvmField val s_color_broken1 = Color.parseColor( "#FF3333" );
        //@JvmField val s_color_broken2 = Color.parseColor( "#990000" );

        @JvmField val s_color_todo = Color.parseColor("#FF0000")
        @JvmField val s_color_progressed = Color.parseColor("#FF8811")
        @JvmField val s_color_done = Color.parseColor("#66BB00")
        @JvmField val s_color_done1 = Color.parseColor("#77CC11")
        @JvmField val s_color_done2 = Color.parseColor("#409000")
        @JvmField val s_color_canceled = Color.parseColor("#AA8855")

        private fun parse_color_sub(color: String, begin: Int, end: Int): Int {
            var ret_val = 0

            for (i in begin..end) {
                val c = color[i]
                if (c in '0'..'9') {
                    ret_val *= 16
                    ret_val += c - '0'
                } else if (c in 'a'..'f') {
                    ret_val *= 16
                    ret_val += c - 'a' + 10
                } else if (c in 'A'..'F') {
                    ret_val *= 16
                    ret_val += c - 'A' + 10
                }
            }

            return ret_val
        }

        @JvmStatic fun parse_color(color: String): Int {
            return Color.rgb(parse_color_sub(color, 1, 2),
                    parse_color_sub(color, 5, 6),
                    parse_color_sub(color, 9, 10))
        }

        @JvmStatic fun color2string(i_color: Int): String {
            return String.format("#%02X%<02X%02X%<02X%02X%<02X",
                    Color.red(i_color),
                    Color.green(i_color),
                    Color.blue(i_color))
        }

        @JvmStatic fun midtone(c1: Int, c2: Int, ratio: Float): Int {
            return Color.rgb(
                    (Color.red(c1) * ratio + Color.red(c2) * (1.0 - ratio)).toInt(),
                    (Color.green(c1) * ratio + Color.green(c2) * (1.0 - ratio)).toInt(),
                    (Color.blue(c1) * ratio + Color.blue(c2) * (1.0 - ratio)).toInt())
        }

        @JvmStatic fun contrast(bg: Int, c1: Int, c2: Int): Int {
            val dist1 = Math.abs(Color.red(bg) - Color.red(c1)) +
                    Math.abs(Color.green(bg) - Color.green(c1)) +
                    Math.abs(Color.blue(bg) - Color.blue(c1))

            val dist2 = Math.abs(Color.red(bg) - Color.red(c2)) +
                    Math.abs(Color.green(bg) - Color.green(c2)) +
                    Math.abs(Color.blue(bg) - Color.blue(c2))

            return if (dist1 > dist2) c1 else c2
        }
    }
}
