package biz.advancedcalendar.activities.accessories;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class OnTabSelectedListener implements TabLayout.OnTabSelectedListener {
	public interface SelectedIndexChangedListener {
		void onSelectedIndexChanged(int index);
	}

	public static class TabTag {
		public String FragmentTag;
		public Class<? extends Fragment> Clazz;
		public int ContainerId;
		public AppCompatActivity AppCompatActivity;
		public Bundle Bundle;
		public boolean UnselectSelectTabOnTabReselected;

		public TabTag(String fragmentTag, Class<? extends Fragment> clazz,
				int containerId,
				android.support.v7.app.AppCompatActivity appCompatActivity,
				android.os.Bundle bundle, boolean unselectSelectTabOnTabReselected) {
			super();
			FragmentTag = fragmentTag;
			Clazz = clazz;
			ContainerId = containerId;
			AppCompatActivity = appCompatActivity;
			Bundle = bundle;
			UnselectSelectTabOnTabReselected = unselectSelectTabOnTabReselected;
		}
	}

	private Fragment attachedFragment;
	private final SelectedIndexChangedListener mSelectedIndexChangedListener;

	/** Constructor used each time a new tab is created.
	 *
	 * @param activity
	 *            The host Activity, used to instantiate the fragment
	 * @param tag
	 *            The identifier tag for the fragment
	 * @param clz
	 *            The fragment's Class, used to instantiate the fragment
	 * @param viewGroupId
	 *            The ViewGroup into which to insert the fragment
	 * @param selectedIndexChangedListener
	 *            The selectedIndexChangedListener
	 * @param bundle
	 *            The bundle */
	public OnTabSelectedListener(SelectedIndexChangedListener selectedIndexChangedListener) {
		mSelectedIndexChangedListener = selectedIndexChangedListener;
		attachedFragment = null;
	}

	/* The following are each of the ActionBar.TabListener callbacks */
	@Override
	public void onTabSelected(Tab tab) {
		TabTag tabTag = (TabTag) tab.getTag();
		String mTag = tabTag.FragmentTag;
		Class<? extends Fragment> mClass = tabTag.Clazz;
		int mViewGroupId = tabTag.ContainerId;
		AppCompatActivity mActivity = tabTag.AppCompatActivity;
		Bundle mBundle = tabTag.Bundle;
		FragmentManager supportFragmentManager = mActivity.getSupportFragmentManager();
		FragmentTransaction ft = supportFragmentManager.beginTransaction();
		Fragment fragment = supportFragmentManager.findFragmentByTag(mTag);
		// Check if we have some fragment already attached
		if (attachedFragment != null) {
			ft.detach(attachedFragment);
		}
		// Check if the fragment is already initialized
		if (fragment == null) {
			// If not, instantiate and add it to the activity
			fragment = Fragment.instantiate(mActivity, mClass.getName());
			fragment.setArguments(mBundle);
			ft.add(mViewGroupId, fragment, mTag);
		} else {
			ft.attach(fragment);
		}
		attachedFragment = fragment;
		ft.commit();
		if (mSelectedIndexChangedListener != null) {
			mSelectedIndexChangedListener.onSelectedIndexChanged(tab.getPosition());
		}
	}

	@Override
	public void onTabUnselected(Tab tab) {
		TabTag tabTag = (TabTag) tab.getTag();
		FragmentTransaction ft = tabTag.AppCompatActivity.getSupportFragmentManager()
				.beginTransaction();
		// Fragment fragment =
		// mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
		// Check if the fragment is already initialized
		if (attachedFragment != null) {
			// Detach the fragment, because another one is being attached
			ft.detach(attachedFragment);
			attachedFragment = null;
		}
		ft.commit();
		if (mSelectedIndexChangedListener != null) {
			mSelectedIndexChangedListener.onSelectedIndexChanged(-1);
		}
	}

	@Override
	public void onTabReselected(Tab tab) {
		// User selected the already selected tab. Usually do nothing.
		TabTag tabTag = (TabTag) tab.getTag();
		if (tabTag.UnselectSelectTabOnTabReselected) {
			tabTag.UnselectSelectTabOnTabReselected = false;
			onTabUnselected(tab);
			onTabSelected(tab);
		}
	}
}
