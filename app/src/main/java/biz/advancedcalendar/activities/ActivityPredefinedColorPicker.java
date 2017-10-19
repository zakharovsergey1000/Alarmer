package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;

// import com.larswerkman.holocolorpicker.ColorPicker.OnColorChangedListener;
public class ActivityPredefinedColorPicker extends AppCompatActivity /* implements
																	 * OnColorChangedListener */{
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_predefined_color_picker);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		// findViewById(R.id.activity_predefined_color_picker_button1).setBackgroundColor(
		// 0xFFFFFFFF);
		findViewById(R.id.activity_predefined_color_picker_button1).setTag(0xFFFFFFFF);
		findViewById(R.id.activity_predefined_color_picker_button2).setTag(0xFFFFFFF0);
		findViewById(R.id.activity_predefined_color_picker_button3).setTag(0xFFFFFFE0);
		findViewById(R.id.activity_predefined_color_picker_button4).setTag(0xFFFFFF00);
		findViewById(R.id.activity_predefined_color_picker_button5).setTag(0xFFFFFAFA);
		findViewById(R.id.activity_predefined_color_picker_button6).setTag(0xFFFFFAF0);
		findViewById(R.id.activity_predefined_color_picker_button7).setTag(0xFFFFFACD);
		findViewById(R.id.activity_predefined_color_picker_button8).setTag(0xFFFFF8DC);
		findViewById(R.id.activity_predefined_color_picker_button9).setTag(0xFFFFF5EE);
		findViewById(R.id.activity_predefined_color_picker_button10).setTag(0xFFFFF0F5);
		findViewById(R.id.activity_predefined_color_picker_button11).setTag(0xFFFFEFD5);
		findViewById(R.id.activity_predefined_color_picker_button12).setTag(0xFFFFEBCD);
		findViewById(R.id.activity_predefined_color_picker_button13).setTag(0xFFFFE4E1);
		findViewById(R.id.activity_predefined_color_picker_button14).setTag(0xFFFFE4C4);
		findViewById(R.id.activity_predefined_color_picker_button15).setTag(0xFFFFE4B5);
		findViewById(R.id.activity_predefined_color_picker_button16).setTag(0xFFFFDEAD);
		findViewById(R.id.activity_predefined_color_picker_button17).setTag(0xFFFFDAB9);
		findViewById(R.id.activity_predefined_color_picker_button18).setTag(0xFFFFD700);
		findViewById(R.id.activity_predefined_color_picker_button19).setTag(0xFFFFC0CB);
		findViewById(R.id.activity_predefined_color_picker_button20).setTag(0xFFFFB6C1);
		findViewById(R.id.activity_predefined_color_picker_button21).setTag(0xFFFFA500);
		findViewById(R.id.activity_predefined_color_picker_button22).setTag(0xFFFFA07A);
		findViewById(R.id.activity_predefined_color_picker_button23).setTag(0xFFFF8C00);
		findViewById(R.id.activity_predefined_color_picker_button24).setTag(0xFFFF7F50);
		findViewById(R.id.activity_predefined_color_picker_button25).setTag(0xFFFF69B4);
		findViewById(R.id.activity_predefined_color_picker_button26).setTag(0xFFFF6347);
		findViewById(R.id.activity_predefined_color_picker_button27).setTag(0xFFFF4500);
		findViewById(R.id.activity_predefined_color_picker_button28).setTag(0xFFFF1493);
		findViewById(R.id.activity_predefined_color_picker_button29).setTag(0xFFFF00FF);
		findViewById(R.id.activity_predefined_color_picker_button30).setTag(0xFFFF00FF);
		findViewById(R.id.activity_predefined_color_picker_button31).setTag(0xFFFF0000);
		findViewById(R.id.activity_predefined_color_picker_button32).setTag(0xFFFDF5E6);
		findViewById(R.id.activity_predefined_color_picker_button33).setTag(0xFFFAFAD2);
		findViewById(R.id.activity_predefined_color_picker_button34).setTag(0xFFFAF0E6);
		findViewById(R.id.activity_predefined_color_picker_button35).setTag(0xFFFAEBD7);
		findViewById(R.id.activity_predefined_color_picker_button36).setTag(0xFFFA8072);
		findViewById(R.id.activity_predefined_color_picker_button37).setTag(0xFFF8F8FF);
		findViewById(R.id.activity_predefined_color_picker_button38).setTag(0xFFF5FFFA);
		findViewById(R.id.activity_predefined_color_picker_button39).setTag(0xFFF5F5F5);
		findViewById(R.id.activity_predefined_color_picker_button40).setTag(0xFFF5F5DC);
		findViewById(R.id.activity_predefined_color_picker_button41).setTag(0xFFF5DEB3);
		findViewById(R.id.activity_predefined_color_picker_button42).setTag(0xFFF4A460);
		findViewById(R.id.activity_predefined_color_picker_button43).setTag(0xFFF0FFFF);
		findViewById(R.id.activity_predefined_color_picker_button44).setTag(0xFFF0FFF0);
		findViewById(R.id.activity_predefined_color_picker_button45).setTag(0xFFF0F8FF);
		findViewById(R.id.activity_predefined_color_picker_button46).setTag(0xFFF0E68C);
		findViewById(R.id.activity_predefined_color_picker_button47).setTag(0xFFF08080);
		findViewById(R.id.activity_predefined_color_picker_button48).setTag(0xFFEEE8AA);
		findViewById(R.id.activity_predefined_color_picker_button49).setTag(0xFFEE82EE);
		findViewById(R.id.activity_predefined_color_picker_button50).setTag(0xFFE9967A);
		findViewById(R.id.activity_predefined_color_picker_button51).setTag(0xFFE6E6FA);
		findViewById(R.id.activity_predefined_color_picker_button52).setTag(0xFFE0FFFF);
		findViewById(R.id.activity_predefined_color_picker_button53).setTag(0xFFDEB887);
		findViewById(R.id.activity_predefined_color_picker_button54).setTag(0xFFDDA0DD);
		findViewById(R.id.activity_predefined_color_picker_button55).setTag(0xFFDCDCDC);
		findViewById(R.id.activity_predefined_color_picker_button56).setTag(0xFFDC143C);
		findViewById(R.id.activity_predefined_color_picker_button57).setTag(0xFFDB7093);
		findViewById(R.id.activity_predefined_color_picker_button58).setTag(0xFFDAA520);
		findViewById(R.id.activity_predefined_color_picker_button59).setTag(0xFFDA70D6);
		findViewById(R.id.activity_predefined_color_picker_button60).setTag(0xFFD8BFD8);
		findViewById(R.id.activity_predefined_color_picker_button61).setTag(0xFFD3D3D3);
		findViewById(R.id.activity_predefined_color_picker_button62).setTag(0xFFD2B48C);
		findViewById(R.id.activity_predefined_color_picker_button63).setTag(0xFFD2691E);
		findViewById(R.id.activity_predefined_color_picker_button64).setTag(0xFFCD853F);
		findViewById(R.id.activity_predefined_color_picker_button65).setTag(0xFFCD5C5C);
		findViewById(R.id.activity_predefined_color_picker_button66).setTag(0xFFC71585);
		findViewById(R.id.activity_predefined_color_picker_button67).setTag(0xFFC0C0C0);
		findViewById(R.id.activity_predefined_color_picker_button68).setTag(0xFFBDB76B);
		findViewById(R.id.activity_predefined_color_picker_button69).setTag(0xFFBC8F8F);
		findViewById(R.id.activity_predefined_color_picker_button70).setTag(0xFFBA55D3);
		findViewById(R.id.activity_predefined_color_picker_button71).setTag(0xFFB8860B);
		findViewById(R.id.activity_predefined_color_picker_button72).setTag(0xFFB22222);
		findViewById(R.id.activity_predefined_color_picker_button73).setTag(0xFFB0E0E6);
		findViewById(R.id.activity_predefined_color_picker_button74).setTag(0xFFB0C4DE);
		findViewById(R.id.activity_predefined_color_picker_button75).setTag(0xFFAFEEEE);
		findViewById(R.id.activity_predefined_color_picker_button76).setTag(0xFFADFF2F);
		findViewById(R.id.activity_predefined_color_picker_button77).setTag(0xFFADD8E6);
		findViewById(R.id.activity_predefined_color_picker_button78).setTag(0xFFA9A9A9);
		findViewById(R.id.activity_predefined_color_picker_button79).setTag(0xFFA52A2A);
		findViewById(R.id.activity_predefined_color_picker_button80).setTag(0xFFA0522D);
		findViewById(R.id.activity_predefined_color_picker_button81).setTag(0xFF9ACD32);
		findViewById(R.id.activity_predefined_color_picker_button82).setTag(0xFF9932CC);
		findViewById(R.id.activity_predefined_color_picker_button83).setTag(0xFF98FB98);
		findViewById(R.id.activity_predefined_color_picker_button84).setTag(0xFF9400D3);
		findViewById(R.id.activity_predefined_color_picker_button85).setTag(0xFF9370DB);
		findViewById(R.id.activity_predefined_color_picker_button86).setTag(0xFF90EE90);
		findViewById(R.id.activity_predefined_color_picker_button87).setTag(0xFF8FBC8F);
		findViewById(R.id.activity_predefined_color_picker_button88).setTag(0xFF8B4513);
		findViewById(R.id.activity_predefined_color_picker_button89).setTag(0xFF8B008B);
		findViewById(R.id.activity_predefined_color_picker_button90).setTag(0xFF8B0000);
		findViewById(R.id.activity_predefined_color_picker_button91).setTag(0xFF8A2BE2);
		findViewById(R.id.activity_predefined_color_picker_button92).setTag(0xFF87CEFA);
		findViewById(R.id.activity_predefined_color_picker_button93).setTag(0xFF87CEEB);
		findViewById(R.id.activity_predefined_color_picker_button94).setTag(0xFF808080);
		findViewById(R.id.activity_predefined_color_picker_button95).setTag(0xFF808000);
		findViewById(R.id.activity_predefined_color_picker_button96).setTag(0xFF800080);
		findViewById(R.id.activity_predefined_color_picker_button97).setTag(0xFF800000);
		findViewById(R.id.activity_predefined_color_picker_button98).setTag(0xFF7FFFD4);
		findViewById(R.id.activity_predefined_color_picker_button99).setTag(0xFF7FFF00);
		findViewById(R.id.activity_predefined_color_picker_button100).setTag(0xFF7CFC00);
		findViewById(R.id.activity_predefined_color_picker_button101).setTag(0xFF7B68EE);
		findViewById(R.id.activity_predefined_color_picker_button102).setTag(0xFF778899);
		findViewById(R.id.activity_predefined_color_picker_button103).setTag(0xFF708090);
		findViewById(R.id.activity_predefined_color_picker_button104).setTag(0xFF6B8E23);
		findViewById(R.id.activity_predefined_color_picker_button105).setTag(0xFF6A5ACD);
		findViewById(R.id.activity_predefined_color_picker_button106).setTag(0xFF696969);
		findViewById(R.id.activity_predefined_color_picker_button107).setTag(0xFF66CDAA);
		findViewById(R.id.activity_predefined_color_picker_button108).setTag(0xFF6495ED);
		findViewById(R.id.activity_predefined_color_picker_button109).setTag(0xFF5F9EA0);
		findViewById(R.id.activity_predefined_color_picker_button110).setTag(0xFF556B2F);
		findViewById(R.id.activity_predefined_color_picker_button111).setTag(0xFF4B0082);
		findViewById(R.id.activity_predefined_color_picker_button112).setTag(0xFF48D1CC);
		findViewById(R.id.activity_predefined_color_picker_button113).setTag(0xFF483D8B);
		findViewById(R.id.activity_predefined_color_picker_button114).setTag(0xFF4682B4);
		findViewById(R.id.activity_predefined_color_picker_button115).setTag(0xFF4169E1);
		findViewById(R.id.activity_predefined_color_picker_button116).setTag(0xFF40E0D0);
		findViewById(R.id.activity_predefined_color_picker_button117).setTag(0xFF3CB371);
		findViewById(R.id.activity_predefined_color_picker_button118).setTag(0xFF32CD32);
		findViewById(R.id.activity_predefined_color_picker_button119).setTag(0xFF2F4F4F);
		findViewById(R.id.activity_predefined_color_picker_button120).setTag(0xFF2E8B57);
		findViewById(R.id.activity_predefined_color_picker_button121).setTag(0xFF228B22);
		findViewById(R.id.activity_predefined_color_picker_button122).setTag(0xFF20B2AA);
		findViewById(R.id.activity_predefined_color_picker_button123).setTag(0xFF1E90FF);
		findViewById(R.id.activity_predefined_color_picker_button124).setTag(0xFF191970);
		findViewById(R.id.activity_predefined_color_picker_button125).setTag(0xFF00FFFF);
		findViewById(R.id.activity_predefined_color_picker_button126).setTag(0xFF00FFFF);
		findViewById(R.id.activity_predefined_color_picker_button127).setTag(0xFF00FF7F);
		findViewById(R.id.activity_predefined_color_picker_button128).setTag(0xFF00FF00);
		findViewById(R.id.activity_predefined_color_picker_button129).setTag(0xFF00FA9A);
		findViewById(R.id.activity_predefined_color_picker_button130).setTag(0xFF00CED1);
		findViewById(R.id.activity_predefined_color_picker_button131).setTag(0xFF00BFFF);
		findViewById(R.id.activity_predefined_color_picker_button132).setTag(0xFF008B8B);
		findViewById(R.id.activity_predefined_color_picker_button133).setTag(0xFF008080);
		findViewById(R.id.activity_predefined_color_picker_button134).setTag(0xFF008000);
		findViewById(R.id.activity_predefined_color_picker_button135).setTag(0xFF006400);
		findViewById(R.id.activity_predefined_color_picker_button136).setTag(0xFF0000FF);
		findViewById(R.id.activity_predefined_color_picker_button137).setTag(0xFF0000CD);
		findViewById(R.id.activity_predefined_color_picker_button138).setTag(0xFF00008B);
		findViewById(R.id.activity_predefined_color_picker_button139).setTag(0xFF000080);
		findViewById(R.id.activity_predefined_color_picker_button140).setTag(0xFF000000);
	}

	public void onPredefinedColorButtonClick(View v) {
		Intent intent = getIntent();
		intent.putExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, (Integer) v.getTag());
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_resnooze, menu);
		MenuItem editItem;
		// editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_OK, 1, getResources()
		// .getString(R.string.action_done));
		// editItem.setIcon(R.drawable.ic_navigation_accept);
		// MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 0, getResources()
				.getString(R.string.action_cancel));
		editItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
			// case CommonConstants.MENU_ID_OK:
			// // Activity finished ok, return the data
			// Intent intent = getIntent();
			// intent.putExtra(CommonConstants.INTENT_EXTRA_COLOR, 0);
			// setResult(Activity.RESULT_OK, intent);
			// finish();
			// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}