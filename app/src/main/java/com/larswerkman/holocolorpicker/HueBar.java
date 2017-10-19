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
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import biz.advancedcalendar.alarmer.R;

/** Displays a holo-themed HueBar.
 * <p>
 * Use {@link #getArgbColor()} to retrieve the selected color. <br>
 * Use {@link #addSVBar(SVBar)} to add a Saturation/Value Bar. <br>
 * Use {@link #setOpacityBar(OpacityBar)} to add a Opacity Bar.
 * </p> */
public class HueBar extends View {
	/* Constants used to save/restore the instance state. */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ARGB = "STATE_ARGB";
	private static final String STATE_HUE = "STATE_HUE";
	private static final String STATE_SATURATION = "STATE_SATURATION";
	private static final String STATE_VALUE = "STATE_VALUE";
	private static final String STATE_OLD_COLOR = "oldColor";
	private static final String STATE_SHOW_OLD_COLOR = "showColor";
	/** Colors to construct the color wheel using {@link android.graphics.SweepGradient}. */
	private static final int[] COLORS = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF,
			0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
	/** {@code Paint} instance used to draw the color wheel. */
	private Paint mColorWheelPaint;
	/** {@code Paint} instance used to draw the pointer's "halo". */
	private Paint mPointerHaloPaint;
	/** {@code Paint} instance used to draw the pointer (the selected color). */
	private Paint mPointerPaint;
	/** The width of the color wheel thickness. */
	private int mColorWheelThickness;
	/** The radius of the color wheel. */
	private int mColorWheelRadius;
	private int mPreferredColorWheelRadius;
	/** The radius of the center circle inside the color wheel. */
	private int mColorCenterRadius;
	private int mPreferredColorCenterRadius;
	/** The radius of the halo of the center circle inside the color wheel. */
	private int mColorCenterHaloRadius;
	private int mPreferredColorCenterHaloRadius;
	/** The radius of the pointer. */
	private int mColorPointerRadius;
	/** The radius of the halo of the pointer. */
	private int mColorPointerHaloRadius;
	/** The rectangle enclosing the color wheel. */
	private RectF mColorWheelRectangle = new RectF();
	/** The rectangle enclosing the center inside the color wheel. */
	private RectF mCenterRectangle = new RectF();
	/** {@code true} if the user clicked on the pointer to start the move mode. <br>
	 * {@code false} once the user stops touching the screen.
	 *
	 * @see #onTouchEvent(android.view.MotionEvent) */
	private boolean mIsMovingPointer = false;
	/** The currently set value */
	// private float mCurrentHueValue;
	/** The ARGB value of the center with the old selected color. */
	private int mCenterOldColor;
	/** Whether to show the old color in the center or not. */
	private boolean mShowCenterOldColor;
	/** The ARGB value of the center with the new selected color. */
	// private int mCenterNewColor;
	/** Number of pixels the origin of this view is moved in X- and Y-direction.
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal coordinate
	 * system. Android uses the upper left corner as origin for the View-specific
	 * coordinate system. So this is the value we use to translate from one coordinate
	 * system to the other.
	 * </p>
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 *
	 * @see #onDraw(android.graphics.Canvas) */
	private float mTranslationOffset;
	/** Distance between pointer and user touch in X-direction. */
	private float mSlopX;
	/** Distance between pointer and user touch in Y-direction. */
	private float mSlopY;
	/** The pointer's position expressed as angle (in rad). */
	private float mAngle;
	/** {@code Paint} instance used to draw the center with the old selected color. */
	private Paint mCenterOldPaint;
	/** {@code Paint} instance used to draw the center with the new selected color. */
	private Paint mCenterNewPaint;
	/** {@code Paint} instance used to draw the halo of the center selected colors. */
	private Paint mCenterHaloPaint;
	/** An array of floats that can be build into a {@code Color} <br>
	 * Where we can extract the Saturation and Value from. */
	private float[] mHSV = new float[3];
	private int mARGB;
	// private int mAlpha;
	/** {@code TouchAnywhereOnColorWheelEnabled} instance used to control <br>
	 * if the color wheel accepts input anywhere on the wheel or just <br>
	 * on the halo. */
	private boolean mTouchAnywhereOnColorWheelEnabled = true;
	// /** {@code onColorChangedListener} instance of the onColorChangedListener */
	// private OnColorChangedListener onColorChangedListener;
	/** {@code ColorPicker} instance used to control the ColorPicker. */
	private ColorPicker mColorPicker = null;

	// private OnColorSelectedListener onColorSelectedListener;
	public HueBar(Context context) {
		super(context);
		init(null, 0);
	}

