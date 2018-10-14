/***********************************************************************************

 Copyright (C) 2012-2016 Ahmet Öztürk (aoz_2@yahoo.com)

 This file is part of Lifeograph.

 Lifeograph is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Lifeograph is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Lifeograph.  If not, see <http://www.gnu.org/licenses/>.

 ***********************************************************************************/


package net.sourceforge.lifeograph;


import android.util.Log;

class NameAndValue {
    final static int HAS_NAME = 0x1;
    final static int HAS_VALUE = 0x2;
    final static int HAS_UNIT = 0x4;
    final static int HAS_EQUAL = 0x8;

    NameAndValue() {
        this("", 0.0);
    }

    NameAndValue(String n, Double v) {
        this(n, v, "", 0);
    }

    NameAndValue(String n, Double v, String u, int s) {
        name = n;
        value = v;
        unit = u;
        status = s;
    }

    String name;
    Double value;
    String unit;
    int status;

    static NameAndValue parse(String text) {
        StringBuilder name = new StringBuilder();
        StringBuilder unit = new StringBuilder();
        Double value = 0.0;
        int status = 0;

        char lf = '='; // =, \, #, $(unit)
        int divider = 0;
        int trim_length = 0;
        int trim_length_unit = 0;
        boolean negative = false;
        char c;

        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            switch (c) {
                case '\\':
                    if (lf == '#' || lf == '$') {
                        unit.append(c);
                        trim_length_unit = 0;
                        lf = '$';
                    } else if (lf == '\\') {
                        name.append(c);
                        trim_length = 0;
                        lf = '=';
                    } else // i.e. (lf == '=')
                        lf = '\\';
                    break;
                case '=':
                    if (name.length() == 0 || lf == '\\') {
                        name.append(c);
                        trim_length = 0;
                        lf = '=';
                    } else if (lf == '#' || lf == '$') {
                        unit.append(c);
                        trim_length_unit = 0;
                        lf = '$';
                    } else // i.e. ( lf == '=' )
                        lf = '#';
                    break;
                case ' ':
                case '\t':
                    // if( lf == '#' ) just ignore
                    if (lf == '=' || lf == '\\') {
                        if (name.length() != 0) { // else ignore
                            name.append(c);
                            trim_length++;
                        }
                    } else if (lf == '$') {
                        unit.append(c);
                        trim_length_unit++;
                    }
                    break;
                case ',':
                case '.':
                    if (divider != 0 || lf == '$') { // note that if divider, lf must be #
                        unit.append(c);
                        trim_length_unit = 0;
                        lf = '$';
                    } else if (lf == '#') {
                        divider = 1;
                    } else {
                        name.append(c);
                        trim_length = 0;
                        lf = '=';
                    }
                    break;
                case '-':
                    if (negative || lf == '$') { // note that if negative, lf must be #
                        unit.append(c);
                        trim_length_unit = 0;
                        lf = '$';
                    } else if (lf == '#') {
                        negative = true;
                    } else {
                        name.append(c);
                        trim_length = 0;
                        lf = '=';
                    }
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if (lf == '#') {
                        status = NameAndValue.HAS_VALUE;
                        value *= 10;
                        value += (c - '0');
                        if (divider != 0)
                            divider *= 10;
                    } else if (lf == '$') {
                        unit.append(c);
                        trim_length_unit = 0;
                    } else {
                        name.append(c);
                        trim_length = 0;
                        lf = '='; // reset ( lf == \ ) case
                    }
                    break;
                default:
                    if (lf == '#' || lf == '$') {
                        unit.append(c);
                        trim_length_unit = 0;
                        lf = '$';
                    } else {
                        name.append(c);
                        trim_length = 0;
                        lf = '=';
                    }
                    break;
            }
        }

        if (lf == '$')
            status |= (HAS_NAME | HAS_EQUAL | HAS_UNIT);
        else if (lf == '#')
            status |= (HAS_NAME | HAS_EQUAL);
        else if (name.length() != 0)
            status = HAS_NAME;

        String name_ = name.toString();
        String unit_ = unit.toString();

        if (trim_length != 0)
            name_ = name_.substring(0, name_.length() - trim_length);
        if (trim_length_unit != 0)
            unit_ = unit_.substring(0, unit_.length() - trim_length_unit);

        if (lf == '=' && !name_.isEmpty()) { // implicit boolean tag
            value = 1.0;
        } else {
            if (divider > 1)
                value /= divider;
            if (negative)
                value *= -1;
        }

        Log.d(Lifeograph.TAG, "tag parsed | name: " + name_ + "; value: " + value + "; " +
                "unit: " + unit_);

        return new NameAndValue(name_, value, unit_, status);
    }
}
