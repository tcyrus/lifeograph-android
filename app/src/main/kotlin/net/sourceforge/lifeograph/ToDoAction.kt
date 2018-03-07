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

import android.content.Context
import android.support.v4.view.ActionProvider
import android.view.MenuItem
import android.view.SubMenu
import android.view.View

class ToDoAction(context: Context)//mContext = context;
    : ActionProvider(context), MenuItem.OnMenuItemClickListener {

    //private Context mContext; // not used now

    override fun hasSubMenu(): Boolean {
        return true
    }

    override fun onPrepareSubMenu(menu: SubMenu?) {
        super.onPrepareSubMenu(menu)

        menu!!.clear()

        menu.add(0, R.id.todo_not, 0, R.string.todo_not)
                .setOnMenuItemClickListener(this)

        menu.add(0, R.id.todo_open, 1, R.string.todo_open)
                .setIcon(R.mipmap.ic_todo_open)
                .setOnMenuItemClickListener(this)

        menu.add(0, R.id.todo_progressed, 1, R.string.todo_progressed)
                .setIcon(R.mipmap.ic_todo_progressed)
                .setOnMenuItemClickListener(this)

        menu.add(0, R.id.todo_done, 1, R.string.todo_done)
                .setIcon(R.mipmap.ic_todo_done)
                .setOnMenuItemClickListener(this)

        menu.add(0, R.id.todo_canceled, 1, R.string.todo_canceled)
                .setIcon(R.mipmap.ic_todo_canceled)
                .setOnMenuItemClickListener(this)
    }

    //@Override
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> return true
            R.id.todo_not -> {
                mObject!!.setTodoStatus(DiaryElement.ES_NOT_TODO)
                return true
            }
            R.id.todo_open -> {
                mObject!!.setTodoStatus(DiaryElement.ES_TODO)
                return true
            }
            R.id.todo_progressed -> {
                mObject!!.setTodoStatus(DiaryElement.ES_PROGRESSED)
                return true
            }
            R.id.todo_done -> {
                mObject!!.setTodoStatus(DiaryElement.ES_DONE)
                return true
            }
            R.id.todo_canceled -> {
                mObject!!.setTodoStatus(DiaryElement.ES_CANCELED)
                return true
            }
        }
        return true
    }

    override fun onCreateActionView(): View? {
        return null
    }

    interface ToDoObject {
        fun setTodoStatus(s: Int)
    }

    internal var mObject: ToDoObject? = null
}