	public HueBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public HueBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorPicker, defStyle, 0);
		final Resources b = getContext().getResources();
		if (isInEditMode()) {
			float density = getResources().getDisplayMetrics().density;
			mColorWheelThickness = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_wheel_thickness,
					(int) (density * 8 + 0.5));
			mColorWheelRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_wheel_radius,
					(int) (density * 124 + 0.5));
			mPreferredColorWheelRadius = mColorWheelRadius;
			mColorCenterRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_center_radius,
					(int) (density * 54 + 0.5));
			mPreferredColorCenterRadius = mColorCenterRadius;
			mColorCenterHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_center_halo_radius,
					(int) (density * 60 + 0.5));
			mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
			mColorPointerRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_pointer_radius,
					(int) (density * 14 + 0.5));
			mColorPointerHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_pointer_halo_radius,
					(int) (density * 18 + 0.5));
		} else {
			mColorWheelThickness = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_wheel_thickness,
					b.getDimensionPixelSize(R.dimen.color_wheel_thickness));
			mColorWheelRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_wheel_radius,
					b.getDimensionPixelSize(R.dimen.color_wheel_radius));
			mPreferredColorWheelRadius = mColorWheelRadius;
			mColorCenterRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_center_radius,
					b.getDimensionPixelSize(R.dimen.color_center_radius));
			mPreferredColorCenterRadius = mColorCenterRadius;
			mColorCenterHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_center_halo_radius,
					b.getDimensionPixelSize(R.dimen.color_center_halo_radius));
			mPreferredColorCenterHaloRadius = mColorCenterHaloRadius;
			mColorPointerRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_pointer_radius,
					b.getDimensionPixelSize(R.dimen.color_pointer_radius));
			mColorPointerHaloRadius = a.getDimensionPixelSize(
					R.styleable.ColorPicker_color_pointer_halo_radius,
					b.getDimensionPixelSize(R.dimen.color_pointer_halo_radius));
		}
		a.recycle();
		mAngle = (float) (-Math.PI / 2);
		Shader s = new SweepGradient(0, 0, HueBar.COLORS, null);
		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mColorWheelThickness);
		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(Color.BLACK);
		mPointerHaloPaint.setAlpha(0x50);
		mPointerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerPaint.setColor(calculateColor(mAngle));
		mCenterNewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterNewPaint.setColor(calculateColor(mAngle));
		mCenterNewPaint.setStyle(Paint.Style.FILL);
		mCenterOldPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterOldPaint.setColor(calculateColor(mAngle));
		mCenterOldPaint.setStyle(Paint.Style.FILL);
		mCenterHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCenterHaloPaint.setColor(Color.BLACK);
		mCenterHaloPaint.setAlpha(0x00);
		mARGB = calculateColor(mAngle);
		mCenterOldColor = calculateColor(mAngle);
		mShowCenterOldColor = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int intrinsicSize = 2 * (mPreferredColorWheelRadius + mColorPointerHaloRadius);
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		int width;
		int height;
		if (widthMode == MeasureSpec.EXACTLY) {
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			width = Math.min(intrinsicSize, widthSize);
		} else {
			width = intrinsicSize;
		}
		if (heightMode == MeasureSpec.EXACTLY) {
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			height = Math.min(intrinsicSize, heightSize);
		} else {
			height = intrinsicSize;
		}
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);
		mTranslationOffset = min * 0.5f;
		// fill the rectangle instances.
		mColorWheelRadius = min / 2 - mColorWheelThickness - mColorPointerHaloRadius;
		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);
		mColorCenterRadius = (int) (mPreferredColorCenterRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mColorCenterHaloRadius = (int) (mPreferredColorCenterHaloRadius * ((float) mColorWheelRadius / (float) mPreferredColorWheelRadius));
		mCenterRectangle.set(-mColorCenterRadius, -mColorCenterRadius,
				mColorCenterRadius, mColorCenterRadius);
	}

	private int ave(int s, int d, float p) {
		return s + Math.round(p * (d - s));
	}

	/** Calculate the color using the supplied angle.
	 *
	 * @param angle
	 *            The selected color's position expressed as angle (in rad).
	 * @return The ARGB value of the color on the color wheel at the specified angle. */
	private int calculateColor(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}
		if (unit <= 0) {
			return HueBar.COLORS[0];
		}
		if (unit >= 1) {
			return HueBar.COLORS[HueBar.COLORS.length - 1];
		}
		float p = unit * (HueBar.COLORS.length - 1);
		int i = (int) p;
		p -= i;
		int c0 = HueBar.COLORS[i];
		int c1 = HueBar.COLORS[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);
		return Color.argb(a, r, g, b);
	}

	/** Set the color to be highlighted by the pointer. If the instances {@code SVBar} and
	 * the {@code OpacityBar} aren't null the color will also be set to them
	 *
	 * @param color
	 *            The RGB value of the color to highlight. If this is not a color
	 *            displayed on the color wheel a very simple algorithm is used to map it
	 *            to the color wheel. The resulting color often won't look close to the
	 *            original color. This is especially true for shades of grey. You have
	 *            been warned! */
	public void setColor(int argb, float[] hsv) {
		mARGB = argb;
		mHSV[0] = hsv[0];
		mHSV[1] = hsv[1];
		mHSV[2] = hsv[2];
		mAngle = (float) Math.toRadians(-hsv[0]);
		mPointerPaint.setColor(calculateColor(mAngle));
		// mCenterOldPaint.setColor(argb);
		// mCenterNewColor = argb;
		mCenterNewPaint.setColor(argb);
		invalidate();
	}

	// /** Convert a color to an angle.
	// *
	// * @param color
	// * The RGB value of the color to "find" on the color wheel.
	// * @return The angle (in rad) the "normalized" color is displayed on the color
	// wheel. */
	// private float colorToAngle(int color) {
	// float[] colors = new float[3];
	// Color.colorToHSV(color, colors);
	// return (float) Math.toRadians(-colors[0]);
	// }
	@Override
	protected void onDraw(Canvas canvas) {
		// All of our positions are using our internal coordinate system.
		// Instead of translating
		// them we let Canvas do the work for us.
		canvas.translate(mTranslationOffset, mTranslationOffset);
		// Draw the color wheel.
		canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);
		float[] pointerPosition = calculatePointerPosition(mAngle);
		// Draw the pointer's "halo"
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mColorPointerHaloRadius, mPointerHaloPaint);
		// Draw the pointer (the currently selected color) slightly smaller on
		// top.
		canvas.drawCircle(pointerPosition[0], pointerPosition[1], mColorPointerRadius,
				mPointerPaint);
		// Draw the halo of the center colors.
		canvas.drawCircle(0, 0, mColorCenterHaloRadius, mCenterHaloPaint);
		if (mShowCenterOldColor) {
			// Draw the old selected color in the center.
			canvas.drawArc(mCenterRectangle, 90, 180, true, mCenterOldPaint);
			// Draw the new selected color in the center.
			canvas.drawArc(mCenterRectangle, 270, 180, true, mCenterNewPaint);
		} else {
			// Draw the new selected color in the center.
			canvas.drawArc(mCenterRectangle, 0, 360, true, mCenterNewPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		getParent().requestDisallowInterceptTouchEvent(true);
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Check whether the user pressed on the pointer.
			float[] pointerPosition = calculatePointerPosition(mAngle);
			float pointerPositionX = pointerPosition[0];
			float pointerPositionY = pointerPosition[1];
			if (pointerPositionX - mColorPointerHaloRadius <= x
					&& x <= pointerPositionX + mColorPointerHaloRadius
					&& pointerPositionY - mColorPointerHaloRadius <= y
					&& y <= pointerPositionY + mColorPointerHaloRadius) {
				mSlopX = x - pointerPositionX;
				mSlopY = y - pointerPositionY;
				mIsMovingPointer = true;
				// if (mColorPicker != null) {
				// int color = calculateColor(mAngle);
				// Color.colorToHSV(color, mHSV);
				// mColorPicker.setNewHue(mHSV[0]);
				// }
				// invalidate();
				break;
			}
			// Check whether the user pressed on the center.
			else if (-mColorCenterRadius <= x && x <= mColorCenterRadius
					&& -mColorCenterRadius <= y && y <= mColorCenterRadius
					&& mShowCenterOldColor) {
				if (x >= 0) {
					mCenterOldColor = mARGB;
					mCenterOldPaint.setColor(mARGB);
				} else {
					mARGB = mCenterOldColor;
					Color.colorToHSV(mARGB, mHSV);
				}
				mCenterHaloPaint.setAlpha(0x50);
				if (mColorPicker != null) {
					mColorPicker.setColor(mARGB, mHSV);
				} else {
					setColor(mARGB, mHSV);
				}
				break;
			}
			// Check whether the user pressed anywhere on the wheel.
			else if (mTouchAnywhereOnColorWheelEnabled
					&& Math.sqrt(x * x + y * y) <= mColorWheelRadius
							+ mColorPointerHaloRadius
					&& Math.sqrt(x * x + y * y) >= mColorWheelRadius
							- mColorPointerHaloRadius) {
				mIsMovingPointer = true;
				//
				mSlopX = 0;
				mSlopY = 0;
				mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);
				int pointerPaintColor = calculateColor(mAngle);
				float[] hsv = new float[3];
				Color.colorToHSV(pointerPaintColor, hsv);
				if (mColorPicker != null) {
					mColorPicker.setNewHue(hsv[0]);
				} else {
					mPointerPaint.setColor(pointerPaintColor);
					// Color.colorToHSV(mCenterNewColor, mHSV);
					mHSV[0] = hsv[0];
					setNewCenterColor(mARGB = Color.HSVToColor(mHSV));
					// mCenterNewPaint.setColor(mCenterNewColor);
				}
				//
				invalidate();
			}
			// If user did not press pointer or center, report event not handled
			else {
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsMovingPointer) {
				mAngle = (float) Math.atan2(y - mSlopY, x - mSlopX);
				int pointerPaintColor = calculateColor(mAngle);
				float[] hsv = new float[3];
				Color.colorToHSV(pointerPaintColor, hsv);
				if (mColorPicker != null) {
					mColorPicker.setNewHue(hsv[0]);
				} else {
					mPointerPaint.setColor(pointerPaintColor);
					// Color.colorToHSV(mCenterNewColor, mHSV);
					mHSV[0] = hsv[0];
					setNewCenterColor(mARGB = Color.HSVToColor(mHSV));
					// mCenterNewPaint.setColor(mCenterNewColor);
				}
				invalidate();
			}
			// If user did not press pointer or center, report event not handled
			else {
				getParent().requestDisallowInterceptTouchEvent(false);
				return false;
			}
			break;
		case MotionEvent.ACTION_UP:
			mIsMovingPointer = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return true;
	}

	/** Calculate the pointer's coordinates on the color wheel using the supplied angle.
	 *
	 * @param angle
	 *            The position of the pointer expressed as angle (in rad).
	 * @return The coordinates of the pointer's center in our internal coordinate system. */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));
		return new float[] {x, y};
	}

	/** Change the color of the center which indicates the new color.
	 *
	 * @param color
	 *            int of the color. */
	private void setNewCenterColor(int color) {
		mARGB = color;
		mCenterNewPaint.setColor(color);
		invalidate();
	}

	/** Change the color of the center which indicates the old color.
	 *
	 * @param color
	 *            int of the color. */
	private void setOldCenterColor(int color) {
		mCenterOldColor = color;
		mCenterOldPaint.setColor(color);
		invalidate();
	}

	/** Set whether the old color is to be shown in the center or not
	 *
	 * @param show
	 *            true if the old color is to be shown, false otherwise */
	public void setShowOldCenterColor(boolean show) {
		mShowCenterOldColor = show;
		invalidate();
	}

	public boolean getShowOldCenterColor() {
		return mShowCenterOldColor;
	}

	/** Adds a {@code ColorPicker} instance to the bar.
	 *
	 * @param picker */
	public void setColorPicker(ColorPicker picker) {
		mColorPicker = picker;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		Bundle state = new Bundle();
		state.putParcelable(HueBar.STATE_PARENT, superState);
		state.putBoolean(HueBar.STATE_SHOW_OLD_COLOR, mShowCenterOldColor);
		state.putInt(HueBar.STATE_ARGB, mARGB);
		state.putFloat(HueBar.STATE_HUE, mHSV[0]);
		state.putFloat(HueBar.STATE_SATURATION, mHSV[1]);
		state.putFloat(HueBar.STATE_VALUE, mHSV[2]);
		state.putInt(HueBar.STATE_OLD_COLOR, mCenterOldColor);
		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;
		Parcelable superState = savedState.getParcelable(HueBar.STATE_PARENT);
		super.onRestoreInstanceState(superState);
		mShowCenterOldColor = savedState.getBoolean(HueBar.STATE_SHOW_OLD_COLOR);
		mARGB = savedState.getInt(HueBar.STATE_ARGB);
		mHSV[0] = savedState.getFloat(HueBar.STATE_HUE);
		mHSV[1] = savedState.getFloat(HueBar.STATE_SATURATION);
		mHSV[2] = savedState.getFloat(HueBar.STATE_VALUE);
		setColor(mARGB, mHSV);
		// setNewCenterColor(mARGB);
		setOldCenterColor(savedState.getInt(HueBar.STATE_OLD_COLOR));
	}

	public void setTouchAnywhereOnColorWheelEnabled(
			boolean TouchAnywhereOnColorWheelEnabled) {
		mTouchAnywhereOnColorWheelEnabled = TouchAnywhereOnColorWheelEnabled;
	}

	public boolean getTouchAnywhereOnColorWheel() {
		return mTouchAnywhereOnColorWheelEnabled;
	}
}
