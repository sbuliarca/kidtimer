package com.bullsora.kidtimer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

public class ActivityMonitor {

  public static final int TASKS = 1;
  public static final int SCHEDULE_PERIOD = 1000;

  private static List<String> EXCLUDED_TASKS = Arrays.asList(
    "com.android.launcher",
    "com.bullsora.kidtimer"
  );

  public void fetchCurrentActivity(Context context) {
    ActivityManager
        activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(TASKS);
    String topActivityName = runningTasks.get(0).baseActivity.getPackageName();
    Log.i("ActivityMonitor", "Top activity is: " + topActivityName);

    if (topActivityName.startsWith("com.android.mms")) {
      Log.i("ActivityMonitor", "Block mms");
      Intent intent = new Intent(context, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      context.startActivity(intent);
    }
  }


}
