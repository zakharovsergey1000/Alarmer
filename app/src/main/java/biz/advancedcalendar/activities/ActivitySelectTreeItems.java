package biz.advancedcalendar.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescriptionTaskImpl.TreeViewListItemDescriptionMatrix;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescriptionTaskImpl.TreeViewListItemDescriptionRow;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;

public class ActivitySelectTreeItems extends AppCompatActivity {
	// private static final String TREE_MANAGER_KEY =
	// "biz.advancedcalendar.TREE_MANAGER_KEY";
	// private static final String TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY =
	// "biz.advancedcalendar.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY";
	private static final String KEY_TREE_VIEW_LIST_ITEM_DESCRIPTION_MATRIX = "KEY_TREE_VIEW_LIST_ITEM_DESCRIPTION_MATRIX";
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	public static final String IntentExtraTitle = "title";
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionMatrix mTreeViewListItemDescriptionMatrix;
	private boolean mActionDonePerformed = false;
	private Toolbar mToolbar;

	/** This is a very simple adapter that provides very basic tree view with a checkboxes
	 * and simple item description. */
	private class FragmentViewTaskTreeAdapter extends AbstractTreeViewAdapter {
		private Map<Long, TreeViewListItemDescription> mTreeViewListItemDescriptions;
		// private Integer mResource;
		private Integer mTextViewResourceId;
		private boolean mMarkSyncNeeded;
		private ArrayList<String> taskInformationStrings;
		private String firstRow;
		private String secondRow;

		@SuppressLint("UseSparseArrays")
		public FragmentViewTaskTreeAdapter(final Activity treeViewListDemo,
				final TreeStateManager treeStateManager, final int numberOfLevels,
				int textViewResourceId) {
			super(treeViewListDemo, treeStateManager, numberOfLevels);
			mTreeViewListItemDescriptions = new HashMap<Long, TreeViewListItemDescription>();
			mTextViewResourceId = textViewResourceId;
		}

		@Override
		public View getNewChildView(final TreeNodeInfo treeNodeInfo) {
			final View viewLayout = getActivity().getLayoutInflater().inflate(
					mTextViewResourceId, null);
			return updateView(viewLayout, treeNodeInfo);
		}

		@Override
		public View updateView(final View view, final TreeNodeInfo treeNodeInfo) {
			TextView textView = (TextView) view;
			String text1 = mTreeViewListItemDescriptions.get(
					treeNodeInfo.getId().longValue()).getDescription();
			String text = text1;
			if (mMarkSyncNeeded) {
				switch (SyncStatus.fromInt(((Task) mTreeViewListItemDescriptions.get(
						treeNodeInfo.getId()).getTag()).getSyncStatusValue())) {
				case SYNCHRONIZED:
				default:
					break;
				case SYNC_UP_REQUIRED:
					text = "\u2191" + text1;
					break;
				case SYNC_DOWN_REQUIRED:
					text = "\u2193" + text1;
					break;
				}
			}
			int backgroundColor = ((Task) mTreeViewListItemDescriptions.get(
					treeNodeInfo.getId()).getTag()).getColor2(getActivity());
			int textColor;
			// Helper. getContrast50(backgroundColor);
			if (Helper.getContrastYIQ(backgroundColor)) {
				textColor = getActivity().getResources().getColor(
						R.color.task_view_text_synchronized_dark);
			} else {
				textColor = getActivity().getResources().getColor(
						R.color.task_view_text_synchronized_light);
			}
			textView.setBackgroundColor(backgroundColor);
			textView.setTextColor(textColor);
			textView.setText(text);
			view.setTag(treeNodeInfo.getId());
			return view;
		}

		@Override
		public long getItemId(final int position) {
			return getTreeId(position);
		}

		public void setTreeViewListItemDescriptions(
				TreeViewListItemDescriptionMatrix treeViewListItemDescriptionMatrix) {
			mTreeViewListItemDescriptions.clear();
			if (mTreeViewListItemDescriptionMatrix != null) {
				ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows = mTreeViewListItemDescriptionMatrix
						.getTreeViewListItemDescriptionRows();
				for (TreeViewListItemDescriptionRow treeViewListItemDescriptionRow : treeViewListItemDescriptionRows) {
					ArrayList<TreeViewListItemDescription> treeViewListItemDescriptions = treeViewListItemDescriptionRow
							.getTreeViewListItemDescriptions();
					for (TreeViewListItemDescription treeViewListItemDescription : treeViewListItemDescriptions) {
						mTreeViewListItemDescriptions.put(
								treeViewListItemDescription.getId(),
								treeViewListItemDescription);
					}
				}
			}
		}

