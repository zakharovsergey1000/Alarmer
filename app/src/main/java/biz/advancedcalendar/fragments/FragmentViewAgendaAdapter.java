package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentViewAgenda.RetainedFragmentForFragmentViewAgendaHolder;
import biz.advancedcalendar.fragments.FragmentViewAgendaAdapter.ExpandListGroupData;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewAgenda.LoadGroupAsyncTaskArguments;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.DaysSelectedListener;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter;
import biz.advancedcalendar.views.accessories.ExpandListGroup;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FragmentViewAgendaAdapter
		extends
		EndlessScrollBaseExpandableListAdapter<ExpandListGroupData, CalendarViewTaskOccurrence> {
	public static class ExpandListGroupData {
		public long DayDateTimeWithTimeAtStartOfDay;

		public ExpandListGroupData(long dayDateTime) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(dayDateTime);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			DayDateTimeWithTimeAtStartOfDay = calendar.getTimeInMillis();
		}
	}

	private long mDateTimeOnVirtualMiddleOffset;
	private LayoutInflater mLayoutInflater;
	private DateFormat mDateFormatDateInstance;
	// private List<Task> mTasks;
	// private TaskClickedListener mTaskClickedListener;
	private DaysSelectedListener mDayClickedListener;
	private DateFormat mDateFormatTimeInstance;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;
	RetainedFragmentForFragmentViewAgendaHolder mRetainedFragmentForFragmentViewAgendaHolder;
	// private
	// EndlessScrollBaseExpandableListAdapter.EndlessScrollBaseExpandableListAdapterDataHolder<ExpandListGroupData,
	// CalendarViewTaskOccurrence> mEndlessScrollBaseExpandableListAdapterDataHolder;
	private FragmentViewAgendaAdapterDataHolder mFragmentViewAgendaAdapterDataHolder;
	private Object mLoadGroupAsyncTask;
	private Context mContext;
	private boolean mMarkSyncNeeded;
	private InformationUnitMatrix informationUnitMatrix;
	private ArrayList<String> taskInformationStrings;
	private String firstRow;
	private String secondRow;

	public void setLoadGroupAsyncTask(Object loadGroupAsyncTask) {
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"setLoadGroupAsyncTask thread: %s", Thread.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    mLoadGroupAsyncTask before: %s", mLoadGroupAsyncTask));
		}
		mLoadGroupAsyncTask = loadGroupAsyncTask;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    mLoadGroupAsyncTask after: %s", mLoadGroupAsyncTask));
		}
	}

	public FragmentViewAgendaAdapter(
			Context context,
			RetainedFragmentForFragmentViewAgendaHolder retainedFragmentForFragmentViewAgendaHolder,
			ExpandableListView expandList, DaysSelectedListener dayClickedListener,
			TaskClickedListener taskClickedListener) {
		super(context, retainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda(), expandList);
		mContext = context;
		mRetainedFragmentForFragmentViewAgendaHolder = retainedFragmentForFragmentViewAgendaHolder;
		// mEndlessScrollBaseExpandableListAdapterDataHolder =
		// retainedFragmentForFragmentViewAgendaHolder
		// .getRetainedFragmentForFragmentViewAgenda();
		mFragmentViewAgendaAdapterDataHolder = retainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(retainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda()
				.getDateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		mDateTimeOnVirtualMiddleOffset = calendar.getTimeInMillis();
		// mExpandList = expandList;
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDateFormatDateInstance = DateFormat.getDateInstance();
		mDateFormatTimeInstance = DateFormat.getTimeInstance();
		// mTasks = retainedFragmentForFragmentViewAgendaHolder
		// .getRetainedFragmentForFragmentViewAgenda().getTasks();
		// mTaskClickedListener = taskClickedListener;
		mDayClickedListener = dayClickedListener;
		LinearLayout linearLayout = (LinearLayout) mLayoutInflater.inflate(
				R.layout.expandlist_group_item, null);
		TextView tv = (TextView) linearLayout.findViewById(R.id.tvGroup);
		mDefaultTextColorStateList = tv.getTextColors();
		mDefaultBackground = tv.getBackground();
	}

	public void reloadTasks() {
		Long[] selectedCalendars = Helper.getLongArray(mContext, mContext.getResources()
				.getString(R.string.preference_key_selected_calendars), null);
		mFragmentViewAgendaAdapterDataHolder.setTasks(DataProvider
				.getActiveNotDeletedTasks(null, mContext, selectedCalendars));
		// mTasks = mFragmentViewAgendaAdapterDataHolder.getTasks();
		clearLoadedGroups();
		// ScrollPositionData data = mEndlessScrollBaseExpandableListAdapterDataHolder
		// .getScrollPositionData();
		//
		// ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> group =
		// getGroup(groupPosition);
		// final CalendarViewTaskOccurrence calendarViewTaskOccurrence = group.getItems()
		// .get(childPosition);
		// long dayStartDateTime = new DateTime(group.getData().DayDateTime).getMillis();
		//
		//
		int virtualGroupPosition = mScrollPositionData.FirstVisibleGroup
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mDateTimeOnVirtualMiddleOffset);
		calendar.add(Calendar.DAY_OF_YEAR, virtualGroupPosition);
		long dateTimeOnVirtualGroupPosition = calendar.getTimeInMillis();
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().getTaskOccurrences(
						dateTimeOnVirtualGroupPosition,
						Helper.getFirstDayOfWeek(mContext));
		ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> expandListGroup = new ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>(
				virtualGroupPosition, new ExpandListGroupData(
						dateTimeOnVirtualGroupPosition), calendarViewTaskOccurrences);
		mLoadedGroups.put(virtualGroupPosition, expandListGroup);
		notifyDataSetChanged();
		// addLoadedGroup(expandListGroup, RETAIN_CURRENT_SCROLL_POSITION);
		// mScrollPositionData.FirstVisibleLoadedGroup = null;
		// mScrollPositionData.FirstVisibleLoadedGroupsFirstVisibleChild = null;
		// mScrollPositionData.setFirstVisibleLoadedGroupsFirstVisibleChildsTop(null);
		int flatListPosition = mExpandableListView.getFlatListPosition(ExpandableListView
				.getPackedPositionForGroup(mScrollPositionData.FirstVisibleGroup))
				+ mScrollPositionData.FirstVisibleGroupsFirstVisibleChild + 1;
		Log.d("JumpDebug", String.format(
				"reloadTasks setSelectionFromTop(%d, %d) thread: %s", flatListPosition,
				mScrollPositionData.FirstVisibleGroupsFirstVisibleChildsTop, Thread
						.currentThread().getName()));
		mExpandableListView.setSelectionFromTop(flatListPosition,
				mScrollPositionData.FirstVisibleGroupsFirstVisibleChildsTop);
	}

	// public void reset(List<Task> tasks) {
	// ExpandableListViewScrollPositionData data =
	// getExpandableListViewScrollPositionData();
	// mTasks = tasks;
	// synchronized (FragmentViewAgendaAdapter.class) {
	// mTaskOccurrencesCalculatorAndDistributor = new
	// TaskOccurrencesCalculatorAndDistributor(
	// mContext, mTasks);
	// }
	// mLoadedGroups.clear();
	// notifyDataSetInvalidated();
	// notifyDataSetChanged();
	// int groupFlatListPosition;
	// int groupsFirstVisibleChild;
	// int groupsFirstVisibleChildsTop;
	// if (data != null) {
	// if (data.FirstVisibleLoadedGroup != null) {
	// groupFlatListPosition = mExpandableListView
	// .getFlatListPosition(ExpandableListView
	// .getPackedPositionForGroup(data.FirstVisibleLoadedGroup));
	// groupsFirstVisibleChild = data.FirstVisibleLoadedGroupsFirstVisibleChild;
	// groupsFirstVisibleChildsTop = data.FirstVisibleLoadedGroupsFirstVisibleChildsTop;
	// } else {
	// groupFlatListPosition = mExpandableListView
	// .getFlatListPosition(ExpandableListView
	// .getPackedPositionForGroup(data.FirstVisibleGroup));
	// groupsFirstVisibleChild = 0;
	// groupsFirstVisibleChildsTop = data.Top;
	// }
	// // postRestoreScrollState();
	// int flatListPosition = groupFlatListPosition + groupsFirstVisibleChild;
	// Log.d("JumpDebug", String.format("reset setSelectionFromTop(%d, %d)",
	// flatListPosition, groupsFirstVisibleChildsTop));
	// mExpandableListView.setSelectionFromTop(flatListPosition,
	// groupsFirstVisibleChildsTop);
	// } else {
	// Log.d("JumpDebug", "reset mScrollPositionData == null && data == null");
	// }
	// }
	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	protected View getGroupDataRow(int groupPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.fragment_view_agenda_expandlist_group_item, parent, false);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> group = getGroup(groupPosition);
		// if (group == null) {
		// loadVirtualGroupPosition(groupPosition
		// - EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
		// }
		Calendar calendarTodayWithTimeAtStartOfDay = Calendar.getInstance();
		calendarTodayWithTimeAtStartOfDay.set(Calendar.HOUR_OF_DAY, 0);
		calendarTodayWithTimeAtStartOfDay.set(Calendar.MINUTE, 0);
		calendarTodayWithTimeAtStartOfDay.set(Calendar.SECOND, 0);
		calendarTodayWithTimeAtStartOfDay.set(Calendar.MILLISECOND, 0);
		Calendar calendarDayWithTimeAtStartOfDay = Calendar.getInstance();
		calendarDayWithTimeAtStartOfDay
				.setTimeInMillis(group.getData().DayDateTimeWithTimeAtStartOfDay);
		// DateTime dateTimeTodayWithTimeAtStartOfDay = DateTime.now()
		// .withTimeAtStartOfDay();
		// DateTime dateTimeDayWithTimeAtStartOfDay = new DateTime(
		// group.getData().DayDateTimeWithTimeAtStartOfDay);
		tv.setTag(calendarDayWithTimeAtStartOfDay.getTimeInMillis());
		if (calendarTodayWithTimeAtStartOfDay.getTimeInMillis() == calendarDayWithTimeAtStartOfDay
				.getTimeInMillis()) {
			int color = Helper.getIntegerPreferenceValue(
					mContext,
					R.string.preference_key_calendar_today_date_text_color,
					mContext.getResources().getColor(
							R.color.calendar_today_date_text_color_default_value), null,
					null);
			tv.setTextColor(color);
			color = Helper.getIntegerPreferenceValue(
					mContext,
					R.string.preference_key_calendar_today_date_highlight_color,
					mContext.getResources().getColor(
							R.color.calendar_today_date_highlight_color_default_value),
					null, null);
			tv.setBackgroundColor(color);
		} else {
			tv.setTextColor(mDefaultTextColorStateList);
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				tv.setBackground(mDefaultBackground == null ? null : mDefaultBackground
						.getConstantState().newDrawable());
			} else {
				tv.setBackgroundDrawable(mDefaultBackground == null ? null
						: mDefaultBackground.getConstantState().newDrawable());
			}
		}
		tv.setText(mDateFormatDateInstance.format(new Date(
				calendarDayWithTimeAtStartOfDay.getTimeInMillis())));
		/* int flatListPosition =
		 * mExpandableListView.getFlatListPosition(ExpandableListView
		 * .getPackedPositionForGroup(groupPosition));
		 * tv.setText(mDateFormatDateInstance.format(new Date(dateTimeDay.getMillis())) +
		 * " groupPosition " + groupPosition + " flatListPosition " + flatListPosition); */
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDayClickedListener != null) {
					Long dateTimeDay = (Long) v.getTag();
					mDayClickedListener.onDaysSelected(dateTimeDay, dateTimeDay);
				}
			}
		});
		return convertView;
	}

	@Override
	protected View getChildDataRow(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(
					R.layout.fragment_view_agenda_expandlist_child_item, parent, false);
		}
		View colorBox = convertView
				.findViewById(R.id.fragment_view_agenda_expandlist_child_item_color_box);
		TextView tv = (TextView) convertView
				.findViewById(R.id.fragment_view_agenda_expandlist_child_item_textview);
		ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> group = getGroup(groupPosition);
		final CalendarViewTaskOccurrence calendarViewTaskOccurrence = group.getItems()
				.get(childPosition);
		long dayStartDateTimeWithTimeAtStartOfDay = group.getData().DayDateTimeWithTimeAtStartOfDay;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dayStartDateTimeWithTimeAtStartOfDay);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		long dayEndDateTimeWithTimeAtStartOfDay = calendar.getTimeInMillis();
		Task task = calendarViewTaskOccurrence.Task;
		taskInformationStrings = informationUnitMatrix.createInformationComposerStrings(
				mContext, calendarViewTaskOccurrence);
		if (taskInformationStrings.size() > 0) {
			firstRow = taskInformationStrings.get(0);
		} else {
			firstRow = "";
		}
		String text1;
		if (calendarViewTaskOccurrence.StartDateTime != null) {
			if (calendarViewTaskOccurrence.EndDateTime != null) {
				if (calendarViewTaskOccurrence.StartDateTime <= dayStartDateTimeWithTimeAtStartOfDay
						&& calendarViewTaskOccurrence.EndDateTime >= dayEndDateTimeWithTimeAtStartOfDay) {
					text1 = "" + task.getName() + ": whole day";
				} else {
					String startTimeString = mDateFormatTimeInstance
							.format(new Date(
									calendarViewTaskOccurrence.StartDateTime <= dayStartDateTimeWithTimeAtStartOfDay ? dayStartDateTimeWithTimeAtStartOfDay
											: calendarViewTaskOccurrence.StartDateTime));
					String endTimeString = mDateFormatTimeInstance
							.format(new Date(
									dayEndDateTimeWithTimeAtStartOfDay <= calendarViewTaskOccurrence.EndDateTime ? dayEndDateTimeWithTimeAtStartOfDay
											: calendarViewTaskOccurrence.EndDateTime));
					text1 = "" + task.getName() + ": " + startTimeString + " - "
							+ endTimeString;
				}
			} else {
				if (calendarViewTaskOccurrence.StartDateTime > dayStartDateTimeWithTimeAtStartOfDay) {
					String startTimeString = mDateFormatTimeInstance.format(new Date(
							calendarViewTaskOccurrence.StartDateTime));
					String endTimeString = mDateFormatTimeInstance.format(new Date(
							dayEndDateTimeWithTimeAtStartOfDay));
					text1 = "" + task.getName() + ": " + startTimeString + " - "
							+ endTimeString;
				} else {
					text1 = "" + task.getName() + ": whole day";
				}
			}
		} else {
			if (calendarViewTaskOccurrence.EndDateTime != null) {
				if (calendarViewTaskOccurrence.EndDateTime < dayEndDateTimeWithTimeAtStartOfDay) {
					String startTimeString = mDateFormatTimeInstance.format(new Date(
							dayStartDateTimeWithTimeAtStartOfDay));
					String endTimeString = mDateFormatTimeInstance.format(new Date(
							calendarViewTaskOccurrence.EndDateTime));
					text1 = "" + task.getName() + ": " + startTimeString + " - "
							+ endTimeString;
				} else {
					text1 = "" + task.getName() + ": whole day";
				}
			} else {
				text1 = "" + task.getName() + ": whole day";
			}
		}
		String text = firstRow;
		if (mMarkSyncNeeded) {
			switch (SyncStatus.fromInt(task.getSyncStatusValue())) {
			case SYNCHRONIZED:
			default:
				break;
			case SYNC_UP_REQUIRED:
				text = "\u2191" + firstRow;
				break;
			case SYNC_DOWN_REQUIRED:
				text = "\u2193" + firstRow;
				break;
			}
		}
		tv.setText(text);
		int backgroundColor = task.getColor2(mContext);
		int textColor;
		if (Helper.getContrastYIQ(backgroundColor)) {
			textColor = ContextCompat.getColor(mContext,
					R.color.task_view_text_synchronized_dark);
		} else {
			textColor = ContextCompat.getColor(mContext,
					R.color.task_view_text_synchronized_light);
		}
		colorBox.setBackgroundColor(backgroundColor);
		tv.setBackgroundColor(backgroundColor);
		tv.setTextColor(textColor);
		return convertView;
	}

	/** returns a View to be displayed in the last row.
	 *
	 * @param groupPosition
	 * @param convertView
	 * @param parent
	 * @return */
	@Override
	protected View getFooterView(int groupPosition, View convertView, ViewGroup parent) {
		TextView textView = new TextView(mContext);
		// convertView.setFocusable(false);
		// convertView.setClickable(false);
		textView.setGravity(Gravity.CENTER);
		if (groupPosition >= serverListSize && serverListSize > 0) {
			// the ListView has reached the last row
			textView.setHint("Reached the last row.");
			return textView;
		} else {
			Calendar dateTimeOnVirtualGroupPosition = Calendar.getInstance();
			dateTimeOnVirtualGroupPosition
					.setTimeInMillis(mDateTimeOnVirtualMiddleOffset);
			dateTimeOnVirtualGroupPosition.add(Calendar.DAY_OF_YEAR, groupPosition
					- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
			textView.setHint("Loading...\n Date: "
					+ new Date(dateTimeOnVirtualGroupPosition.getTimeInMillis()));
			return textView;
		}
	}

	@Override
	public void onLoadMore(Integer virtualGroupPosition) {
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"onLoadMore thread: %s", Thread.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    virtualGroupPosition: %s", virtualGroupPosition));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    mLoadGroupAsyncTask: %s", mLoadGroupAsyncTask));
		}
		Integer virtualPosition = null;
		if (/* true || */mLoadGroupAsyncTask == null) {
			virtualPosition = launchGroupLoaderIfNeeded(virtualGroupPosition);
			if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format(
								"    launchGroupLoaderIfNeeded(virtualGroupPosition) returned: %s",
								virtualPosition));
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void launchTaskScrollVirtualGroupPositionToTop(int virtualGroupPosition) {
		mLoadGroupAsyncTask = new Object();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda()
					.getLoadGroupAsyncTask()
					.executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR,
							new LoadGroupAsyncTaskArguments(
									virtualGroupPosition,
									true,
									EndlessScrollBaseExpandableListAdapter.SCROLL_TO_THE_TOP_OF_THE_LOADED_GROUP,
									Helper.getFirstDayOfWeek(mContext)));
		} else {
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda()
					.getLoadGroupAsyncTask()
					.execute(
							new LoadGroupAsyncTaskArguments(
									virtualGroupPosition,
									true,
									EndlessScrollBaseExpandableListAdapter.SCROLL_TO_THE_TOP_OF_THE_LOADED_GROUP,
									Helper.getFirstDayOfWeek(mContext)));
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	Integer launchGroupLoaderIfNeeded(Integer virtualGroupPosition) {
		if (virtualGroupPosition != null) {
			mLoadGroupAsyncTask = new Object();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mRetainedFragmentForFragmentViewAgendaHolder
						.getRetainedFragmentForFragmentViewAgenda()
						.getLoadGroupAsyncTask()
						.executeOnExecutor(
								AsyncTask.THREAD_POOL_EXECUTOR,
								new LoadGroupAsyncTaskArguments(
										virtualGroupPosition,
										true,
										EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
										Helper.getFirstDayOfWeek(mContext)));
			} else {
				mRetainedFragmentForFragmentViewAgendaHolder
						.getRetainedFragmentForFragmentViewAgenda()
						.getLoadGroupAsyncTask()
						.execute(
								new LoadGroupAsyncTaskArguments(
										virtualGroupPosition,
										true,
										EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
										Helper.getFirstDayOfWeek(mContext)));
			}
			if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
						"launchGroupLoaderIfNeeded thread: %s", Thread.currentThread()
								.getName()));
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format("    return: %s", virtualGroupPosition));
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format("    mLoadGroupAsyncTask: %s", mLoadGroupAsyncTask));
			}
			return virtualGroupPosition;
		} else {
			ScrollPositionData data = mScrollPositionData;
			if (data == null) {
				mLoadGroupAsyncTask = null;
				return null;
			}
			if (data.FirstVisibleLoadedGroup != null
					&& data.FirstVisibleGroup < data.FirstVisibleLoadedGroup) {
				int currentVirtualGroup = data.FirstVisibleLoadedGroup - 1
						- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
				mLoadGroupAsyncTask = new Object();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mRetainedFragmentForFragmentViewAgendaHolder
							.getRetainedFragmentForFragmentViewAgenda()
							.getLoadGroupAsyncTask()
							.executeOnExecutor(
									AsyncTask.THREAD_POOL_EXECUTOR,
									new LoadGroupAsyncTaskArguments(
											currentVirtualGroup,
											true,
											EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
											Helper.getFirstDayOfWeek(mContext)));
				} else {
					mRetainedFragmentForFragmentViewAgendaHolder
							.getRetainedFragmentForFragmentViewAgenda()
							.getLoadGroupAsyncTask()
							.execute(
									new LoadGroupAsyncTaskArguments(
											currentVirtualGroup,
											true,
											EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
											Helper.getFirstDayOfWeek(mContext)));
				}
				if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
					Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
							"launchGroupLoaderIfNeeded thread: %s", Thread
									.currentThread().getName()));
					Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
							String.format("    return: %s", currentVirtualGroup));
					Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
							"    mLoadGroupAsyncTask: %s", mLoadGroupAsyncTask));
				}
				return currentVirtualGroup;
			}
			for (int currentGroup = data.FirstVisibleGroup; currentGroup <= data.LastVisibleGroup; currentGroup++) {
				int currentVirtualGroup = currentGroup
						- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
				if (!isGroupLoaded(currentVirtualGroup)) {
					mLoadGroupAsyncTask = new Object();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					} else {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.execute(
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					}
					if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("launchGroupLoaderIfNeeded thread: %s", Thread
										.currentThread().getName()));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
								String.format("    return: %s", currentVirtualGroup));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("    mLoadGroupAsyncTask: %s",
										mLoadGroupAsyncTask));
					}
					return currentVirtualGroup;
				}
			}
			int currentUpperGroup = data.FirstVisibleGroup - 1;
			int currentLowerGroup = data.LastVisibleGroup + 1;
			for (int i = 0; i < visibleThreshold; i++) {
				int currentVirtualGroup = currentUpperGroup
						- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
				if (!isGroupLoaded(currentVirtualGroup)) {
					mLoadGroupAsyncTask = new Object();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					} else {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.execute(
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					}
					if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("launchGroupLoaderIfNeeded thread: %s", Thread
										.currentThread().getName()));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
								String.format("    return: %s", currentVirtualGroup));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("    mLoadGroupAsyncTask: %s",
										mLoadGroupAsyncTask));
					}
					return currentVirtualGroup;
				}
				currentVirtualGroup = currentLowerGroup
						- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
				if (!isGroupLoaded(currentVirtualGroup)) {
					mLoadGroupAsyncTask = new Object();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					} else {
						mRetainedFragmentForFragmentViewAgendaHolder
								.getRetainedFragmentForFragmentViewAgenda()
								.getLoadGroupAsyncTask()
								.execute(
										new LoadGroupAsyncTaskArguments(
												currentVirtualGroup,
												true,
												EndlessScrollBaseExpandableListAdapter.RETAIN_CURRENT_SCROLL_POSITION,
												Helper.getFirstDayOfWeek(mContext)));
					}
					if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("launchGroupLoaderIfNeeded thread: %s", Thread
										.currentThread().getName()));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
								String.format("    return: %s", currentVirtualGroup));
						Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
								.format("    mLoadGroupAsyncTask: %s",
										mLoadGroupAsyncTask));
					}
					return currentVirtualGroup;
				}
				currentUpperGroup--;
				currentLowerGroup++;
			}
			mLoadGroupAsyncTask = null;
			if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
						"launchGroupLoaderIfNeeded thread: %s", Thread.currentThread()
								.getName()));
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format("    return: null"));
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format("    mLoadGroupAsyncTask: %s", mLoadGroupAsyncTask));
			}
			return null;
		}
	}

	// @Override
	// public ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>
	// loadVirtualGroupPosition(
	// int virtualGroupPosition) {
	// DateTime dateTimeOnVirtualGroupPosition = mDateTimeOnVirtualMiddleOffset
	// .plusDays(virtualGroupPosition);
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences =
	// getTaskOccurrences(dateTimeOnVirtualGroupPosition);
	// ArrayList<CalendarViewTaskOccurrence> entities = new
	// ArrayList<CalendarViewTaskOccurrence>();
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrences) {
	// entities.add(new CalendarViewTaskOccurrence(calendarViewTaskOccurrence));
	// }
	// ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> expandListGroup =
	// new ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>(
	// virtualGroupPosition, new ExpandListGroupData(
	// dateTimeOnVirtualGroupPosition.getMillis()), entities);
	// // mStateParameters2.mKnownChildrenCounts.put(virtualGroupPosition,
	// // expandListGroup
	// // .getItems().size());
	// return expandListGroup;
	// }
	// public int getFlatListGroupPositionForDate(long dateTime) {
	// // int groupPosition = Days.daysBetween(mDateTimeOnVirtualMiddleOffset,
	// // new DateTime(dateTime)).getDays()
	// // + EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
	// return mExpandList.getFlatListPosition(ExpandableListView
	// .getPackedPositionForGroup(Days.daysBetween(
	// new DateTime(mDateTimeOnVirtualMiddleOffset),
	// new DateTime(dateTime)).getDays()
	// + EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET));
	// }
	public int getVirtualGroupPositionForDate(long dateTime) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(mDateTimeOnVirtualMiddleOffset);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeInMillis(dateTime);
		return CalendarHelper.daysBetween(calendar1, calendar2);
	}

	public void setParameters(SyncPolicy syncPolicy,
			MarkSyncNeededPolicy markSyncNeededPolicy,
			InformationUnitMatrix informationUnitMatrix) {
		boolean markSyncNeeded;
		switch (markSyncNeededPolicy) {
		case ALWAYS:
			markSyncNeeded = true;
			break;
		case IF_SYNC_IS_SWITCHED_ON:
			markSyncNeeded = syncPolicy.equals(SyncPolicy.DO_SYNC);
			break;
		case NEVER:
		default:
			markSyncNeeded = false;
			break;
		}
		mMarkSyncNeeded = markSyncNeeded;
		this.informationUnitMatrix = informationUnitMatrix;
		notifyDataSetChanged();
	}

	public void setInformationUnitMatrix(InformationUnitMatrix informationUnitMatrix) {
		this.informationUnitMatrix = informationUnitMatrix;
	}
}