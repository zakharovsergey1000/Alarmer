package biz.advancedcalendar.views.accessories;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;

/** A text view which, when pressed or activated, displays a blue circle around the text. */
public class RectangularCheckBox extends android.support.v7.widget.AppCompatTextView {
	private static final int SELECTED_CIRCLE_ALPHA = 60;
	Paint mCirclePaint = new Paint();
	private int mCircleColor;
	private String mItemIsSelectedText;
	private boolean mDrawCircle;
	private RectF rectF;
	private InformationUnit informationUnit;
	private Context context;
	private Resources resources;
	private int rowIndex;
	private Point rowAndColumnIndex;

	public RectangularCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	private void init() {
		resources = context.getResources();
		mCircleColor = resources.getColor(com.android.supportdatetimepicker.R.color.blue);
		mItemIsSelectedText = context.getResources().getString(
				com.android.supportdatetimepicker.R.string.item_is_selected);
		mCirclePaint.setFakeBoldText(true);
		mCirclePaint.setAntiAlias(true);
		mCirclePaint.setColor(mCircleColor);
		mCirclePaint.setTextAlign(Align.CENTER);
		mCirclePaint.setStyle(Style.FILL);
		mCirclePaint.setAlpha(RectangularCheckBox.SELECTED_CIRCLE_ALPHA);
		rectF = new RectF();
	}

	public void setChecked(boolean drawCircle) {
		if (mDrawCircle != drawCircle) {
			mDrawCircle = drawCircle;
			invalidate();
		}
	}

	public boolean isChecked() {
		return mDrawCircle;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mDrawCircle) {
			final int width = getWidth();
			final int height = getHeight();
			rectF.set(0, 0, width, height);
			int radius = height / 4;
			canvas.drawRoundRect(rectF, radius, radius, mCirclePaint);
		}
	}

	public InformationUnit getInformationUnit() {
		return informationUnit;
	}

	public void setInformationUnit(InformationUnit informationUnit) {
		this.informationUnit = informationUnit;
		InformationUnitSelector informationUnitSelector = informationUnit
				.getInformationUnitSelector();
		String descriptionText;
		if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
			descriptionText = informationUnit.getWhateverDelimiterString();
			if (descriptionText.trim().isEmpty()) {
				informationUnit.setWhateverDelimiterString(" ");
				descriptionText = resources
						.getString(R.string.information_unit_description_text_space_character);
			}
		} else {
			int textId = informationUnitSelector.getTextId();
			descriptionText = resources.getString(textId);
		}
		setText(descriptionText);
	}

	public int getRowIndex(TableLayout tableLayout) {
		int rowsCount = tableLayout.getChildCount();
		int rowIndex = -1;
		if (rowsCount > 0) {
			RadioGroup radioGroupInsideTableRow = (RadioGroup) getParent();
			TableRow tableRow = (TableRow) radioGroupInsideTableRow.getParent();
			rowIndex = tableLayout.indexOfChild(tableRow);
		}
		return rowIndex;
	}

	public Point getRowAndColumnIndex(TableLayout tableLayout) {
		Point rowAndColumnIndex = null;
		int rowsCount = tableLayout.getChildCount();
		if (rowsCount > 0) {
			RadioGroup radioGroupInsideTableRow = (RadioGroup) getParent();
			int columnIndex = radioGroupInsideTableRow.indexOfChild(this);
			TableRow tableRow = (TableRow) radioGroupInsideTableRow.getParent();
			int rowIndex = tableLayout.indexOfChild(tableRow);
			rowAndColumnIndex = new Point(rowIndex, columnIndex);
		}
		return rowAndColumnIndex;
	}
}
