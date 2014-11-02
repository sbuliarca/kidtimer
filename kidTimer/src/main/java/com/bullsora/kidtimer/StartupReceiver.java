package com.bullsora.kidtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

public class StartupReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    Log.i("" + this, "Received boot event");
    startTasks(context);
  }

  public static void startTasks(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

    /*  schedule usage watchdog for 2 secs */
    Calendar calendar = Calendar.getInstance();
    long currentTime = calendar.getTimeInMillis();
    scheduleAlarmWithAction(context, alarmManager, 1000,
                            MonitoringController.BLOCKING_WATCHDOG, currentTime);

    /*  schedule usage tracking for 10 secs */
    scheduleAlarmWithAction(context, alarmManager, MonitoringController.USAGE_TRACKING_PERIOD * 1000,
                            MonitoringController.TRACK_USAGE, currentTime);

    /*  schedule "scheduled" usage for 1 minute */
    scheduleAlarmWithAction(context, alarmManager, 60 * 1000, MonitoringController.SCHEDULE_ACTION,
                            currentTime);

    /*  schedule remote management for 5 secs */
    scheduleAlarmWithAction(context, alarmManager, 5 * 1000, MonitoringController.REMOTE_ACTION,
                            currentTime);


    /*  schedule the new day action at 0:00 each day  */
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    scheduleAlarmWithAction(context, alarmManager, 24 * 60 * 60 * 1000,
                            MonitoringController.NEW_DAY_ACTION, calendar.getTimeInMillis());
  }

  private static void scheduleAlarmWithAction(Context context, AlarmManager alarmManager,
                                              int periodInSecs,
                                              String action, long startingTime) {
    Intent intent = new Intent(context, ScheduledActionsReceiver.class);
    intent.setAction(action);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    alarmManager.cancel(pendingIntent);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startingTime, periodInSecs,
                              pendingIntent);
  }
}
