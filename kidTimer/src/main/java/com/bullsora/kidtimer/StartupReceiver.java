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
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    Intent usageIntend = new Intent(context, UsageReceiver.class);
    usageIntend.setAction(ActivityMonitor.USAGE_ACTION);
    PendingIntent usagePendingIntent = PendingIntent.getBroadcast(context, 0, usageIntend, 0);

    alarmManager.cancel(usagePendingIntent);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                              ActivityMonitor.SCHEDULE_PERIOD * 1000,
                              usagePendingIntent);

    Intent scheduleIntent = new Intent(context, UsageReceiver.class);
    scheduleIntent.setAction(ActivityMonitor.SCHEDULE_ACTION);
    PendingIntent schedulePendingIntent = PendingIntent.getBroadcast(context, 0, scheduleIntent, 0);

    alarmManager.cancel(schedulePendingIntent);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 5 * 1000,
                              schedulePendingIntent);

  }
}
