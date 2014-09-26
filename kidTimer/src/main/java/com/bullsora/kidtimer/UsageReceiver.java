package com.bullsora.kidtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class UsageReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (ActivityMonitor.SCHEDULE_ACTION.equals(action)) {
      ActivityMonitor.checkSchedule(context);
    } else if (ActivityMonitor.USAGE_ACTION.equals(action)) {
      ActivityMonitor.checkUsage(context);
    } else if (ActivityMonitor.REMOTE_ACTION.equals(action)) {
      AsyncRemoteCheck asyncCheck = new AsyncRemoteCheck();
      asyncCheck.execute("");
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