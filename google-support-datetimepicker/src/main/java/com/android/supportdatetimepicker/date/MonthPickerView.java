/* Copyright (C) 2013 The Android Open Source Project Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the
 * License. */
package com.android.supportdatetimepicker.date;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.supportdatetimepicker.R;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateChangedListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Displays a selectable list of years.
 */
public class MonthPickerView extends TableLayout implements OnDateChangedListener,
    OnClickListener {
    private static final String TAG = "MonthPickerView";
    private final DatePickerController mController;
    private MonthAdapter2 mAdapter;
    private int mViewSize;
    private int mChildSize;
    private TextViewWithCircularIndicator mSelectedView;

    /**
     * @param context
     */
    public MonthPickerView(Context context, DatePickerController controller) {
        super(context);
        mController = controller;
        mController.registerOnDateChangedListener(this);
        init(context);
        ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(frame);
        // setBackgroundColor(0xFF00FFFF);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        TableRow.LayoutParams tableCellParams = new TableRow.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        // tableRowParams.weight = 1.0f;
        // tableCellParams.gravity = Gravity.CENTER;
        int month = 0;
        for (int i = 0; i < 4; i++) {
            TableRow row = new TableRow(context);
            row.setLayoutParams(tableRowParams);
            // row.setBackgroundColor(0xFF0000FF);
            addView(row);
            for (int j = 0; j < 3; j++) {
                // TextView textView = new TextView(context);// textView.set
                // textView.setLayoutParams(tableCellParams);
                // textView.setGravity(Gravity.CENTER);
                // textView.setText("" + month++);
                // if (month > 6)
                // textView.setBackgroundColor(0xFF00FF00);
                // row.addView(textView);
                TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) mAdapter
                    .getView(month++, null, row);
                v.setLayoutParams(tableCellParams);
                v.setGravity(Gravity.CENTER);
                row.addView(v);
            }
        }
        Resources res = context.getResources();
        // mViewSize =
        // res.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
        // mChildSize = res.getDimensionPixelOffset(R.dimen.year_label_height);
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength(mChildSize / 3);
        // setOnItemClickListener(this);
        // setSelector(new StateListDrawable());
        // setDividerHeight(0);
        onDateChanged();
    }

    private void init(Context context) {
        ArrayList<String> months = new ArrayList<String>();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Format formatter = new SimpleDateFormat("MMM");
        for (int month = Calendar.JANUARY; month <= Calendar.DECEMBER; month++) {
            calendar.set(Calendar.MONTH, month);
            months.add(formatter.format(new Date(calendar.getTimeInMillis())));
        }
        mAdapter = new MonthAdapter2(context, R.layout.support_year_label_text_view,
            months);
        // setAdapter(mAdapter);
    }

    @Override
    public void onClick(View view) {
        mController.tryVibrate();
        TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
        if (clickedView != null) {
            if (clickedView != mSelectedView) {
                if (mSelectedView != null) {
                    mSelectedView.drawIndicator(false);
                    mSelectedView.requestLayout();
                }
                clickedView.drawIndicator(true);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }
            mController
                .onMonthSelected(MonthPickerView.getMonthFromTextView(clickedView));
            // mAdapter.notifyDataSetChanged();
            requestLayout();
        }
    }

    private static int getMonthFromTextView(TextView view) {
        return (Integer) view.getTag();
    }

    private class MonthAdapter2 extends ArrayAdapter<String> {
        public MonthAdapter2(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) super
                .getView(position, convertView, parent);
            v.setOnClickListener(MonthPickerView.this);
            v.setTag(position);
            v.requestLayout();
            boolean selected = mController.getSelectedDay().month == position;
            v.drawIndicator(selected);
            if (selected) {
                mSelectedView = v;
            }
            return v;
        }
    }

    // public void postSetSelectionCentered(final int position) {
    // postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
    // }
    //
    public void postSetSelection(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                // setSelectionFromTop(position, offset);
                requestLayout();
            }
        });
    }

    // public int getFirstPositionOffset() {
    // final View firstChild = getChildAt(0);
    // if (firstChild == null) {
    // return 0;
    // }
    // return firstChild.getTop();
    // }
    @Override
    public void onDateChanged() {
        // mAdapter.notifyDataSetChanged();
        postSetSelection(mController.getSelectedDay().month);
    }
    // @Override
    // public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    // super.onInitializeAccessibilityEvent(event);
    // if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SELECTED) {
    // // event.setFromIndex(0);
    // // event.setToIndex(0);
    // }
    // }
}
