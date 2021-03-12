// Generated code from Butter Knife. Do not modify!
package com.dialog.wearables.activities;

import android.view.View;
import butterknife.ButterKnife.Finder;

public class MainActivity$$ViewInjector {
  public static void inject(Finder finder, final com.dialog.wearables.activities.MainActivity target, Object source) {
    View view;
    view = finder.findRequiredView(source, 2131296384, "field 'deviceListView'");
    target.deviceListView = (android.widget.ListView) view;
    view = finder.findRequiredView(source, 2131296621, "field 'mViewContainer'");
    target.mViewContainer = (android.widget.LinearLayout) view;
    view = finder.findRequiredView(source, 2131296523, "field 'mProgressView'");
    target.mProgressView = view;
  }

  public static void reset(com.dialog.wearables.activities.MainActivity target) {
    target.deviceListView = null;
    target.mViewContainer = null;
    target.mProgressView = null;
  }
}
