/***********************************************************************************
 * Copyright (C) 2012-2016 Ahmet Öztürk (aoz_2@yahoo.com)
 * <p/>
 * This file is part of Lifeograph.
 * <p/>
 * Lifeograph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * Lifeograph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with Lifeograph.  If not, see <http://www.gnu.org/licenses/>.
 ***********************************************************************************/

package net.sourceforge.lifeograph;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


public class ViewEntryTags extends View implements GestureDetector.OnGestureListener
{
    // CONSTANTS (different in Android)
    static final float MARGIN = Lifeograph.getScreenShortEdge() * Lifeograph.sDPIX / 70f /
                                (Lifeograph.getScreenShortEdge() >= 2.8 ?
                                (float) Math.log(Lifeograph.getScreenShortEdge()) : 1f);
    static final float HSPACING = MARGIN / 1.16f;
    static final float VSPACING = MARGIN / 0.7f;
    static final float TEXT_HEIGHT = MARGIN / 0.4f;
    static final float TEXT_PADDING = MARGIN / 2.8f;
    static final float ITEM_HEIGHT =  TEXT_HEIGHT + (2 * TEXT_PADDING);
    static final float HALF_HEIGHT = ITEM_HEIGHT / 2;
    static final float STROKE_WIDTH = MARGIN / 14f;

    public ViewEntryTags(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;

        // we set a new Path
        mPath = new Path();

        // and we set a new Paint with the desired attributes
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(4 * STROKE_WIDTH);

        mGestureDetector = new GestureDetector(c, this);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void set_entry(Entry entry) {
        m_ptr2entry = entry;
        m_pos_x = MARGIN;

        m_items.clear();
        List<Tag> tags = m_ptr2entry.get_tags();
        for (Tag tag : tags) {
            TagItem ti = new TagItem(tag.get_name_and_value(entry, false, true));
            ti.tag = tag;

            m_items.add(ti);
        }

        if (m_flag_editable) {
            TagItem ti_add = new TagItem("Add Tag");
            m_items.add(ti_add);
        }

        invalidate();
    }

    void update() {
        invalidate();
    }

    void add_item( Canvas canvas, TagItem ti ) {
        float text_width = mPaint.measureText( ti.label );
        if( m_pos_x + ( 2 * TEXT_PADDING ) + text_width + HALF_HEIGHT + MARGIN > m_width ) {
            m_pos_x = MARGIN;
            m_pos_y += ( ITEM_HEIGHT + VSPACING );
            mDesiredHeight += ( ITEM_HEIGHT + VSPACING );
        }

        ti.xl = m_pos_x - HSPACING / 2;
        ti.xr = ti.xl + TEXT_PADDING + text_width + TEXT_PADDING + HALF_HEIGHT + HSPACING;
        ti.yl = m_pos_y - VSPACING / 2;
        ti.yr = ti.yl + ITEM_HEIGHT + VSPACING;

        mPath.reset(); // reset path

        // BACKGROUND
        if (ti.hovered || ti.tag != null) {
            float h_width = ( TEXT_PADDING + text_width + TEXT_PADDING );

            mPath.moveTo( m_pos_x, m_pos_y );
            mPath.rLineTo( h_width, 0.0f );
            mPath.rLineTo( HALF_HEIGHT, HALF_HEIGHT );
            mPath.rLineTo( HALF_HEIGHT * -1, HALF_HEIGHT );
            mPath.rLineTo( -h_width, 0 );
            mPath.close();

            if (ti.hovered && m_flag_editable)
                mPaint.setStrokeWidth( 6 * STROKE_WIDTH );

            if (ti.tag != null && ti.tag.getHasOwnTheme()) {
                mPaint.setColor(ti.tag.getTheme().color_base);
                canvas.drawPath( mPath, mPaint );

                mPaint.setColor(ti.tag.getTheme().color_highlight);
                mPaint.setStyle( Paint.Style.STROKE );
                canvas.drawPath( mPath, mPaint );

                mPaint.setPathEffect( new DashPathEffect( new float[] { 5, 10 }, 0 ) );
                mPaint.setColor(ti.tag.getTheme().color_heading);
                canvas.drawPath( mPath, mPaint );
                mPaint.setPathEffect( null );
            }
            else {
                mPaint.setColor( Color.WHITE );
                canvas.drawPath( mPath, mPaint );
                mPaint.setStyle( Paint.Style.STROKE );
                canvas.drawPath( mPath, mPaint );
            }
            mPaint.setStyle( Paint.Style.FILL );

            if( ti.hovered && m_flag_editable )
                mPaint.setStrokeWidth( STROKE_WIDTH );  // restore the stroke width
        }

        // LABEL
        if( ti.tag == null )
            mPaint.setColor( ti.hovered ? m_color_text_default : Color.WHITE );
        else if( ti.tag.getHasOwnTheme() )
            mPaint.setColor(ti.tag.getTheme().color_text);
        else
            mPaint.setColor( m_color_text_default );

        canvas.drawText( ti.label, m_pos_x + TEXT_PADDING, m_pos_y + TEXT_HEIGHT, mPaint );
        // NOTE: TEXT_PADDING is not added to the y coordinate making text doubly padded at the
        // bottom as this produces a better result

        m_pos_x += ( TEXT_PADDING + text_width + TEXT_PADDING + HALF_HEIGHT + HSPACING );
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh ) {
        super.onSizeChanged( w, h, oldw, oldh );

        m_width = w;
    }

    @Override
    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );

