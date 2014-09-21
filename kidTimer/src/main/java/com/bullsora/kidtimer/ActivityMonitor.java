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

  public static void fetchCurrentActivity(Context context) {
    ActivityManager
        activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(TASKS);
    String topTaskPackage = runningTasks.get(0).baseActivity.getPackageName();
    Log.i("ActivityMonitor", "Top activity is: " + topTaskPackage);

    if (EXCLUDED_TASKS.contains(topTaskPackage)) {
      return;
    }

//    SharedPreferences usage = context.getSharedPreferences("Usage", 0);
//    SharedPreferences.Editor usageEditor = usage.edit();
//    usageEditor.pu
//
    if (topTaskPackage.startsWith("com.android.mms")) {
      blockUsage(context, topTaskPackage);
    }
  }

  private static void blockUsage(Context context, String topActivityName) {
    Log.i("ActivityMonitor", "Block " + topActivityName);
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    context.startActivity(intent);
  }


}
