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
  public static final int SCHEDULE_PERIOD = 1;
  public static final int MAX_USAGE_IN_SEC = 45 * 60 * 60;

  private static List<String> EXCLUDED_TASKS = Arrays.asList(
    "com.android.launcher",
    "com.bullsora.kidtimer"
  );

  private static String LAST_TASK_NAME;

  private static int LAST_TASK_USAGE = 0;

  private static int totalUsage = 0;

  private static final int DUMP_USAGE_STATS_CYCLES = 60;

  private static boolean hasDataToDump;
  private static int runCycle = 0;

  public static void fetchCurrentActivity(Context context) {
    ActivityManager
        activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(TASKS);
    String topTaskPackage = runningTasks.get(0).baseActivity.getPackageName();
    Log.i("ActivityMonitor", "Top activity is: " + topTaskPackage);

    if (EXCLUDED_TASKS.contains(topTaskPackage)) {
      dumpUsage(context);
      return;
    }

    runCycle++;
    totalUsage += SCHEDULE_PERIOD;
    hasDataToDump = true;
    Log.i("Manager", "Total usage " + totalUsage);

    if (runCycle == DUMP_USAGE_STATS_CYCLES) {
      dumpUsage(context);
      runCycle = 0;
    }

    if (totalUsage > MAX_USAGE_IN_SEC) {
      blockUsage(context, topTaskPackage);
    }
  }

  private static void dumpUsage(Context context) {
    if (!hasDataToDump) {
      return;
    }

    SharedPreferences usage = context.getSharedPreferences("Usage", 0);
    SharedPreferences.Editor usageEditor = usage.edit();
    usageEditor.putFloat("total", totalUsage);
    usageEditor.commit();
    hasDataToDump = false;
  }


  private static void blockUsage(Context context, String topActivityName) {
    Log.i("ActivityMonitor", "Block " + topActivityName);
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    context.startActivity(intent);
  }


}