        if (m_ptr2entry == null)
            return;

        m_pos_x = m_pos_y = MARGIN;
        mDesiredHeight = ( int ) ( MARGIN + ITEM_HEIGHT + MARGIN );
        mPaint.setTextSize( TEXT_HEIGHT );
        mPaint.setStyle( Paint.Style.FILL );
        mPaint.setStrokeWidth( STROKE_WIDTH );

        if (m_items.isEmpty() && !m_flag_editable) {
            mPaint.setColor(m_color_text_default);
            canvas.drawText("Not tagged", m_pos_x, m_pos_y + TEXT_HEIGHT, mPaint);
        }

        for (TagItem ti: m_items)
            add_item(canvas, ti);

        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = mDesiredHeight;
        setLayoutParams(lp);
    }

    //override the onTouchEvent
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mGestureDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    // GestureDetector.OnGestureListener INTERFACE METHODS
    public boolean onDown(MotionEvent event) {
        if (m_flag_editable) {
            for (TagItem ti : m_items) {
                if (event.getX() > ti.xl && event.getX() < ti.xr &&
                    ti.yl < event.getY() && ti.yr > event.getY()) {
                    ti.hovered = true;
                    update();
                    break;
                }
            }
        }
        return true;
    }

    public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
        return true;
    }

    public void onLongPress( MotionEvent event ) {
    }

    public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
        return true;
    }

    public void onShowPress( MotionEvent event ) {
        if( m_flag_editable ) {
            Tag tag = null;
            for( TagItem ti : m_items ) {
                if( ti.hovered ) {
                    ti.hovered = false;
                    tag = ti.tag;
                }
            }
            update();
            mListener.onTagSelected( tag );
        }
    }

    public boolean onSingleTapUp( MotionEvent event ) {
        return true;
    }

    // INTERFACE
    public interface Listener
    {
        void onTagSelected( Tag tag );
    }

    // DATA
    private Entry m_ptr2entry = null;
    private int m_color_text_default = getResources().getColor( R.color.t_mid );

    class TagItem
    {
        TagItem(String l) {
            label = l;
        }

        Tag tag = null;
        String label = "";
        float xl, xr, yl, yr;
        boolean hovered = false;
    }

    private List<TagItem> m_items = new ArrayList<>();

    // GEOMETRICAL VARIABLES
    private int m_width = 0;
    private int mDesiredHeight;
    float m_pos_x = MARGIN;
    float m_pos_y = MARGIN;

    Context context;
    private Paint mPaint;
    private Path mPath;

    boolean m_flag_editable = !Diary.diary.is_read_only();

    private GestureDetector mGestureDetector;
    private Listener mListener = null;
}
