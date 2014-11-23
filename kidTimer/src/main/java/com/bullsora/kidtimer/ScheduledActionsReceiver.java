package com.bullsora.kidtimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class ScheduledActionsReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    if (MonitoringController.SCHEDULE_ACTION.equals(action)) {
      MonitoringController.checkSchedule(context);
      new AsyncRemoteLog().execute("");
    } else if (MonitoringController.BLOCKING_WATCHDOG.equals(action)) {
      MonitoringController.blockUsageIfNecessary(context);
    } else if (MonitoringController.NEW_DAY_ACTION.equals(action)) {
      MonitoringController.resetUsage(context);
    } else if (MonitoringController.REMOTE_ACTION.equals(action)) {
      AsyncRemoteCheck asyncCheck = new AsyncRemoteCheck();
      asyncCheck.execute(context);
    } else if (MonitoringController.TRACK_USAGE.equals(action)) {
      MonitoringController.trackUsage(context);
    }
  }

  private class AsyncRemoteCheck extends AsyncTask<Context, Void, Void> {

    @Override
    protected Void doInBackground(Context... params) {
      MonitoringController.checkRemoteManagement(params[0]);
      return null;
    }
  }

  private class AsyncRemoteLog extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... params) {
      MonitoringController.logOperatingFields();
      return null;
    }
  }

}