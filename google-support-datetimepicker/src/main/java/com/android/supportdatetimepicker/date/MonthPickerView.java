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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.supportdatetimepicker.R;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Displays a selectable grid of months.
 */
public class MonthPickerView extends LinearLayout implements DatePickerDialog.OnDateChangedListener,
    OnClickListener {
    private final DatePickerController mController;
    private TextViewWithCircularIndicator mSelectedView;

    /**
     * @param context
     */
    public MonthPickerView(Context context, DatePickerController controller) {
        super(context);
        mController = controller;
        mController.registerOnDateChangedListener(this);
        init(context);
        onDateChanged();
    }

    private void init(Context context) {
        String[] months = new String[12];
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Format formatter = new SimpleDateFormat("LLL");
        for (int month = Calendar.JANUARY, i = 0; month <= Calendar.DECEMBER; month++, i++) {
            calendar.set(Calendar.MONTH, month);
            months[i] = formatter.format(new Date(calendar.getTimeInMillis()));
        }
        ViewGroup.LayoutParams frame = new ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        setLayoutParams(frame);
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams tableRowParams = new LinearLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        int month = 0;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < 4; i++) {
            LinearLayout row = new LinearLayout(context);
            row.setLayoutParams(tableRowParams);
            addView(row);
            for (int j = 0; j < 3; j++) {
                TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) layoutInflater.inflate(R.layout.month_label_text_view, row, false);
                v.setOnClickListener(MonthPickerView.this);
                v.setTag(month);
                v.setText(months[month]);
                boolean selected = mController.getSelectedDay().month == month;
                v.drawIndicator(selected);
                if (selected) {
                    mSelectedView = v;
                }
                row.addView(v);
                month++;
            }
        }
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

    public void postSetSelection() {
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
            }
        });
    }

    @Override
    public void onDateChanged() {
        postSetSelection();
    }
}
