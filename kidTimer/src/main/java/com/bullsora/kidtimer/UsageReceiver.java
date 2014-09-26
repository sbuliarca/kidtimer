package com.bullsora.kidtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class UsageReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (ActivityMonitor.SCHEDULE_ACTION.equals(action)) {
      ActivityMonitor.checkSchedule();
    } else if (ActivityMonitor.BLOCKING_WATCHDOG.equals(action)) {
      ActivityMonitor.blockUsageIfNecessary(context);
    } else if (ActivityMonitor.NEW_DAY_ACTION.equals(action)) {
      ActivityMonitor.resetUsage();
    } else if (ActivityMonitor.REMOTE_ACTION.equals(action)) {
      AsyncRemoteCheck asyncCheck = new AsyncRemoteCheck();
      asyncCheck.execute("");
    } else if (ActivityMonitor.TRACK_USAGE.equals(action)) {
      ActivityMonitor.trackUsage(context);
    }
  }

  private class AsyncRemoteCheck extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
      ActivityMonitor.checkRemoteManagement();
      return null;
    }
  }

}