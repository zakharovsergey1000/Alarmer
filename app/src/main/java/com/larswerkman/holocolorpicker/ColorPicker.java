package com.larswerkman.holocolorpicker;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.utils.Helper;
import org.json.JSONArray;
import org.json.JSONException;

public class ColorPicker {
	private Context mContext = null;
	private float[] mHSV = new float[3];
	private int mARGB;
	private OpacityBar mOpacityBar = null;
	private SaturationBar mSaturationBar = null;
	private HueBar mHueBar = null;
	private ValueBar mValueBar = null;
	private RedBar mRedBar = null;
	private GreenBar mGreenBar = null;
	private BlueBar mBlueBar = null;
	private EditText mAlphaEditText = null;
	private EditText mHueEditText = null;
	private EditText mSaturationEditText = null;
	private EditText mValueEditText = null;
	private EditText mRedEditText = null;
	private EditText mGreenEditText = null;
	private EditText mBlueEditText = null;
	private Button[] mCustomColorButtons = null;
	private OnClickListener customColorButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			setColor((Integer) v.getTag());
		}
	};
	private OnLongClickListener customColorButtonOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			v.setBackgroundColor(mARGB);
			v.setTag(mARGB);
			// write
			JSONArray arr = new JSONArray();
			for (int i = 0; i < mCustomColorButtons.length; i++) {
				Button button = mCustomColorButtons[i];
				arr.put(button.getTag());
			}
			PreferenceManager
					.getDefaultSharedPreferences(mContext)
					.edit()
					.putString(
							mContext.getResources().getString(
									R.string.preference_key_custom_colors),
							arr.toString()).commit();
			return true;
		}
	};
	private OnFocusChangeListener hueTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				float hue = mHSV[0];
				try {
					hue = Float.parseFloat(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, mHSV[0] + "");
				}
				setNewHue(hue);
			}
		}
	};
	private OnFocusChangeListener saturationTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				float saturation = mHSV[1];
				try {
					saturation = Float.parseFloat(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, mHSV[1] + "");
				}
				setNewSaturation(saturation);
			}
		}
	};
	private OnFocusChangeListener valueTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				float value = mHSV[2];
				try {
					value = Float.parseFloat(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, mHSV[2] + "");
				}
				setNewValue(value);
			}
		}
	};
	private OnFocusChangeListener alphaTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				int alpha = Color.alpha(mARGB);
				try {
					alpha = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, alpha + "");
				}
				setNewAlpha(alpha);
			}
		}
	};
	private OnFocusChangeListener redTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				int red = Color.red(mARGB);
				try {
					red = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, red + "");
				}
				setNewRed(red);
			}
		}
	};
	private OnFocusChangeListener greenTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				int green = Color.green(mARGB);
				try {
					green = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, green + "");
				}
				setNewGreen(green);
			}
		}
	};
	private OnFocusChangeListener blueTextFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (!hasFocus) {
				EditText editText = (EditText) v;
				Editable s = editText.getEditableText();
				int blue = Color.blue(mARGB);
				try {
					blue = Integer.parseInt(s.toString());
				} catch (NumberFormatException e) {
					s.clear();
					s.insert(0, blue + "");
				}
				setNewBlue(blue);
			}
		}
	};

	// private TextWatcher saturationTextWatcher = new TextWatcher() {
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before, int count) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	// // TODO Auto-generated method stub
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// float value = mHSV[1];
	// try {
	// value = Float.parseFloat(s.toString());
	// } catch (NumberFormatException e) {
	// s.clear();
	// s.insert(0, mHSV[1] + "");
	// }
	// if (value >= 0 && value <= 1.0f) {
	// mHSV[1] = value;
	// setColor(mARGB, mHSV);
	// } else {
	// }
	// }
	// };
	public ColorPicker(Context context) {
		mContext = context;
		mARGB = 0xFFFFFFFF;
	}

	public ColorPicker(Context context, int color) {
		mContext = context;
		mARGB = color;
	}

	// /** {@code onColorChangedListener} instance of the onColorChangedListener */
	// private OnColorChangedListener onColorChangedListener;
	// /** {@code onColorSelectedListener} instance of the onColorSelectedListener */
	// private OnColorSelectedListener onColorSelectedListener;
	//
	// /** An interface that is called whenever the color is changed. Currently it is
	// always
	// * called when the color is changes.
	// *
	// * @author lars */
	// public interface OnColorChangedListener {
	// public void onColorChanged(int color);
	// }
	//
	// /** An interface that is called whenever a new color has been selected. Currently
	// it is
	// * always called when the color wheel has been released. */
	// public interface OnColorSelectedListener {
	// public void onColorSelected(int color);
	// }
	//
	// /** Set a onColorChangedListener
	// *
	// * @param listener
	// * {@code OnColorChangedListener} */
	// public void setOnColorChangedListener(OnColorChangedListener listener) {
	// onColorChangedListener = listener;
	// }
	//
	// /** Gets the onColorChangedListener
	// *
	// * @return {@code OnColorChangedListener} */
	// public OnColorChangedListener getOnColorChangedListener() {
	// return onColorChangedListener;
	// }
	//
	// /** Set a onColorSelectedListener
	// *
	// * @param listener
	// * {@code OnColorSelectedListener} */
	// public void setOnColorSelectedListener(OnColorSelectedListener listener) {
	// onColorSelectedListener = listener;
	// }
	//
	// /** Gets the onColorSelectedListener
	// *
	// * @return {@code OnColorSelectedListener} */
	// public OnColorSelectedListener getOnColorSelectedListener() {
	// return onColorSelectedListener;
	// }
	public int getArgbColor() {
		return mARGB;
	}

	public float[] getHsvColor() {
		return mHSV;
	}

	public void setColor(int color) {
		mARGB = color;
		Color.colorToHSV(color, mHSV);
		setHsvBars();
		setRgbBars();
	}

	public void setColor(int argb, float[] hsv) {
		mHSV[0] = hsv[0];
		mHSV[1] = hsv[1];
		mHSV[2] = hsv[2];
		mARGB = argb;
		setHsvBars();
		setRgbBars();
	}

	public void setHueBar(HueBar bar) {
		mHueBar = bar;
		mHueBar.setColorPicker(this);
		setHsvBars();
	}

	public void setSaturationBar(SaturationBar bar) {
		mSaturationBar = bar;
		mSaturationBar.setColorPicker(this);
		setHsvBars();
	}

	public void setValueBar(ValueBar bar) {
		mValueBar = bar;
		mValueBar.setColorPicker(this);
		setHsvBars();
	}

	public void setOpacityBar(OpacityBar bar) {
		mOpacityBar = bar;
		mOpacityBar.setColorPicker(this);
		setRgbBars();
	}

	public void setRedBar(RedBar bar) {
		mRedBar = bar;
		mRedBar.setColorPicker(this);
		setRgbBars();
	}

	public void setGreenBar(GreenBar bar) {
		mGreenBar = bar;
		mGreenBar.setColorPicker(this);
		setRgbBars();
	}

	public void setBlueBar(BlueBar bar) {
		mBlueBar = bar;
		mBlueBar.setColorPicker(this);
		setRgbBars();
	}

	public void setHueEditText(EditText editText) {
		mHueEditText = editText;
		mHueEditText.setOnFocusChangeListener(hueTextFocusChangeListener);
		setHsvBars();
	}

	public void setSaturationEditText(EditText editText) {
		mSaturationEditText = editText;
		// mSaturationEditText.addTextChangedListener(saturationTextWatcher);
		mSaturationEditText.setOnFocusChangeListener(saturationTextFocusChangeListener);
		setHsvBars();
	}

	public void setValueEditText(EditText editText) {
		mValueEditText = editText;
		mValueEditText.setOnFocusChangeListener(valueTextFocusChangeListener);
		setHsvBars();
	}

	public void setAlphaEditText(EditText editText) {
		mAlphaEditText = editText;
		mAlphaEditText.setOnFocusChangeListener(alphaTextFocusChangeListener);
		setRgbBars();
	}

	public void setRedEditText(EditText editText) {
		mRedEditText = editText;
		mRedEditText.setOnFocusChangeListener(redTextFocusChangeListener);
		setRgbBars();
	}

	public void setGreenEditText(EditText editText) {
		mGreenEditText = editText;
		mGreenEditText.setOnFocusChangeListener(greenTextFocusChangeListener);
		setRgbBars();
	}

	public void setBlueEditText(EditText editText) {
		mBlueEditText = editText;
		mBlueEditText.setOnFocusChangeListener(blueTextFocusChangeListener);
		setRgbBars();
	}

	public void setCustomColorButtons(Button[] customColorButtons) {
		mCustomColorButtons = customColorButtons;
		for (int i = 0; i < customColorButtons.length; i++) {
			Button button = customColorButtons[i];
			button.setTag(0xFFFFFFFF);
			button.setBackgroundColor(0xFFFFFFFF);
			button.setOnClickListener(customColorButtonOnClickListener);
			button.setOnLongClickListener(customColorButtonOnLongClickListener);
		}
		try {
			JSONArray arr = new JSONArray(Helper.getStringPreferenceValue(
					mContext,
					mContext.getResources().getString(
							R.string.preference_key_custom_colors), "[]"));
			for (int i = 0; i < customColorButtons.length && i < arr.length(); i++) {
				Button button = customColorButtons[i];
				Integer color = arr.getInt(i);
				button.setTag(color);
				button.setBackgroundColor(color);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setNewHue(float newHue) {
		mHSV[0] = newHue;
		mARGB = Color.HSVToColor(Color.alpha(mARGB), mHSV);
		setHsvBars();
		setRgbBars();
	}

	public void setNewSaturation(float newSaturation) {
		mHSV[1] = newSaturation;
		mARGB = Color.HSVToColor(Color.alpha(mARGB), mHSV);
		setHsvBars();
		setRgbBars();
	}

	public void setNewValue(float newValue) {
		mHSV[2] = newValue;
		mARGB = Color.HSVToColor(Color.alpha(mARGB), mHSV);
		setHsvBars();
		setRgbBars();
	}

	public void setNewAlpha(int newAlpha) {
		mARGB = Color.argb(newAlpha, Color.red(mARGB), Color.green(mARGB),
				Color.blue(mARGB));
		Color.colorToHSV(mARGB, mHSV);
		setHsvBars();
		setRgbBars();
	}

	void setNewRed(int newRed) {
		int green = Color.green(mARGB);
		int blue = Color.blue(mARGB);
		float oldHue = mHSV[0];
		mARGB = Color.argb(Color.alpha(mARGB), newRed, green, blue);
		Color.colorToHSV(mARGB, mHSV);
		if (newRed == 255 && green == 255 && blue == 255) {
			mHSV[0] = oldHue;
		}
		setHsvBars();
		setRgbBars();
	}

	void setNewGreen(int newGreen) {
		int red = Color.red(mARGB);
		int blue = Color.blue(mARGB);
		float oldHue = mHSV[0];
		mARGB = Color.argb(Color.alpha(mARGB), red, newGreen, blue);
		Color.colorToHSV(mARGB, mHSV);
		if (newGreen == 255 && red == 255 && blue == 255) {
			mHSV[0] = oldHue;
		}
		setHsvBars();
		setRgbBars();
	}

	void setNewBlue(int newBlue) {
		int red = Color.red(mARGB);
		int green = Color.green(mARGB);
		float oldHue = mHSV[0];
		mARGB = Color.argb(Color.alpha(mARGB), red, green, newBlue);
		Color.colorToHSV(mARGB, mHSV);
		if (newBlue == 255 && red == 255 && green == 255) {
			mHSV[0] = oldHue;
		}
		setHsvBars();
		setRgbBars();
	}

	private void setHsvBars() {
		if (mHueBar != null) {
			mHueBar.setColor(mARGB, mHSV);
		}
		if (mSaturationBar != null) {
			mSaturationBar.setColor(mHSV);
		}
		if (mValueBar != null) {
			mValueBar.setColor(mHSV);
		}
		if (mHueEditText != null) {
			mHueEditText.setText(mHSV[0] + "");
		}
		if (mSaturationEditText != null) {
			// mSaturationEditText.removeTextChangedListener(saturationTextWatcher);
			mSaturationEditText.setText(mHSV[1] + "");
			// mSaturationEditText.addTextChangedListener(saturationTextWatcher);
		}
		if (mValueEditText != null) {
			mValueEditText.setText(mHSV[2] + "");
		}
	}

	private void setRgbBars() {
		if (mOpacityBar != null) {
			mOpacityBar.setValue(Color.alpha(mARGB));
		}
		if (mRedBar != null) {
			mRedBar.setValue(Color.red(mARGB));
		}
		if (mGreenBar != null) {
			mGreenBar.setValue(Color.green(mARGB));
		}
		if (mBlueBar != null) {
			mBlueBar.setValue(Color.blue(mARGB));
		}
		if (mAlphaEditText != null) {
			mAlphaEditText.setText(Color.alpha(mARGB) + "");
		}
		if (mRedEditText != null) {
			mRedEditText.setText(Color.red(mARGB) + "");
		}
		if (mGreenEditText != null) {
			mGreenEditText.setText(Color.green(mARGB) + "");
		}
		if (mBlueEditText != null) {
			mBlueEditText.setText(Color.blue(mARGB) + "");
		}
	}
}