		public void setMarkSyncParameters(SyncPolicy syncPolicy,
				MarkSyncNeededPolicy markSyncNeededPolicy) {
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
			notifyDataSetChanged();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tree_view_list);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		final ActionBar actionBar = getSupportActionBar();
		String title = getIntent().getStringExtra(
				ActivitySelectTreeItems.IntentExtraTitle);
		actionBar.setTitle(title);
		mTreeViewList = (TreeViewList) findViewById(R.id.tree_view_list);
		if (savedInstanceState == null) {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					fillAdapter(null, null);
				}
				mTreeViewList.setAdapter(mMySimpleAdapter);
			}
		} else {
			//
			if (mTreeViewListItemDescriptionMatrix == null) {
				mTreeViewListItemDescriptionMatrix = savedInstanceState
						.getParcelable(ActivitySelectTreeItems.KEY_TREE_VIEW_LIST_ITEM_DESCRIPTION_MATRIX);
				mManager = (TreeStateManager) savedInstanceState
						.getParcelable(CommonConstants.treeManager);
			}
			fillAdapter(mTreeViewListItemDescriptionMatrix, mManager);
			mTreeViewList.setAdapter(mMySimpleAdapter);
		}
		SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
				.getIntegerPreferenceValueFromStringArray(this,
						R.string.preference_key_sync_policy,
						R.array.sync_policy_values_array,
						R.integer.sync_policy_default_value));
		MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
				.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(this,
						R.string.preference_key_mark_sync_needed,
						R.array.mark_sync_needed_values_array,
						R.integer.mark_sync_needed_default_value));
		syncPolicy = SyncPolicy.DO_NOT_SYNC;
		markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
		mMySimpleAdapter.setMarkSyncParameters(syncPolicy, markSyncNeededPolicy);
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
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(
				ActivitySelectTreeItems.KEY_TREE_VIEW_LIST_ITEM_DESCRIPTION_MATRIX,
				mTreeViewListItemDescriptionMatrix);
		savedInstanceState.putParcelable(CommonConstants.treeManager, mManager);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			finish();
			return true;
		case CommonConstants.MENU_ID_OK:
			mActionDonePerformed = true;
			finish();
			return true;
		default:
			boolean superOnOptionsItemSelectedResult = super.onOptionsItemSelected(item);
			Log.d("DEBUG", "superOnOptionsItemSelectedResult: "
					+ superOnOptionsItemSelectedResult);
			return superOnOptionsItemSelectedResult;
		}
	}

	@Override
	public void finish() {
		Intent intent = getIntent();
		if (!mActionDonePerformed) {
			// Activity finished by cancel, return no data
			setResult(Activity.RESULT_CANCELED);
			super.finish();
			return;
		}
		// TreeViewList tvl = (TreeViewList) findViewById(R.id.tree_view_list);
		// long[] idArray = tvl.getCheckedItemIds();
		intent.putExtra(CommonConstants.ID_ARRAY,
				((TreeViewList) findViewById(R.id.tree_view_list)).getCheckedItemIds());
		// Activity finished ok, return the data
		setResult(Activity.RESULT_OK, intent);
		super.finish();
	}

	private void fillAdapter(
			TreeViewListItemDescriptionMatrix treeViewListItemDescriptionMatrix,
			TreeStateManager manager) {
		Bundle b = getIntent().getBundleExtra(
				CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE);
		if (treeViewListItemDescriptionMatrix != null) {
			mTreeViewListItemDescriptionMatrix = treeViewListItemDescriptionMatrix;
		} else {
			int bundleType = b
					.getInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE);
			switch (bundleType) {
			case CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE:
				Long excludeSubtree = null;
				if (b.containsKey(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE)) {
					excludeSubtree = b
							.getLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE);
				}
				boolean includeNonDeleted = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK);
				boolean includeDeleted = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK);
				boolean includeActive = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK);
				boolean includeCompleted = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK);
				mTreeViewListItemDescriptionMatrix = DataProvider
						.getTreeViewListItemDescriptionMatrix(null,
								getApplicationContext(), null, excludeSubtree,
								includeNonDeleted, includeDeleted, includeActive,
								includeCompleted);
				break;
			case CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_LABEL_TREE:
				excludeSubtree = null;
				if (b.containsKey(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE)) {
					excludeSubtree = b
							.getLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE);
				}
				includeNonDeleted = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_LABEL);
				includeDeleted = b
						.getBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_LABEL);
				mTreeViewListItemDescriptionMatrix = DataProvider
						.getTreeViewListItemDescriptionMatrixForLabel(null,
								getApplicationContext(), null, excludeSubtree, false,
								includeNonDeleted);
				break;
			default:
				break;
			}
		}
		if (manager != null) {
			mManager = manager;
		} else {
			mManager = manager = new InMemoryTreeStateManager();
			final TreeBuilder treeBuilder = new TreeBuilder(manager);
			if (mTreeViewListItemDescriptionMatrix != null) {
				ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows = mTreeViewListItemDescriptionMatrix
						.getTreeViewListItemDescriptionRows();
				for (TreeViewListItemDescriptionRow treeViewListItemDescriptionRow : treeViewListItemDescriptionRows) {
					ArrayList<TreeViewListItemDescription> treeViewListItemDescriptions = treeViewListItemDescriptionRow
							.getTreeViewListItemDescriptions();
					for (TreeViewListItemDescription treeViewListItemDescription : treeViewListItemDescriptions) {
						treeBuilder.sequentiallyAddNextNode(
								treeViewListItemDescription.getId(),
								treeViewListItemDescription.getDeepLevel());
					}
				}
			}
		}
		int choiceMode = b.getInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE);
		mTreeViewList.setChoiceMode(choiceMode);
		switch (choiceMode) {
		case AbsListView.CHOICE_MODE_SINGLE:
			mMySimpleAdapter = new FragmentViewTaskTreeAdapter(this, manager,
					ActivitySelectTreeItems.LEVEL_NUMBER,
					android.R.layout.simple_list_item_single_choice);
			break;
		case AbsListView.CHOICE_MODE_MULTIPLE:
		case AbsListView.CHOICE_MODE_MULTIPLE_MODAL:
			mMySimpleAdapter = new FragmentViewTaskTreeAdapter(this, manager,
					ActivitySelectTreeItems.LEVEL_NUMBER,
					android.R.layout.simple_list_item_multiple_choice);
			break;
		default:
			break;
		}
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionMatrix);
	}
}
