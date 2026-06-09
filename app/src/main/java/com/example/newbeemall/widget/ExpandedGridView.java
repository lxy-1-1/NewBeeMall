package com.example.newbeemall.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class ExpandedGridView extends GridView {
    public ExpandedGridView(Context context) {
        super(context);
    }

    public ExpandedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandedGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandedSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandedSpec);
    }
}
