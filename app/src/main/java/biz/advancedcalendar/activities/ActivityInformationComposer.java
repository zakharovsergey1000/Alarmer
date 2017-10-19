package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import biz.advancedcalendar.views.accessories.InformationComposer;
import biz.advancedcalendar.views.accessories.InformationUnit;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrder;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrdersHolder;
import biz.advancedcalendar.views.accessories.RectangularCheckBox;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ActivityInformationComposer extends AppCompatActivity {
	private static final String KEY_INFORMATION_UNIT_MATRIX = "KEY_INFORMATION_UNIT_MATRIX";
	private static final String KEY_INFORMATION_UNIT_SORT_ORDERS_HOLDER = "KEY_INFORMATION_UNIT_SORT_ORDERS_HOLDER";
	private static final String KEY_LAST_SELECTED_CHECKBOX_ID = "KEY_LAST_SELECTED_CHECKBOX_ID";
	private static final String KEY_LAST_SELECTED_RADIOBUTTON_ID = "KEY_LAST_SELECTED_RADIOBUTTON_ID";
	public static final String EXTRA_START_POSITION = "EXTRA_START_POSITION";
	private InformationComposer informationComposer;
	private InformationUnitMatrix informationUnitMatrix;
	private InformationUnitSortOrdersHolder informationUnitSortOrdersHolder;
	private InformationUnitSelectorComparatorByValue informationUnitSelectorComparatorByValue;
	private TableLayout mTableLayout;
	private RadioGroup radioGroupInformationUnits;
	private Activity activity;
	private Resources resources;
	private DragShadowBuilder dragShadowBuilder;
	private RectangularCheckBoxOnLongClickListener rectangularCheckBoxOnLongClickListener;
	private RectangularCheckBoxOnDragListener rectangularCheckBoxOnDragListener;
	private RadioButtonInformationUnitOnLongClickListener radioButtonInformationUnitOnLongClickListener;
	private RadioButtonInformationUnitOnDragListener radioButtonInformationUnitOnDragListener;
	private OnClickListener rectangularCheckBoxOnClickListener;
	private OnClickListener buttonReplaceOnClickListener;
	private OnClickListener buttonInsertOnClickListener;
	private OnClickListener buttonDeleteOnClickListener;
	private RadioGroupOnCheckedChangeListener radioGroupOnCheckedChangeListener;
	RectangularCheckBox lastSelectedCheckBox;
	RadioButton lastSelectedRadioButton;
	private int lastSelectedCheckboxId;
	private int lastSelectedRadioButtonId;
	private int maxId;
	private ListView mListView;
	private EditText editText;
	private LayoutInflater layoutInflater;
	private Toolbar mToolbar;

	private static class InformationUnitSelectorComparatorByValue implements
			Comparator<InformationUnitSelector>, Serializable {
		private static final long serialVersionUID = 2L;

		@Override
		public int compare(InformationUnitSelector lhs, InformationUnitSelector rhs) {
			if (lhs == null) {
				return 1;
			} else if (rhs == null) {
				return -1;
			} else if (lhs.getValue() < rhs.getValue()) {
				return -1;
			} else if (lhs.getValue() > rhs.getValue()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private class RectangularCheckBoxOnLongClickListener implements OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			v.performClick();
			DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
			RectangularCheckBox rectangularCheckBox = (RectangularCheckBox) v;
			InformationUnit informationUnit = lastSelectedCheckBox.getInformationUnit();
			InformationUnitSelector informationUnitSelector = informationUnit
					.getInformationUnitSelector();
			CharSequence text;
			if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
				text = informationUnit.getWhateverDelimiterString();
				String trim = text.toString().trim();
				if (trim.isEmpty()) {
					text = trim;
				}
			} else {
				text = rectangularCheckBox.getText();
			}
			ClipData clipData = ClipData.newPlainText(text, text);
			v.startDrag(clipData, shadowBuilder, null, 0);
			return true;
		}
	}

	private class RectangularCheckBoxOnDragListener implements OnDragListener {
		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:
				EditText editText = (EditText) v;
				ClipData clipData = event.getClipData();
				int size = clipData.getItemCount();
				if (size > 0) {
					Item item = clipData.getItemAt(0);
					CharSequence text = item.getText();
					editText.setText(text);
					editText.setSelection(text.length());
				}
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				break;
			default:
				break;
			}
			return true;
		}
	}

	private class RadioButtonInformationUnitOnLongClickListener implements
			OnLongClickListener {
		@Override
		public boolean onLongClick(View v) {
			v.performClick();
			DragShadowBuilder shadowBuilder = new DragShadowBuilder(v);
			RadioButton radioButtonInformationUnit = (RadioButton) v;
			RadioGroup radioGroupInformationUnits = (RadioGroup) radioButtonInformationUnit
					.getParent();
			int position = radioGroupInformationUnits
					.indexOfChild(radioButtonInformationUnit);
			// CharSequence text = radioButtonInformationUnit.getText();
			// ClipData clipData = ClipData.newPlainText(text, text);
			// Intent intent = new Intent();
			// intent.putExtra(ActivityInformationComposer.EXTRA_START_POSITION,
			// position);
			// ClipData.Item clipDataItem = new Item(intent);
			// clipData.addItem(clipDataItem);
			v.startDrag(null, shadowBuilder, position, 0);
			return true;
		}
	}

	private class RadioButtonInformationUnitOnDragListener implements OnDragListener {
		@Override
		public boolean onDrag(View v, DragEvent event) {
			int action = event.getAction();
			switch (action) {
			case DragEvent.ACTION_DRAG_STARTED:
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				break;
			case DragEvent.ACTION_DRAG_LOCATION:
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				break;
			case DragEvent.ACTION_DROP:
				RadioButton radioButtonInformationUnit = (RadioButton) v;
				RadioGroup radioGroupInformationUnits = (RadioGroup) radioButtonInformationUnit
						.getParent();
				int startPosition = (Integer) event.getLocalState();
				int newPosition = radioGroupInformationUnits
						.indexOfChild(radioButtonInformationUnit);
				int viewHeight = v.getHeight();
				int center = viewHeight / 2;
				float coord = event.getY();
				if (newPosition <= startPosition) {
					if (newPosition < startPosition) {
						if (coord > center) {
							newPosition = newPosition + 1;
						}
					}
				} else {
					if (coord < center) {
						newPosition = newPosition - 1;
					}
				}
				if (newPosition != startPosition) {
					RadioButton radioButtonBeingMoved = (RadioButton) radioGroupInformationUnits
							.getChildAt(startPosition);
					radioGroupInformationUnits.removeViewAt(startPosition);
					radioGroupInformationUnits
							.addView(radioButtonBeingMoved, newPosition);
					ArrayList<InformationUnitSortOrder> informationUnitSortOrders = informationUnitSortOrdersHolder
							.getInformationUnitSortOrders();
					InformationUnitSortOrder informationUnitSortOrder = informationUnitSortOrders
							.remove(startPosition);
					informationUnitSortOrders.add(newPosition, informationUnitSortOrder);
				}
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				break;
			default:
				break;
			}
			return true;
		}
	}

	private class RadioGroupOnCheckedChangeListener implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			lastSelectedRadioButtonId = checkedId;
			lastSelectedRadioButton = (RadioButton) group.findViewById(checkedId);
		}
	}

	private class RectangularCheckBoxOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			RectangularCheckBox checkBox = (RectangularCheckBox) v;
			if (checkBox != lastSelectedCheckBox) {
				lastSelectedCheckBox.setChecked(false);
				lastSelectedCheckBox = checkBox;
				boolean isChecked = checkBox.isChecked();
				checkBox.setChecked(!isChecked);
				RadioGroup radioGroupInformationUnits = (RadioGroup) v.getParent();
				int i = radioGroupInformationUnits.indexOfChild(checkBox);
				if (isChecked) {
				} else {
				}
			} else {
			}
		}
	}

	private class ButtonReplaceOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (lastSelectedCheckBox != null) {
				InformationUnit informationUnit = lastSelectedCheckBox
						.getInformationUnit();
				int buttonId = ((Button) v).getId();
				switch (buttonId) {
				case R.id.button_replace_information_unit:
					InformationUnitSelector informationUnitSelector = InformationUnitSelector
							.fromInt((byte) lastSelectedRadioButtonId);
					String descriptionText;
					if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
						descriptionText = editText.getText().toString();
						informationUnit.setWhateverDelimiterString(descriptionText);
						lastSelectedCheckBox
								.setOnLongClickListener(rectangularCheckBoxOnLongClickListener);
					} else {
						InformationUnitSelector previousInformationUnitSelector = informationUnit
								.getInformationUnitSelector();
						if (previousInformationUnitSelector == InformationUnitSelector.ANY_STRING) {
							lastSelectedCheckBox.setOnLongClickListener(null);
						}
					}
					informationUnit.setInformationUnitSelector(informationUnitSelector);
					lastSelectedCheckBox.setInformationUnit(informationUnit);
					break;
				case R.id.button_replace_text:
					informationUnitSelector = informationUnit
							.getInformationUnitSelector();
					if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
						lastSelectedCheckBox.setInformationUnit(informationUnit);
					}
					break;
				default:
					break;
				}
			} else {
			}
		}
	}

	private class ButtonInsertOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Button button = (Button) v;
			int buttonId = button.getId();
			InformationUnitSelector informationUnitSelector = InformationUnitSelector
					.fromInt((byte) lastSelectedRadioButtonId);
			InformationUnit informationUnit = new InformationUnit(informationUnitSelector);
			String descriptionText;
			if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
				descriptionText = editText.getText().toString();
				if (descriptionText.trim().isEmpty()) {
					informationUnit.setWhateverDelimiterString(" ");
					descriptionText = resources
							.getString(R.string.information_unit_description_text_space_character);
				} else {
					informationUnit.setWhateverDelimiterString(descriptionText);
				}
			} else {
				int textId = informationUnitSelector.getTextId();
				descriptionText = resources.getString(textId);
			}
			int rowsCount = mTableLayout.getChildCount();
			if (rowsCount > 0) {
				RadioGroup radioGroupInsideTableRow = (RadioGroup) lastSelectedCheckBox
						.getParent();
				TableRow tableRow = (TableRow) radioGroupInsideTableRow.getParent();
				int rowIndex = mTableLayout.indexOfChild(tableRow);
				RectangularCheckBox checkBox = (RectangularCheckBox) layoutInflater
						.inflate(R.layout.information_unit_checkbox,
								radioGroupInsideTableRow, false);
				checkBox.setId(maxId);
				maxId = maxId + 1;
				checkBox.setInformationUnit(informationUnit);
				if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
					checkBox.setOnLongClickListener(rectangularCheckBoxOnLongClickListener);
				}
				checkBox.setOnClickListener(rectangularCheckBoxOnClickListener);
				switch (buttonId) {
				case R.id.button_insert_to_left:
				case R.id.button_insert_to_right:
					int columnIndex = radioGroupInsideTableRow
							.indexOfChild(lastSelectedCheckBox);
					ArrayList<InformationUnit> informationUnits = informationComposer
							.getInformationUnits(rowIndex);
					if (buttonId == R.id.button_insert_to_right) {
						columnIndex = columnIndex + 1;
						radioGroupInsideTableRow.addView(checkBox, columnIndex);
						informationUnits.add(columnIndex, informationUnit);
					} else {
						radioGroupInsideTableRow.addView(checkBox, columnIndex);
						informationUnits.add(columnIndex, informationUnit);
						columnIndex = columnIndex + 1;
					}
					checkBox = (RectangularCheckBox) radioGroupInsideTableRow
							.getChildAt(columnIndex);
					break;
				case R.id.button_insert_to_up:
				case R.id.button_insert_to_down:
					tableRow = (TableRow) layoutInflater.inflate(
							R.layout.activity_information_composer_tablerow,
							mTableLayout, false);
					radioGroupInsideTableRow = (RadioGroup) tableRow.getChildAt(0);
					radioGroupInsideTableRow.addView(checkBox);
					if (buttonId == R.id.button_insert_to_down) {
						rowIndex = rowIndex + 1;
					} else {
						checkBox = lastSelectedCheckBox;
					}
					mTableLayout.addView(tableRow, rowIndex);
					informationUnits = new ArrayList<InformationUnit>();
					informationUnits.add(informationUnit);
					informationComposer.addInformationUnits(informationUnits, rowIndex);
					break;
				default:
					break;
				}
				lastSelectedCheckBox.setChecked(false);
				lastSelectedCheckBox = checkBox;
				lastSelectedCheckBox.setChecked(true);
			} else {
				ArrayList<InformationUnit> informationUnits = new ArrayList<InformationUnit>();
				informationUnits.add(informationUnit);
				informationComposer.addInformationUnits(informationUnits);
				TableRow tableRow = (TableRow) layoutInflater.inflate(
						R.layout.activity_information_composer_tablerow, mTableLayout,
						false);
				RadioGroup radioGroupInsideTableRow = (RadioGroup) tableRow.getChildAt(0);
				lastSelectedCheckBox = (RectangularCheckBox) layoutInflater.inflate(
						R.layout.information_unit_checkbox, radioGroupInsideTableRow,
						false);
				lastSelectedCheckBox.setId(maxId);
				maxId = maxId + 1;
				lastSelectedCheckBox.setInformationUnit(informationUnit);
				if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
					lastSelectedCheckBox
							.setOnLongClickListener(rectangularCheckBoxOnLongClickListener);
				}
				lastSelectedCheckBox
						.setOnClickListener(rectangularCheckBoxOnClickListener);
				lastSelectedCheckBox.setChecked(true);
				radioGroupInsideTableRow.addView(lastSelectedCheckBox);
				mTableLayout.addView(tableRow);
			}
		}
	}

	private class ButtonDeleteOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Button button = (Button) v;
			int buttonId = button.getId();
			int rowsCount = mTableLayout.getChildCount();
			if (rowsCount > 0) {
				RadioGroup radioGroupInsideTableRow = (RadioGroup) lastSelectedCheckBox
						.getParent();
				TableRow tableRow = (TableRow) radioGroupInsideTableRow.getParent();
				int rowIndex = mTableLayout.indexOfChild(tableRow);
				switch (buttonId) {
				case R.id.button_delete:
					int index = radioGroupInsideTableRow
							.indexOfChild(lastSelectedCheckBox);
					radioGroupInsideTableRow.removeViewAt(index);
					ArrayList<InformationUnit> informationUnits = informationComposer
							.getInformationUnits(rowIndex);
					informationUnits.remove(index);
					int size = radioGroupInsideTableRow.getChildCount();
					if (size > 0) {
						if (index >= size) {
							index = index - 1;
						} else {
							if (index > 0) {
								index = index - 1;
							}
						}
						lastSelectedCheckBox = (RectangularCheckBox) radioGroupInsideTableRow
								.getChildAt(index);
						lastSelectedCheckBox.setChecked(true);
					} else {
						mTableLayout.removeViewAt(rowIndex);
						informationComposer.removeInformationUnitRow(rowIndex);
						rowsCount = mTableLayout.getChildCount();
						if (rowsCount > 0) {
							if (rowIndex >= rowsCount) {
								rowIndex = rowIndex - 1;
							} else {
								if (rowIndex > 0) {
									rowIndex = rowIndex - 1;
								}
							}
							tableRow = (TableRow) mTableLayout.getChildAt(rowIndex);
							radioGroupInsideTableRow = (RadioGroup) tableRow
									.getChildAt(0);
							lastSelectedCheckBox = (RectangularCheckBox) radioGroupInsideTableRow
									.getChildAt(0);
							lastSelectedCheckBox.setChecked(true);
						} else {
							lastSelectedCheckBox = null;
						}
					}
					break;
				default:
					break;
				}
			} else {
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_information_composer);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		activity = this;
		resources = activity.getResources();
		layoutInflater = getLayoutInflater();
		editText = (EditText) activity
				.findViewById(R.id.activity_information_composer_edittext_whatever_delimiter_string);
		mTableLayout = (TableLayout) findViewById(R.id.activity_information_composer_tablelayout);
		mTableLayout.setStretchAllColumns(true);
		if (savedInstanceState == null) {
			informationUnitMatrix = getIntent().getParcelableExtra(
					CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_MATRIX);
			informationUnitSortOrdersHolder = getIntent().getParcelableExtra(
					CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_SORT_ORDERS_HOLDER);
			lastSelectedCheckboxId = getIntent().getIntExtra(
					CommonConstants.INTENT_EXTRA_LAST_SELECTED_CHECKBOX_ID,
					CommonConstants.INITIAL_VALUE_FOR_RECTANGULAR_CHECK_BOX_IDS);
			lastSelectedRadioButtonId = getIntent().getIntExtra(
					CommonConstants.INTENT_EXTRA_LAST_SELECTED_RADIOBUTTON_ID,
					InformationUnitSelector.ANY_STRING.getValue());
		} else {
			informationUnitMatrix = savedInstanceState
					.getParcelable(ActivityInformationComposer.KEY_INFORMATION_UNIT_MATRIX);
			informationUnitSortOrdersHolder = savedInstanceState
					.getParcelable(ActivityInformationComposer.KEY_INFORMATION_UNIT_SORT_ORDERS_HOLDER);
			lastSelectedCheckboxId = savedInstanceState
					.getInt(ActivityInformationComposer.KEY_LAST_SELECTED_CHECKBOX_ID);
			lastSelectedRadioButtonId = savedInstanceState
					.getInt(ActivityInformationComposer.KEY_LAST_SELECTED_RADIOBUTTON_ID);
		}
		informationComposer = new InformationComposer(informationUnitMatrix);
		informationUnitSelectorComparatorByValue = new InformationUnitSelectorComparatorByValue();
		rectangularCheckBoxOnLongClickListener = new RectangularCheckBoxOnLongClickListener();
		rectangularCheckBoxOnDragListener = new RectangularCheckBoxOnDragListener();
		radioButtonInformationUnitOnLongClickListener = new RadioButtonInformationUnitOnLongClickListener();
		radioButtonInformationUnitOnDragListener = new RadioButtonInformationUnitOnDragListener();
		editText.setOnDragListener(rectangularCheckBoxOnDragListener);
		rectangularCheckBoxOnClickListener = new RectangularCheckBoxOnClickListener();
		buttonReplaceOnClickListener = new ButtonReplaceOnClickListener();
		buttonInsertOnClickListener = new ButtonInsertOnClickListener();
		buttonDeleteOnClickListener = new ButtonDeleteOnClickListener();
		radioGroupOnCheckedChangeListener = new RadioGroupOnCheckedChangeListener();
		Button button = (Button) activity
				.findViewById(R.id.button_replace_information_unit);
		button.setOnClickListener(buttonReplaceOnClickListener);
		button = (Button) activity.findViewById(R.id.button_replace_text);
		button.setOnClickListener(buttonReplaceOnClickListener);
		button = (Button) activity.findViewById(R.id.button_insert_to_left);
		button.setOnClickListener(buttonInsertOnClickListener);
		button = (Button) activity.findViewById(R.id.button_insert_to_right);
		button.setOnClickListener(buttonInsertOnClickListener);
		button = (Button) activity.findViewById(R.id.button_insert_to_up);
		button.setOnClickListener(buttonInsertOnClickListener);
		button = (Button) activity.findViewById(R.id.button_insert_to_down);
		button.setOnClickListener(buttonInsertOnClickListener);
		button = (Button) activity.findViewById(R.id.button_delete);
		button.setOnClickListener(buttonDeleteOnClickListener);
		radioGroupInformationUnits = (RadioGroup) activity
				.findViewById(R.id.activity_information_composer_radiogroup_information_units);
		initializeView();
	}

	private void initializeView() {
		maxId = 0;
		List<InformationUnitSelector> list = InformationUnitSelector.getList();
		// mListView = (ListView)
		// findViewById(R.id.activity_information_composer_listview_information_units);
		// mListView.setVisibility(View.GONE);
		// InformationUnitSelectorArrayAdapter arrayAdapter = new
		// InformationUnitSelectorArrayAdapter(
		// this, android.R.layout.simple_list_item_single_choice, list);
		// mListView.setAdapter(arrayAdapter);
		// mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		// mListView.setItemChecked(lastSelectedRadioButtonId - 1, true);
		Set<InformationUnitSelector> existing = new HashSet<InformationUnitSelector>();
		ArrayList<InformationUnitSortOrder> informationUnitSortOrders = informationUnitSortOrdersHolder
				.getInformationUnitSortOrders();
		for (InformationUnitSortOrder informationUnitSortOrder : informationUnitSortOrders) {
			InformationUnitSelector informationUnitSelector = InformationUnitSelector
					.fromInt((byte) informationUnitSortOrder.getSortOrder());
			existing.add(informationUnitSelector);
			RadioButton radioButton = createRadioButton(informationUnitSelector);
			radioGroupInformationUnits.addView(radioButton);
		}
		SortedSet<InformationUnitSelector> rest = new TreeSet<InformationUnitSelector>();
		rest.addAll(InformationUnitSelector.getList());
		rest.removeAll(existing);
		int index = existing.size();
		for (InformationUnitSelector informationUnitSelector : rest) {
			RadioButton radioButton = createRadioButton(informationUnitSelector);
			radioGroupInformationUnits.addView(radioButton);
			InformationUnitSortOrder informationUnitSortOrder = new InformationUnitSortOrder(
					index, informationUnitSelector.getValue());
			index = index + 1;
			informationUnitSortOrders.add(informationUnitSortOrder);
		}
		lastSelectedRadioButton = (RadioButton) radioGroupInformationUnits
				.findViewById(lastSelectedRadioButtonId);
		lastSelectedRadioButton.setChecked(true);
		radioGroupInformationUnits
				.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
		mTableLayout.removeAllViews();
		int rowsCount = informationComposer.getRowsCount();
		for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
			TableRow tableRow = (TableRow) layoutInflater.inflate(
					R.layout.activity_information_composer_tablerow, mTableLayout, false);
			RadioGroup radioGroupInsideTableRow = (RadioGroup) tableRow.getChildAt(0);
			ArrayList<InformationUnit> informationUnits = informationComposer
					.getInformationUnits(rowIndex);
			int size = informationUnits.size();
			for (int i = 0; i < size; i++) {
				RectangularCheckBox checkBox = (RectangularCheckBox) layoutInflater
						.inflate(R.layout.information_unit_checkbox,
								radioGroupInsideTableRow, false);
				checkBox.setId(maxId);
				maxId = maxId + 1;
				InformationUnit informationUnit = informationUnits.get(i);
				checkBox.setInformationUnit(informationUnit);
				InformationUnitSelector informationUnitSelector = informationUnit
						.getInformationUnitSelector();
				if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
					checkBox.setOnLongClickListener(rectangularCheckBoxOnLongClickListener);
				}
				checkBox.setOnClickListener(rectangularCheckBoxOnClickListener);
				radioGroupInsideTableRow.addView(checkBox);
			}
			mTableLayout.addView(tableRow);
		}
		lastSelectedCheckBox = (RectangularCheckBox) mTableLayout
				.findViewById(lastSelectedCheckboxId);
		if (lastSelectedCheckBox == null) {
			rowsCount = mTableLayout.getChildCount();
			if (rowsCount > 0) {
				TableRow tableRow = (TableRow) mTableLayout.getChildAt(0);
				RadioGroup radioGroupInsideTableRow = (RadioGroup) tableRow.getChildAt(0);
				lastSelectedCheckBox = (RectangularCheckBox) radioGroupInsideTableRow
						.getChildAt(0);
			}
		}
		if (lastSelectedCheckBox != null) {
			lastSelectedCheckBox.setChecked(true);
		}
	}

	private RadioButton createRadioButton(InformationUnitSelector informationUnitSelector) {
		RadioButton radioButton = (RadioButton) layoutInflater.inflate(
				R.layout.support_time_selection_radiobutton, radioGroupInformationUnits,
				false);
		radioButton.setText(informationUnitSelector.getTextId());
		byte id = informationUnitSelector.getValue();
		if (maxId <= id) {
			maxId = id + 1;
		}
		radioButton.setId(id);
		radioButton.setOnLongClickListener(radioButtonInformationUnitOnLongClickListener);
		radioButton.setOnDragListener(radioButtonInformationUnitOnDragListener);
		return radioButton;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem;
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_OK, 2, getResources()
				.getString(R.string.action_done));
		menuItem.setIcon(R.drawable.ic_done_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 1, getResources()
				.getString(R.string.action_cancel));
		menuItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_PICK_PREDEFINED_COLOR, 0,
				getResources().getString(R.string.action_pick_predefined_color));
		menuItem.setIcon(R.drawable.ic_view_module_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case CommonConstants.MENU_ID_OK:
			Intent intent = getIntent();
			intent.putExtra(CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_MATRIX,
					informationUnitMatrix);
			ArrayList<InformationUnitSortOrder> informationUnitSortOrders = informationUnitSortOrdersHolder
					.getInformationUnitSortOrders();
			int index = 0;
			for (InformationUnitSortOrder informationUnitSortOrder : informationUnitSortOrders) {
				informationUnitSortOrder.setIndex(index);
				index = index + 1;
			}
			intent.putExtra(
					CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_SORT_ORDERS_HOLDER,
					informationUnitSortOrdersHolder);
			intent.putExtra(
					CommonConstants.INTENT_EXTRA_IS_INFORMATION_UNITS_SORT_ORDER_CHANGED,
					true);
			setResult(Activity.RESULT_OK, intent);
			finish();
			return true;
		case CommonConstants.MENU_ID_PICK_PREDEFINED_COLOR:
			// intent = new Intent(this, ActivityPredefinedColorPicker.class);
			// startActivityForResult(intent,
			// CommonConstants.REQUEST_CODE_PICK_PREDEFINED_COLOR);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(
				ActivityInformationComposer.KEY_INFORMATION_UNIT_MATRIX,
				informationUnitMatrix);
		savedInstanceState.putParcelable(
				ActivityInformationComposer.KEY_INFORMATION_UNIT_SORT_ORDERS_HOLDER,
				informationUnitSortOrdersHolder);
		savedInstanceState.putInt(
				ActivityInformationComposer.KEY_LAST_SELECTED_CHECKBOX_ID,
				lastSelectedCheckBox.getId());
		savedInstanceState.putInt(
				ActivityInformationComposer.KEY_LAST_SELECTED_RADIOBUTTON_ID,
				lastSelectedRadioButton.getId());
		super.onSaveInstanceState(savedInstanceState);
	}

	private class InformationUnitSelectorArrayAdapter extends
			ArrayAdapter<InformationUnitSelector> {
		List<InformationUnitSelector> objects;

		public InformationUnitSelectorArrayAdapter(Context context,
				int textViewResourceId, List<InformationUnitSelector> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckedTextView textView;
			if (convertView == null) {
				textView = (CheckedTextView) getLayoutInflater().inflate(
						android.R.layout.simple_list_item_single_choice, parent, false);
			} else {
				textView = (CheckedTextView) convertView;
			}
			InformationUnitSelector informationUnitSelector = objects.get(position);
			textView.setText(informationUnitSelector.getTextId());
			return textView;
		}

		@Override
		public long getItemId(int position) {
			InformationUnitSelector item = objects.get(position);
			return item.getValue();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	public InformationUnitSortOrdersHolder getInformationUnitSortOrdersHolder() {
		return informationUnitSortOrdersHolder;
	}
}
