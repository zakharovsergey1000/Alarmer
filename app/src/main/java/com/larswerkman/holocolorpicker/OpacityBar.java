/* Copyright 2012 Lars Werkman Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless
 * required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License. */
package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import biz.advancedcalendar.alarmer.R;

public class OpacityBar extends View {
	/* Constants used to save/restore the instance state. */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_VALUE = "value";
	// private static final String STATE_ORIENTATION = "orientation";
	/** Constants used to identify orientation. */
	private static final boolean ORIENTATION_HORIZONTAL = true;
	private static final boolean ORIENTATION_VERTICAL = false;
	/** Default orientation of the bar. */
	private static final boolean ORIENTATION_DEFAULT = OpacityBar.ORIENTATION_HORIZONTAL;
	/** The thickness of the bar. */
	private int mBarThickness;
	/** The length of the bar. */
	private int mBarLength;
	private int mPreferredBarLength;
	/** The radius of the pointer. */
	private int mBarPointerRadius;
	/** The radius of the halo of the pointer. */
	private int mBarPointerHaloRadius;
	private float mTouchPositionInBarAndBarPointerPositionDelta;
	/** {@code Paint} instance used to draw the bar. */
	private Paint mBarPaint;
	/** {@code Paint} instance used to draw the pointer. */
	private Paint mBarPointerPaint;
	/** {@code Paint} instance used to draw the halo of the pointer. */
	private Paint mBarPointerHaloPaint;
	/** The rectangle enclosing the bar. */
	private RectF mBarRect = new RectF();
	/** {@code Shader} instance used to fill the shader of the paint. */
	private Shader shader;
	/** {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 *
	 * @see #onTouchEvent(android.view.MotionEvent) */
	private boolean mIsMovingPointer;
	/** The currently set value */
	private int mCurrentValue;
	/** Factor used to calculate the position to the Opacity on the bar. */
	private float mPositionToValueFactor;
	/** Factor used to calculate the Opacity to the position on the bar. */
	private float mValueToPositionFactor;
	/** {@code ColorPicker} instance used to control the ColorPicker. */
	private ColorPicker mPicker = null;
	/** Used to toggle orientation between vertical and horizontal. */
	private boolean mOrientation;

	public OpacityBar(Context context) {
		super(context);
		init(null, 0);
	}

