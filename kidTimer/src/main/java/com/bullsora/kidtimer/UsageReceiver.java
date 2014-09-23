package com.bullsora.kidtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UsageReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (ActivityMonitor.SCHEDULE_ACTION.equals(action)) {
      ActivityMonitor.checkSchedule(context);
    }

    if (ActivityMonitor.USAGE_ACTION.equals(action)) {
      ActivityMonitor.checkUsage(context);
    }
  }

}