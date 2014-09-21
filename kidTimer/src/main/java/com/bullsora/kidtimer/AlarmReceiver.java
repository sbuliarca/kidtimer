package com.bullsora.kidtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {


  private final ActivityMonitor activityMonitor = new ActivityMonitor();

  @Override
  public void onReceive(Context context, Intent intent) {
    activityMonitor.fetchCurrentActivity(context);
  }

}