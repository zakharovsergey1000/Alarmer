package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TableLayout;

import com.android.supportdatetimepicker.date.TextViewWithCircularIndicator;

import java.util.HashSet;

import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.TwoWayHashmap;
import biz.advancedcalendar.alarmer.R;

public class ActivitySelectDaysOfYear2 extends AppCompatActivity {
	private TwoWayHashmap<Integer, Integer> mTwoWayHashmap = new TwoWayHashmap<Integer, Integer>();
	// ArrayList<Integer> mSelectedDays = new ArrayList<Integer>();
	HashSet<Integer> selectedDays;
	private final String mSelectedDaysKey = "SelectedDaysKey";
	TableLayout[] monthViews = new TableLayout[12];
	private Toolbar mToolbar;

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;
		GridView months;

		public ImageAdapter(Context c) {
			mContext = c;
			LayoutInflater infalInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			monthViews[0] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month1, null);
			monthViews[1] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month2, null);
			monthViews[2] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month3, null);
			monthViews[3] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month4, null);
			monthViews[4] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month5, null);
			monthViews[5] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month6, null);
			monthViews[6] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month7, null);
			monthViews[7] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month8, null);
			monthViews[8] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month9, null);
			monthViews[9] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month10, null);
			monthViews[10] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month11, null);
			monthViews[11] = (TableLayout) infalInflater.inflate(
					R.layout.tablelayout_month12, null);
		}

		@Override
		public int getCount() {
			return 12;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = monthViews[position];
			TextViewWithCircularIndicator textViewWithCircularIndicator;
			for (int selectedDay : selectedDays) {
				textViewWithCircularIndicator = (TextViewWithCircularIndicator) v
						.findViewById(mTwoWayHashmap.getForward(selectedDay));
				if (textViewWithCircularIndicator != null) {
					textViewWithCircularIndicator.setSelected(true);
					textViewWithCircularIndicator.requestLayout();
					textViewWithCircularIndicator.drawIndicator(true);
				}
			}
			return v;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_days_of_year2);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		GridView gridview = (GridView) findViewById(R.id.gridview);
		// ListView lv = null;
		// lv.setItemsCanFocus(true);
		// gridview.setItemsCanFocus(true);
		gridview.setAdapter(new ImageAdapter(this));
		fillMaps();
		if (savedInstanceState != null) {
			// mSelectedDays = savedInstanceState.getIntegerArrayList(mSelectedDaysKey);
			selectedDays = (HashSet<Integer>) savedInstanceState
					.getSerializable(mSelectedDaysKey);
		} else {
			// mSelectedDays = getIntent().getIntegerArrayListExtra(
			// CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR);
			selectedDays = (HashSet<Integer>) getIntent().getSerializableExtra(
					CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 0,
				getResources().getString(R.string.action_cancel));
		menuItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_OK, 1, getResources()
				.getString(R.string.action_done));
		menuItem.setIcon(R.drawable.ic_done_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			setResult(Activity.RESULT_CANCELED);
			finish();
			return true;
		case CommonConstants.MENU_ID_OK:
			Intent intent = getIntent();
			// intent.putExtra(CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR,
			// mSelectedDays);
			intent.putExtra(CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR,
					selectedDays);
			setResult(Activity.RESULT_OK, intent);
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// savedInstanceState.putIntegerArrayList(mSelectedDaysKey, mSelectedDays);
		savedInstanceState.putSerializable(mSelectedDaysKey, selectedDays);
	}

	public void onDayOfYearClicked(View v) {
		v.setSelected(!v.isSelected());
		v.requestLayout();
		boolean selected = v.isSelected();
		((TextViewWithCircularIndicator) v).drawIndicator(selected);
		if (selected) {
			// mSelectedDays.add(mTwoWayHashmap.getBackward(v.getId()));
			selectedDays.add(mTwoWayHashmap.getBackward(v.getId()));
		} else {
			// mSelectedDays.remove(mTwoWayHashmap.getBackward(v.getId()));
			selectedDays.remove(mTwoWayHashmap.getBackward(v.getId()));
		}
	}

	private void fillMaps() {
		mTwoWayHashmap.add(1, R.id.checkBox1);
		mTwoWayHashmap.add(2, R.id.checkBox2);
		mTwoWayHashmap.add(3, R.id.checkBox3);
		mTwoWayHashmap.add(4, R.id.checkBox4);
		mTwoWayHashmap.add(5, R.id.checkBox5);
		mTwoWayHashmap.add(6, R.id.checkBox6);
		mTwoWayHashmap.add(7, R.id.checkBox7);
		mTwoWayHashmap.add(8, R.id.checkBox8);
		mTwoWayHashmap.add(9, R.id.checkBox9);
		mTwoWayHashmap.add(10, R.id.checkBox10);
		mTwoWayHashmap.add(11, R.id.checkBox11);
		mTwoWayHashmap.add(12, R.id.checkBox12);
		mTwoWayHashmap.add(13, R.id.checkBox13);
		mTwoWayHashmap.add(14, R.id.checkBox14);
		mTwoWayHashmap.add(15, R.id.checkBox15);
		mTwoWayHashmap.add(16, R.id.checkBox16);
		mTwoWayHashmap.add(17, R.id.checkBox17);
		mTwoWayHashmap.add(18, R.id.checkBox18);
		mTwoWayHashmap.add(19, R.id.checkBox19);
		mTwoWayHashmap.add(20, R.id.checkBox20);
		mTwoWayHashmap.add(21, R.id.checkBox21);
		mTwoWayHashmap.add(22, R.id.checkBox22);
		mTwoWayHashmap.add(23, R.id.checkBox23);
		mTwoWayHashmap.add(24, R.id.checkBox24);
		mTwoWayHashmap.add(25, R.id.checkBox25);
		mTwoWayHashmap.add(26, R.id.checkBox26);
		mTwoWayHashmap.add(27, R.id.checkBox27);
		mTwoWayHashmap.add(28, R.id.checkBox28);
		mTwoWayHashmap.add(29, R.id.checkBox29);
		mTwoWayHashmap.add(30, R.id.checkBox30);
		mTwoWayHashmap.add(31, R.id.checkBox31);
		mTwoWayHashmap.add(32, R.id.checkBox32);
		mTwoWayHashmap.add(33, R.id.checkBox33);
		mTwoWayHashmap.add(34, R.id.checkBox34);
		mTwoWayHashmap.add(35, R.id.checkBox35);
		mTwoWayHashmap.add(36, R.id.checkBox36);
		mTwoWayHashmap.add(37, R.id.checkBox37);
		mTwoWayHashmap.add(38, R.id.checkBox38);
		mTwoWayHashmap.add(39, R.id.checkBox39);
		mTwoWayHashmap.add(40, R.id.checkBox40);
		mTwoWayHashmap.add(41, R.id.checkBox41);
		mTwoWayHashmap.add(42, R.id.checkBox42);
		mTwoWayHashmap.add(43, R.id.checkBox43);
		mTwoWayHashmap.add(44, R.id.checkBox44);
		mTwoWayHashmap.add(45, R.id.checkBox45);
		mTwoWayHashmap.add(46, R.id.checkBox46);
		mTwoWayHashmap.add(47, R.id.checkBox47);
		mTwoWayHashmap.add(48, R.id.checkBox48);
		mTwoWayHashmap.add(49, R.id.checkBox49);
		mTwoWayHashmap.add(50, R.id.checkBox50);
		mTwoWayHashmap.add(51, R.id.checkBox51);
		mTwoWayHashmap.add(52, R.id.checkBox52);
		mTwoWayHashmap.add(53, R.id.checkBox53);
		mTwoWayHashmap.add(54, R.id.checkBox54);
		mTwoWayHashmap.add(55, R.id.checkBox55);
		mTwoWayHashmap.add(56, R.id.checkBox56);
		mTwoWayHashmap.add(57, R.id.checkBox57);
		mTwoWayHashmap.add(58, R.id.checkBox58);
		mTwoWayHashmap.add(59, R.id.checkBox59);
		mTwoWayHashmap.add(60, R.id.checkBox60);
		mTwoWayHashmap.add(61, R.id.checkBox61);
		mTwoWayHashmap.add(62, R.id.checkBox62);
		mTwoWayHashmap.add(63, R.id.checkBox63);
		mTwoWayHashmap.add(64, R.id.checkBox64);
		mTwoWayHashmap.add(65, R.id.checkBox65);
		mTwoWayHashmap.add(66, R.id.checkBox66);
		mTwoWayHashmap.add(67, R.id.checkBox67);
		mTwoWayHashmap.add(68, R.id.checkBox68);
		mTwoWayHashmap.add(69, R.id.checkBox69);
		mTwoWayHashmap.add(70, R.id.checkBox70);
		mTwoWayHashmap.add(71, R.id.checkBox71);
		mTwoWayHashmap.add(72, R.id.checkBox72);
		mTwoWayHashmap.add(73, R.id.checkBox73);
		mTwoWayHashmap.add(74, R.id.checkBox74);
		mTwoWayHashmap.add(75, R.id.checkBox75);
		mTwoWayHashmap.add(76, R.id.checkBox76);
		mTwoWayHashmap.add(77, R.id.checkBox77);
		mTwoWayHashmap.add(78, R.id.checkBox78);
		mTwoWayHashmap.add(79, R.id.checkBox79);
		mTwoWayHashmap.add(80, R.id.checkBox80);
		mTwoWayHashmap.add(81, R.id.checkBox81);
		mTwoWayHashmap.add(82, R.id.checkBox82);
		mTwoWayHashmap.add(83, R.id.checkBox83);
		mTwoWayHashmap.add(84, R.id.checkBox84);
		mTwoWayHashmap.add(85, R.id.checkBox85);
		mTwoWayHashmap.add(86, R.id.checkBox86);
		mTwoWayHashmap.add(87, R.id.checkBox87);
		mTwoWayHashmap.add(88, R.id.checkBox88);
		mTwoWayHashmap.add(89, R.id.checkBox89);
		mTwoWayHashmap.add(90, R.id.checkBox90);
		mTwoWayHashmap.add(91, R.id.checkBox91);
		mTwoWayHashmap.add(92, R.id.checkBox92);
		mTwoWayHashmap.add(93, R.id.checkBox93);
		mTwoWayHashmap.add(94, R.id.checkBox94);
		mTwoWayHashmap.add(95, R.id.checkBox95);
		mTwoWayHashmap.add(96, R.id.checkBox96);
		mTwoWayHashmap.add(97, R.id.checkBox97);
		mTwoWayHashmap.add(98, R.id.checkBox98);
		mTwoWayHashmap.add(99, R.id.checkBox99);
		mTwoWayHashmap.add(100, R.id.checkBox100);
		mTwoWayHashmap.add(101, R.id.checkBox101);
		mTwoWayHashmap.add(102, R.id.checkBox102);
		mTwoWayHashmap.add(103, R.id.checkBox103);
		mTwoWayHashmap.add(104, R.id.checkBox104);
		mTwoWayHashmap.add(105, R.id.checkBox105);
		mTwoWayHashmap.add(106, R.id.checkBox106);
		mTwoWayHashmap.add(107, R.id.checkBox107);
		mTwoWayHashmap.add(108, R.id.checkBox108);
		mTwoWayHashmap.add(109, R.id.checkBox109);
		mTwoWayHashmap.add(110, R.id.checkBox110);
		mTwoWayHashmap.add(111, R.id.checkBox111);
		mTwoWayHashmap.add(112, R.id.checkBox112);
		mTwoWayHashmap.add(113, R.id.checkBox113);
		mTwoWayHashmap.add(114, R.id.checkBox114);
		mTwoWayHashmap.add(115, R.id.checkBox115);
		mTwoWayHashmap.add(116, R.id.checkBox116);
		mTwoWayHashmap.add(117, R.id.checkBox117);
		mTwoWayHashmap.add(118, R.id.checkBox118);
		mTwoWayHashmap.add(119, R.id.checkBox119);
		mTwoWayHashmap.add(120, R.id.checkBox120);
		mTwoWayHashmap.add(121, R.id.checkBox121);
		mTwoWayHashmap.add(122, R.id.checkBox122);
		mTwoWayHashmap.add(123, R.id.checkBox123);
		mTwoWayHashmap.add(124, R.id.checkBox124);
		mTwoWayHashmap.add(125, R.id.checkBox125);
		mTwoWayHashmap.add(126, R.id.checkBox126);
		mTwoWayHashmap.add(127, R.id.checkBox127);
		mTwoWayHashmap.add(128, R.id.checkBox128);
		mTwoWayHashmap.add(129, R.id.checkBox129);
		mTwoWayHashmap.add(130, R.id.checkBox130);
		mTwoWayHashmap.add(131, R.id.checkBox131);
		mTwoWayHashmap.add(132, R.id.checkBox132);
		mTwoWayHashmap.add(133, R.id.checkBox133);
		mTwoWayHashmap.add(134, R.id.checkBox134);
		mTwoWayHashmap.add(135, R.id.checkBox135);
		mTwoWayHashmap.add(136, R.id.checkBox136);
		mTwoWayHashmap.add(137, R.id.checkBox137);
		mTwoWayHashmap.add(138, R.id.checkBox138);
		mTwoWayHashmap.add(139, R.id.checkBox139);
		mTwoWayHashmap.add(140, R.id.checkBox140);
		mTwoWayHashmap.add(141, R.id.checkBox141);
		mTwoWayHashmap.add(142, R.id.checkBox142);
		mTwoWayHashmap.add(143, R.id.checkBox143);
		mTwoWayHashmap.add(144, R.id.checkBox144);
		mTwoWayHashmap.add(145, R.id.checkBox145);
		mTwoWayHashmap.add(146, R.id.checkBox146);
		mTwoWayHashmap.add(147, R.id.checkBox147);
		mTwoWayHashmap.add(148, R.id.checkBox148);
		mTwoWayHashmap.add(149, R.id.checkBox149);
		mTwoWayHashmap.add(150, R.id.checkBox150);
		mTwoWayHashmap.add(151, R.id.checkBox151);
		mTwoWayHashmap.add(152, R.id.checkBox152);
		mTwoWayHashmap.add(153, R.id.checkBox153);
		mTwoWayHashmap.add(154, R.id.checkBox154);
		mTwoWayHashmap.add(155, R.id.checkBox155);
		mTwoWayHashmap.add(156, R.id.checkBox156);
		mTwoWayHashmap.add(157, R.id.checkBox157);
		mTwoWayHashmap.add(158, R.id.checkBox158);
		mTwoWayHashmap.add(159, R.id.checkBox159);
		mTwoWayHashmap.add(160, R.id.checkBox160);
		mTwoWayHashmap.add(161, R.id.checkBox161);
		mTwoWayHashmap.add(162, R.id.checkBox162);
		mTwoWayHashmap.add(163, R.id.checkBox163);
		mTwoWayHashmap.add(164, R.id.checkBox164);
		mTwoWayHashmap.add(165, R.id.checkBox165);
		mTwoWayHashmap.add(166, R.id.checkBox166);
		mTwoWayHashmap.add(167, R.id.checkBox167);
		mTwoWayHashmap.add(168, R.id.checkBox168);
		mTwoWayHashmap.add(169, R.id.checkBox169);
		mTwoWayHashmap.add(170, R.id.checkBox170);
		mTwoWayHashmap.add(171, R.id.checkBox171);
		mTwoWayHashmap.add(172, R.id.checkBox172);
		mTwoWayHashmap.add(173, R.id.checkBox173);
		mTwoWayHashmap.add(174, R.id.checkBox174);
		mTwoWayHashmap.add(175, R.id.checkBox175);
		mTwoWayHashmap.add(176, R.id.checkBox176);
		mTwoWayHashmap.add(177, R.id.checkBox177);
		mTwoWayHashmap.add(178, R.id.checkBox178);
		mTwoWayHashmap.add(179, R.id.checkBox179);
		mTwoWayHashmap.add(180, R.id.checkBox180);
		mTwoWayHashmap.add(181, R.id.checkBox181);
		mTwoWayHashmap.add(182, R.id.checkBox182);
		mTwoWayHashmap.add(183, R.id.checkBox183);
		mTwoWayHashmap.add(184, R.id.checkBox184);
		mTwoWayHashmap.add(185, R.id.checkBox185);
		mTwoWayHashmap.add(186, R.id.checkBox186);
		mTwoWayHashmap.add(187, R.id.checkBox187);
		mTwoWayHashmap.add(188, R.id.checkBox188);
		mTwoWayHashmap.add(189, R.id.checkBox189);
		mTwoWayHashmap.add(190, R.id.checkBox190);
		mTwoWayHashmap.add(191, R.id.checkBox191);
		mTwoWayHashmap.add(192, R.id.checkBox192);
		mTwoWayHashmap.add(193, R.id.checkBox193);
		mTwoWayHashmap.add(194, R.id.checkBox194);
		mTwoWayHashmap.add(195, R.id.checkBox195);
		mTwoWayHashmap.add(196, R.id.checkBox196);
		mTwoWayHashmap.add(197, R.id.checkBox197);
		mTwoWayHashmap.add(198, R.id.checkBox198);
		mTwoWayHashmap.add(199, R.id.checkBox199);
		mTwoWayHashmap.add(200, R.id.checkBox200);
		mTwoWayHashmap.add(201, R.id.checkBox201);
		mTwoWayHashmap.add(202, R.id.checkBox202);
		mTwoWayHashmap.add(203, R.id.checkBox203);
		mTwoWayHashmap.add(204, R.id.checkBox204);
		mTwoWayHashmap.add(205, R.id.checkBox205);
		mTwoWayHashmap.add(206, R.id.checkBox206);
		mTwoWayHashmap.add(207, R.id.checkBox207);
		mTwoWayHashmap.add(208, R.id.checkBox208);
		mTwoWayHashmap.add(209, R.id.checkBox209);
		mTwoWayHashmap.add(210, R.id.checkBox210);
		mTwoWayHashmap.add(211, R.id.checkBox211);
		mTwoWayHashmap.add(212, R.id.checkBox212);
		mTwoWayHashmap.add(213, R.id.checkBox213);
		mTwoWayHashmap.add(214, R.id.checkBox214);
		mTwoWayHashmap.add(215, R.id.checkBox215);
		mTwoWayHashmap.add(216, R.id.checkBox216);
		mTwoWayHashmap.add(217, R.id.checkBox217);
		mTwoWayHashmap.add(218, R.id.checkBox218);
		mTwoWayHashmap.add(219, R.id.checkBox219);
		mTwoWayHashmap.add(220, R.id.checkBox220);
		mTwoWayHashmap.add(221, R.id.checkBox221);
		mTwoWayHashmap.add(222, R.id.checkBox222);
		mTwoWayHashmap.add(223, R.id.checkBox223);
		mTwoWayHashmap.add(224, R.id.checkBox224);
		mTwoWayHashmap.add(225, R.id.checkBox225);
		mTwoWayHashmap.add(226, R.id.checkBox226);
		mTwoWayHashmap.add(227, R.id.checkBox227);
		mTwoWayHashmap.add(228, R.id.checkBox228);
		mTwoWayHashmap.add(229, R.id.checkBox229);
		mTwoWayHashmap.add(230, R.id.checkBox230);
		mTwoWayHashmap.add(231, R.id.checkBox231);
		mTwoWayHashmap.add(232, R.id.checkBox232);
		mTwoWayHashmap.add(233, R.id.checkBox233);
		mTwoWayHashmap.add(234, R.id.checkBox234);
		mTwoWayHashmap.add(235, R.id.checkBox235);
		mTwoWayHashmap.add(236, R.id.checkBox236);
		mTwoWayHashmap.add(237, R.id.checkBox237);
		mTwoWayHashmap.add(238, R.id.checkBox238);
		mTwoWayHashmap.add(239, R.id.checkBox239);
		mTwoWayHashmap.add(240, R.id.checkBox240);
		mTwoWayHashmap.add(241, R.id.checkBox241);
		mTwoWayHashmap.add(242, R.id.checkBox242);
		mTwoWayHashmap.add(243, R.id.checkBox243);
		mTwoWayHashmap.add(244, R.id.checkBox244);
		mTwoWayHashmap.add(245, R.id.checkBox245);
		mTwoWayHashmap.add(246, R.id.checkBox246);
		mTwoWayHashmap.add(247, R.id.checkBox247);
		mTwoWayHashmap.add(248, R.id.checkBox248);
		mTwoWayHashmap.add(249, R.id.checkBox249);
		mTwoWayHashmap.add(250, R.id.checkBox250);
		mTwoWayHashmap.add(251, R.id.checkBox251);
		mTwoWayHashmap.add(252, R.id.checkBox252);
		mTwoWayHashmap.add(253, R.id.checkBox253);
		mTwoWayHashmap.add(254, R.id.checkBox254);
		mTwoWayHashmap.add(255, R.id.checkBox255);
		mTwoWayHashmap.add(256, R.id.checkBox256);
		mTwoWayHashmap.add(257, R.id.checkBox257);
		mTwoWayHashmap.add(258, R.id.checkBox258);
		mTwoWayHashmap.add(259, R.id.checkBox259);
		mTwoWayHashmap.add(260, R.id.checkBox260);
		mTwoWayHashmap.add(261, R.id.checkBox261);
		mTwoWayHashmap.add(262, R.id.checkBox262);
		mTwoWayHashmap.add(263, R.id.checkBox263);
		mTwoWayHashmap.add(264, R.id.checkBox264);
		mTwoWayHashmap.add(265, R.id.checkBox265);
		mTwoWayHashmap.add(266, R.id.checkBox266);
		mTwoWayHashmap.add(267, R.id.checkBox267);
		mTwoWayHashmap.add(268, R.id.checkBox268);
		mTwoWayHashmap.add(269, R.id.checkBox269);
		mTwoWayHashmap.add(270, R.id.checkBox270);
		mTwoWayHashmap.add(271, R.id.checkBox271);
		mTwoWayHashmap.add(272, R.id.checkBox272);
		mTwoWayHashmap.add(273, R.id.checkBox273);
		mTwoWayHashmap.add(274, R.id.checkBox274);
		mTwoWayHashmap.add(275, R.id.checkBox275);
		mTwoWayHashmap.add(276, R.id.checkBox276);
		mTwoWayHashmap.add(277, R.id.checkBox277);
		mTwoWayHashmap.add(278, R.id.checkBox278);
		mTwoWayHashmap.add(279, R.id.checkBox279);
		mTwoWayHashmap.add(280, R.id.checkBox280);
		mTwoWayHashmap.add(281, R.id.checkBox281);
		mTwoWayHashmap.add(282, R.id.checkBox282);
		mTwoWayHashmap.add(283, R.id.checkBox283);
		mTwoWayHashmap.add(284, R.id.checkBox284);
		mTwoWayHashmap.add(285, R.id.checkBox285);
		mTwoWayHashmap.add(286, R.id.checkBox286);
		mTwoWayHashmap.add(287, R.id.checkBox287);
		mTwoWayHashmap.add(288, R.id.checkBox288);
		mTwoWayHashmap.add(289, R.id.checkBox289);
		mTwoWayHashmap.add(290, R.id.checkBox290);
		mTwoWayHashmap.add(291, R.id.checkBox291);
		mTwoWayHashmap.add(292, R.id.checkBox292);
		mTwoWayHashmap.add(293, R.id.checkBox293);
		mTwoWayHashmap.add(294, R.id.checkBox294);
		mTwoWayHashmap.add(295, R.id.checkBox295);
		mTwoWayHashmap.add(296, R.id.checkBox296);
		mTwoWayHashmap.add(297, R.id.checkBox297);
		mTwoWayHashmap.add(298, R.id.checkBox298);
		mTwoWayHashmap.add(299, R.id.checkBox299);
		mTwoWayHashmap.add(300, R.id.checkBox300);
		mTwoWayHashmap.add(301, R.id.checkBox301);
		mTwoWayHashmap.add(302, R.id.checkBox302);
		mTwoWayHashmap.add(303, R.id.checkBox303);
		mTwoWayHashmap.add(304, R.id.checkBox304);
		mTwoWayHashmap.add(305, R.id.checkBox305);
		mTwoWayHashmap.add(306, R.id.checkBox306);
		mTwoWayHashmap.add(307, R.id.checkBox307);
		mTwoWayHashmap.add(308, R.id.checkBox308);
		mTwoWayHashmap.add(309, R.id.checkBox309);
		mTwoWayHashmap.add(310, R.id.checkBox310);
		mTwoWayHashmap.add(311, R.id.checkBox311);
		mTwoWayHashmap.add(312, R.id.checkBox312);
		mTwoWayHashmap.add(313, R.id.checkBox313);
		mTwoWayHashmap.add(314, R.id.checkBox314);
		mTwoWayHashmap.add(315, R.id.checkBox315);
		mTwoWayHashmap.add(316, R.id.checkBox316);
		mTwoWayHashmap.add(317, R.id.checkBox317);
		mTwoWayHashmap.add(318, R.id.checkBox318);
		mTwoWayHashmap.add(319, R.id.checkBox319);
		mTwoWayHashmap.add(320, R.id.checkBox320);
		mTwoWayHashmap.add(321, R.id.checkBox321);
		mTwoWayHashmap.add(322, R.id.checkBox322);
		mTwoWayHashmap.add(323, R.id.checkBox323);
		mTwoWayHashmap.add(324, R.id.checkBox324);
		mTwoWayHashmap.add(325, R.id.checkBox325);
		mTwoWayHashmap.add(326, R.id.checkBox326);
		mTwoWayHashmap.add(327, R.id.checkBox327);
		mTwoWayHashmap.add(328, R.id.checkBox328);
		mTwoWayHashmap.add(329, R.id.checkBox329);
		mTwoWayHashmap.add(330, R.id.checkBox330);
		mTwoWayHashmap.add(331, R.id.checkBox331);
		mTwoWayHashmap.add(332, R.id.checkBox332);
		mTwoWayHashmap.add(333, R.id.checkBox333);
		mTwoWayHashmap.add(334, R.id.checkBox334);
		mTwoWayHashmap.add(335, R.id.checkBox335);
		mTwoWayHashmap.add(336, R.id.checkBox336);
		mTwoWayHashmap.add(337, R.id.checkBox337);
		mTwoWayHashmap.add(338, R.id.checkBox338);
		mTwoWayHashmap.add(339, R.id.checkBox339);
		mTwoWayHashmap.add(340, R.id.checkBox340);
		mTwoWayHashmap.add(341, R.id.checkBox341);
		mTwoWayHashmap.add(342, R.id.checkBox342);
		mTwoWayHashmap.add(343, R.id.checkBox343);
		mTwoWayHashmap.add(344, R.id.checkBox344);
		mTwoWayHashmap.add(345, R.id.checkBox345);
		mTwoWayHashmap.add(346, R.id.checkBox346);
		mTwoWayHashmap.add(347, R.id.checkBox347);
		mTwoWayHashmap.add(348, R.id.checkBox348);
		mTwoWayHashmap.add(349, R.id.checkBox349);
		mTwoWayHashmap.add(350, R.id.checkBox350);
		mTwoWayHashmap.add(351, R.id.checkBox351);
		mTwoWayHashmap.add(352, R.id.checkBox352);
		mTwoWayHashmap.add(353, R.id.checkBox353);
		mTwoWayHashmap.add(354, R.id.checkBox354);
		mTwoWayHashmap.add(355, R.id.checkBox355);
		mTwoWayHashmap.add(356, R.id.checkBox356);
		mTwoWayHashmap.add(357, R.id.checkBox357);
		mTwoWayHashmap.add(358, R.id.checkBox358);
		mTwoWayHashmap.add(359, R.id.checkBox359);
		mTwoWayHashmap.add(360, R.id.checkBox360);
		mTwoWayHashmap.add(361, R.id.checkBox361);
		mTwoWayHashmap.add(362, R.id.checkBox362);
		mTwoWayHashmap.add(363, R.id.checkBox363);
		mTwoWayHashmap.add(364, R.id.checkBox364);
		mTwoWayHashmap.add(365, R.id.checkBox365);
		mTwoWayHashmap.add(366, R.id.checkBox366);
		mTwoWayHashmap.add(367, R.id.checkBox367);
	}
}