	public OpacityBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public OpacityBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorBars, defStyle, 0);
		final Resources b = getContext().getResources();
		if (isInEditMode()) {
			float density = getResources().getDisplayMetrics().density;
			mBarThickness = a.getDimensionPixelSize(R.styleable.ColorBars_bar_thickness,
					(int) (density * 4 + 0.5));
			mBarLength = a.getDimensionPixelSize(R.styleable.ColorBars_bar_length,
					(int) (density * 240 + 0.5));
			mPreferredBarLength = mBarLength;
			mBarPointerRadius = a.getDimensionPixelSize(
					R.styleable.ColorBars_bar_pointer_radius, (int) (density * 6 + 0.5));
			mBarPointerHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorBars_bar_pointer_halo_radius,
					(int) (density * 14 + 0.5));
			mOrientation = a.getBoolean(R.styleable.ColorBars_bar_orientation_horizontal,
					OpacityBar.ORIENTATION_DEFAULT);
		} else {
			mBarThickness = a.getDimensionPixelSize(R.styleable.ColorBars_bar_thickness,
					b.getDimensionPixelSize(R.dimen.bar_thickness));
			mBarLength = a.getDimensionPixelSize(R.styleable.ColorBars_bar_length,
					b.getDimensionPixelSize(R.dimen.bar_length));
			mPreferredBarLength = mBarLength;
			mBarPointerRadius = a.getDimensionPixelSize(
					R.styleable.ColorBars_bar_pointer_radius,
					b.getDimensionPixelSize(R.dimen.bar_pointer_radius));
			mBarPointerHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorBars_bar_pointer_halo_radius,
					b.getDimensionPixelSize(R.dimen.bar_pointer_halo_radius));
			mOrientation = a.getBoolean(R.styleable.ColorBars_bar_orientation_horizontal,
					OpacityBar.ORIENTATION_DEFAULT);
		}
		a.recycle();
		mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPaint.setShader(shader);
		mBarPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBarPointerHaloPaint.setColor(Color.BLACK);
		mBarPointerHaloPaint.setAlpha(0x50);
		mBarPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCurrentValue = 0xFF;
		mBarPointerPaint.setColor(Color.argb(mCurrentValue, 0xFF, 0xFF, 0xFF));
		mPositionToValueFactor = 0xFF / (float) mBarLength;
		mValueToPositionFactor = (float) mBarLength / 0xFF;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int barPointerHaloRadiusx2 = mBarPointerHaloRadius * 2;
		final int intrinsicSize = mPreferredBarLength + barPointerHaloRadiusx2;
		// Variable orientation
		int measureSpec;
		if (mOrientation == OpacityBar.ORIENTATION_HORIZONTAL) {
			measureSpec = widthMeasureSpec;
		} else {
			measureSpec = heightMeasureSpec;
		}
		int lengthMode = MeasureSpec.getMode(measureSpec);
		int lengthSize = MeasureSpec.getSize(measureSpec);
		int length;
		if (lengthMode == MeasureSpec.EXACTLY) {
			length = lengthSize;
		} else if (lengthMode == MeasureSpec.AT_MOST) {
			length = Math.min(intrinsicSize, lengthSize);
		} else {
			length = intrinsicSize;
		}
		mBarLength = length - barPointerHaloRadiusx2;
		if (mOrientation == OpacityBar.ORIENTATION_VERTICAL) {
			setMeasuredDimension(barPointerHaloRadiusx2, length);
		} else {
			setMeasuredDimension(length, barPointerHaloRadiusx2);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Fill the rectangle instance based on orientation
		int x1, y1;
		if (mOrientation == OpacityBar.ORIENTATION_HORIZONTAL) {
			x1 = mBarLength + mBarPointerHaloRadius;
			y1 = mBarThickness;
			mBarLength = w - mBarPointerHaloRadius * 2;
			mBarRect.set(mBarPointerHaloRadius,
					mBarPointerHaloRadius - mBarThickness / 2, mBarLength
							+ mBarPointerHaloRadius, mBarPointerHaloRadius
							+ mBarThickness / 2);
		} else {
			x1 = mBarThickness;
			y1 = mBarLength + mBarPointerHaloRadius;
			mBarLength = h - mBarPointerHaloRadius * 2;
			mBarRect.set(mBarPointerHaloRadius - mBarThickness / 2,
					mBarPointerHaloRadius, mBarPointerHaloRadius + mBarThickness / 2,
					mBarLength + mBarPointerHaloRadius);
		}
		shader = new LinearGradient(mBarPointerHaloRadius, 0, x1, y1, new int[] {
				Color.TRANSPARENT, Color.WHITE}, null, Shader.TileMode.CLAMP);
		mBarPaint.setShader(shader);
		mPositionToValueFactor = 0xFF / (float) mBarLength;
		mValueToPositionFactor = (float) mBarLength / 0xFF;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Draw the bar.
		canvas.drawRect(mBarRect, mBarPaint);
		// Calculate the center of the pointer.
		int cX, cY, c = Math.round(mBarPointerHaloRadius + mCurrentValue
				* mValueToPositionFactor);
		if (mOrientation == OpacityBar.ORIENTATION_HORIZONTAL) {
			cX = c;
			cY = mBarPointerHaloRadius;
		} else {
			cX = mBarPointerHaloRadius;
			cY = c;
		}
		// Draw the pointer halo.
		canvas.drawCircle(cX, cY, mBarPointerHaloRadius, mBarPointerHaloPaint);
		// Draw the pointer.
		canvas.drawCircle(cX, cY, mBarPointerRadius, mBarPointerPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);
		// Convert coordinates to our internal coordinate system
		float touchPositionInBar;
		if (mOrientation == OpacityBar.ORIENTATION_HORIZONTAL) {
			touchPositionInBar = event.getX() - mBarPointerHaloRadius;
		} else {
			touchPositionInBar = event.getY() - mBarPointerHaloRadius;
		}
		float barPointerPosition = mValueToPositionFactor * mCurrentValue;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsMovingPointer = true;
			// Check whether the user pressed on the pointer
			if (barPointerPosition - mBarPointerHaloRadius <= touchPositionInBar
					&& touchPositionInBar <= barPointerPosition + mBarPointerHaloRadius) {
				mTouchPositionInBarAndBarPointerPositionDelta = touchPositionInBar
						- barPointerPosition;
				break;
			}
			//
			if (0 <= touchPositionInBar && touchPositionInBar <= mBarLength) {
				barPointerPosition = touchPositionInBar;
			} else if (touchPositionInBar < 0) {
				barPointerPosition = 0;
			} else {
				barPointerPosition = mBarLength;
			}
			//
			mCurrentValue = (int) (barPointerPosition * mPositionToValueFactor + 0.5f);
			if (mPicker != null) {
				mPicker.setNewAlpha(mCurrentValue);
			}
			mTouchPositionInBarAndBarPointerPositionDelta = 0;
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsMovingPointer) {
				if (mTouchPositionInBarAndBarPointerPositionDelta <= -mBarPointerHaloRadius
						|| mBarPointerHaloRadius <= mTouchPositionInBarAndBarPointerPositionDelta) {
					mTouchPositionInBarAndBarPointerPositionDelta = touchPositionInBar
							- barPointerPosition;
				} else {
					// Move the pointer on the bar.
					float touchPositionInBarWithSubtractedDelta = touchPositionInBar
							- mTouchPositionInBarAndBarPointerPositionDelta;
					if (0 <= touchPositionInBarWithSubtractedDelta
							&& touchPositionInBarWithSubtractedDelta <= mBarLength) {
						barPointerPosition = touchPositionInBarWithSubtractedDelta;
					} else if (touchPositionInBarWithSubtractedDelta < 0) {
						barPointerPosition = 0;
						mTouchPositionInBarAndBarPointerPositionDelta = touchPositionInBar
								- barPointerPosition;
					} else {
						barPointerPosition = mBarLength;
						mTouchPositionInBarAndBarPointerPositionDelta = touchPositionInBar
								- barPointerPosition;
					}
					mCurrentValue = (int) (barPointerPosition * mPositionToValueFactor + 0.5f);
					mBarPointerPaint
							.setColor(Color.argb(mCurrentValue, 0xFF, 0xFF, 0xFF));
					if (mPicker != null) {
						mPicker.setNewAlpha(mCurrentValue);
					}
					invalidate();
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			mIsMovingPointer = false;
			break;
		}
		return true;
	}

	/** Set the pointer on the bar.
	 *
	 * @param value
	 *            int between 0 and 255 */
	public void setValue(int value) {
		mCurrentValue = value;
		mBarPointerPaint.setColor(Color.argb(value, 0xFF, 0xFF, 0xFF));
		invalidate();
	}

	/** Get the currently selected value.
	 *
	 * @return The currently selected value. */
	public int getValue() {
		return mCurrentValue;
	}

	/** Adds a {@code ColorPicker} instance to the bar.
	 *
	 * @param picker */
	public void setColorPicker(ColorPicker picker) {
		mPicker = picker;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle state = new Bundle();
		state.putParcelable(OpacityBar.STATE_PARENT, superState);
		state.putInt(OpacityBar.STATE_VALUE, getValue());
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;
		Parcelable superState = savedState.getParcelable(OpacityBar.STATE_PARENT);
		super.onRestoreInstanceState(superState);
		setValue(savedState.getInt(OpacityBar.STATE_VALUE));
	}
}