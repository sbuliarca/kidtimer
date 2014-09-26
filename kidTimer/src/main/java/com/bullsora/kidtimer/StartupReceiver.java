package com.bullsora.kidtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.i("" + this, "Received boot event");
    startTasks(context);
  }

  public static void startTasks(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    /*  schedule usage monitor for 2 secs */
    scheduleAlarmWithAction(context, alarmManager, ActivityMonitor.SCHEDULE_PERIOD * 1000,
                            ActivityMonitor.USAGE_ACTION);

    scheduleAlarmWithAction(context, alarmManager, 60 * 1000, ActivityMonitor.SCHEDULE_ACTION);

    scheduleAlarmWithAction(context, alarmManager, 5 * 1000, ActivityMonitor.REMOTE_ACTION);
  }

  private static void scheduleAlarmWithAction(Context context, AlarmManager alarmManager, int periodInSecs,
                                       String action) {
    Intent intent = new Intent(context, UsageReceiver.class);
    intent.setAction(action);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    alarmManager.cancel(pendingIntent);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), periodInSecs,
                              pendingIntent);
  }
}
