package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import com.larswerkman.holocolorpicker.BlueBar;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.GreenBar;
import com.larswerkman.holocolorpicker.HueBar;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.RedBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;

// import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
public class ActivityColorPicker extends AppCompatActivity /* implements
															 * OnColorChangedListener */{
	private static final String KEY_COLOR_ARGB = "KEY_COLOR_ARGB";
	private static final String KEY_COLOR_HSV = "KEY_COLOR_HSV";
	// private static final String COLOR_HUE = "COLOR_HUE";
	// private static final String COLOR_SATURATION = "COLOR_SATURATION";
	// private static final String COLOR_VALUE = "COLOR_VALUE";
	private ColorPicker colorPicker;
	private HueBar hueBar;
	private SaturationBar saturationBar;
	private ValueBar valueBar;
	private OpacityBar alphaBar;
	private RedBar redBar;
	private GreenBar greenBar;
	private BlueBar blueBar;
	private EditText hueEditText;
	private EditText saturationEditText;
	private EditText valueEditText;
	private EditText alphaEditText;
	private EditText redEditText;
	private EditText greenEditText;
	private EditText blueEditText;
	private Toolbar mToolbar;

	// private Button button;
	// private TextView text;
	// private int mColor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_color_picker);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		hueBar = (HueBar) findViewById(R.id.huebar);
		saturationBar = (SaturationBar) findViewById(R.id.saturationbar);
		valueBar = (ValueBar) findViewById(R.id.valuebar);
		alphaBar = (OpacityBar) findViewById(R.id.alphabar);
		redBar = (RedBar) findViewById(R.id.redbar);
		greenBar = (GreenBar) findViewById(R.id.greenbar);
		blueBar = (BlueBar) findViewById(R.id.bluebar);
		hueEditText = (EditText) findViewById(R.id.edittext_hue);
		saturationEditText = (EditText) findViewById(R.id.edittext_saturation);
		valueEditText = (EditText) findViewById(R.id.edittext_value);
		alphaEditText = (EditText) findViewById(R.id.edittext_alpha);
		redEditText = (EditText) findViewById(R.id.edittext_red);
		greenEditText = (EditText) findViewById(R.id.edittext_green);
		blueEditText = (EditText) findViewById(R.id.edittext_blue);
		Button[] customColorButtons = new Button[16];
		Button customColorButton = (Button) findViewById(R.id.button1_1);
		customColorButtons[0] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_2);
		customColorButtons[1] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_3);
		customColorButtons[2] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_4);
		customColorButtons[3] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_5);
		customColorButtons[4] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_6);
		customColorButtons[5] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_7);
		customColorButtons[6] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_8);
		customColorButtons[7] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_9);
		customColorButtons[8] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_10);
		customColorButtons[9] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_11);
		customColorButtons[10] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_12);
		customColorButtons[11] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_13);
		customColorButtons[12] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_14);
		customColorButtons[13] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_15);
		customColorButtons[14] = customColorButton;
		customColorButton = (Button) findViewById(R.id.button1_16);
		customColorButtons[15] = customColorButton;
		// button = (Button) findViewById(R.id.button);
		// text = (TextView) findViewById(R.id.textView1);
		colorPicker = new ColorPicker(this);
		colorPicker.setHueBar(hueBar);
		colorPicker.setSaturationBar(saturationBar);
		colorPicker.setValueBar(valueBar);
		colorPicker.setOpacityBar(alphaBar);
		colorPicker.setRedBar(redBar);
		colorPicker.setGreenBar(greenBar);
		colorPicker.setBlueBar(blueBar);
		colorPicker.setHueEditText(hueEditText);
		colorPicker.setSaturationEditText(saturationEditText);
		colorPicker.setValueEditText(valueEditText);
		colorPicker.setAlphaEditText(alphaEditText);
		colorPicker.setRedEditText(redEditText);
		colorPicker.setGreenEditText(greenEditText);
		colorPicker.setBlueEditText(blueEditText);
		colorPicker.setCustomColorButtons(customColorButtons);
		// picker.setOnColorChangedListener(this);
		// button.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// text.setTextColor(colorPicker.getColor());
		// // picker.setOldCenterColor(picker.getColor());
		// }
		// });
		// mColor = 0xFFFF0000;
		// if (savedInstanceState != null) {
		// if (savedInstanceState.containsKey(CommonConstants.INTENT_EXTRA_COLOR)) {
		// mColor = savedInstanceState.getInt(CommonConstants.INTENT_EXTRA_COLOR);
		// }
		// }
		if (savedInstanceState == null) {
			colorPicker.setColor(getIntent().getIntExtra(
					CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, 0xFFFF0000));
		} else {
			colorPicker.setColor(
					savedInstanceState.getInt(ActivityColorPicker.KEY_COLOR_ARGB),
					savedInstanceState.getFloatArray(ActivityColorPicker.KEY_COLOR_HSV));
		}
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
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case CommonConstants.MENU_ID_OK:
			// Activity finished ok, return the data
			Intent intent = getIntent();
			intent.putExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER,
					colorPicker.getArgbColor());
			setResult(Activity.RESULT_OK, intent);
			finish();
			return true;
		case CommonConstants.MENU_ID_PICK_PREDEFINED_COLOR:
			intent = new Intent(this, ActivityPredefinedColorPicker.class);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_PICK_PREDEFINED_COLOR);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is we're responding to
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_PICK_PREDEFINED_COLOR:
			if (resultCode == Activity.RESULT_OK) {
				int color = intent.getIntExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, 0);
				colorPicker.setColor(color);
			}
			break;
		default:
			break;
		}
	}

	// @Override
	// protected void onSaveInstanceState(Bundle savedInstanceState) {
	// super.onSaveInstanceState(savedInstanceState);
	// savedInstanceState.putInt(CommonConstants.INTENT_EXTRA_COLOR, mColor);
	// }
	// @Override
	// public void onColorChanged(int color) {
	// // gives the color when it's changed.
	// mColor = color;
	// }
	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putInt(ActivityColorPicker.KEY_COLOR_ARGB,
				colorPicker.getArgbColor());
		savedInstanceState.putFloatArray(ActivityColorPicker.KEY_COLOR_HSV,
				colorPicker.getHsvColor());
		super.onSaveInstanceState(savedInstanceState);
	}
}