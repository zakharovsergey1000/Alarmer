package biz.advancedcalendar.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MonthViewWeekDaysHeader extends ViewGroup {
	private WeekViewDayHeaderClickedListener mDayHeaderClickedListener;
	// private static final int WEEK_HEADER_HEIGHT_DIP = 30;
	// private static final int WEEK_HEADER_TEXT_SIZE_SP = 15;
	private static final int WEEK_HEADER_AREA_BACKGROUND_COLOR = 0xFFFFFFFF;
	// private static final int WEEK_HEADER_AREA_COLOR = 0xFF21DFDF;
	// private static final int WEEK_HEADER_TEXT_COLOR = 0xFF11ED4B;
	// private int mWeekHeaderHeightPx;
	// private int mWeekHeaderTextSizePx;
	private List<Integer> mDayHeadersCoords;
	private List<TextView> mDayHeaderButtons = new ArrayList<TextView>();

	// private int mHourHeaderWidthPx;
	// private long mWeekStartTime;
	// private SimpleDateFormat mSimpleDateFormat;
	// private ColorStateList mDefaultTextColorStateList;
	// private Drawable mDefaultBackground;
	/** Constructor
	 *
	 * @param context */
	public MonthViewWeekDaysHeader(Context context) {
		this(context, null, 0);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle */
	public MonthViewWeekDaysHeader(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/** Constructor
	 *
	 * @param context
	 *            The context
	 * @param attrs
	 *            Attributes
	 * @param defStyle
	 *            defStyle */
	public MonthViewWeekDaysHeader(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundColor(MonthViewWeekDaysHeader.WEEK_HEADER_AREA_BACKGROUND_COLOR);
		// mSimpleDateFormat = new SimpleDateFormat("EEE d", Locale.getDefault());
	}

	public void setDayHeaderClickedListener(
			WeekViewDayHeaderClickedListener dayHeaderClickedListener) {
		mDayHeaderClickedListener = dayHeaderClickedListener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		//
		int proposedWidth = 48 * 7;
		if (mDayHeadersCoords != null && mDayHeadersCoords.size() > 0) {
			proposedWidth = mDayHeadersCoords.get(mDayHeadersCoords.size() - 1)
					- mDayHeadersCoords.get(0);
		}
		int dayHeadersCoordsSize = mDayHeadersCoords != null ? mDayHeadersCoords.size()
				: 0;
		int maxHeight = 0;
		for (int i = 0; i < dayHeadersCoordsSize; i += 2) {
			TextView b = mDayHeaderButtons.get(i / 2);
			int x1 = mDayHeadersCoords.get(i);
			int x2 = mDayHeadersCoords.get(i + 1);
			b.measure(MeasureSpec.EXACTLY | x2 - x1, MeasureSpec.UNSPECIFIED);
			if (maxHeight < b.getMeasuredHeight()) {
				maxHeight = b.getMeasuredHeight();
			}
		}
		for (int i = 0; i < dayHeadersCoordsSize; i += 2) {
			TextView b = mDayHeaderButtons.get(i / 2);
			int x1 = mDayHeadersCoords.get(i);
			int x2 = mDayHeadersCoords.get(i + 1);
			b.measure(MeasureSpec.EXACTLY | x2 - x1, MeasureSpec.EXACTLY | maxHeight);
		}
		switch (specModeWidth) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (proposedWidth > specSizeWidth) {
				proposedWidth = specSizeWidth;
			}
			break;
		case MeasureSpec.EXACTLY:
			proposedWidth = specSizeWidth;
			break;
		}
		//
		int proposedHeight = maxHeight;
		//
		switch (specModeHeight) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (proposedHeight > specSizeHeight) {
				proposedHeight = specSizeHeight;
			}
			break;
		case MeasureSpec.EXACTLY:
			proposedHeight = specSizeHeight;
			break;
		}
		setMeasuredDimension(proposedWidth, proposedHeight);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top,
			final int right, final int bottom) {
		if (mDayHeadersCoords != null) {
			int childViewBottom = getHeight();
			int offset = mDayHeadersCoords.get(0);
			int dayHeadersCoordsSize = mDayHeadersCoords.size();
			for (int i = 0; i < dayHeadersCoordsSize; i += 2) {
				TextView b = mDayHeaderButtons.get(i / 2);
				int x1 = mDayHeadersCoords.get(i);
				int x2 = mDayHeadersCoords.get(i + 1);
				b.layout(x1 - offset, 0, x2 - offset, childViewBottom);
			}
		}
	}

	public void init(int firstDayOfWeek, List<Integer> dayHeadersCoords) {
		removeAllViews();
		mDayHeadersCoords = dayHeadersCoords;
		int buttonsCount = mDayHeadersCoords.size() / 2;
		for (int i = 0; i < buttonsCount; i++) {
			TextView weekDay = new TextView(getContext());
			LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
					LayoutParams.FILL_PARENT);
			weekDay.setLayoutParams(lp);
			weekDay.setPadding(0, 0, 0, 0);
			weekDay.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
			weekDay.setGravity(Gravity.CENTER);
			weekDay.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mDayHeaderClickedListener != null) {
						mDayHeaderClickedListener.onDayHeaderClicked((Long) v.getTag());
					}
				}
			});
			mDayHeaderButtons.add(weekDay);
			addView(weekDay);
		}
		setFirstDayOfWeek(firstDayOfWeek);
		requestLayout();
	}

	public void setFirstDayOfWeek(int firstDayOfWeek) {
		int buttonsCount = getChildCount();
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
		for (int i = 0; i < buttonsCount; i++) {
			TextView weekDay = (TextView) getChildAt(i);
			int dayOfWeek = (firstDayOfWeek + i) % 7;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}
			calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
			String text = sdf.format(new Date(calendar.getTimeInMillis())).toUpperCase();
			weekDay.setText(text);
		}
	}
}
