package com.bullsora.kidtimer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ActivityMonitor {

  public static final int USAGE_TRACKING_PERIOD = 10;
  public static final int MAX_USAGE_IN_SEC = 45 * 60 * 60;
  public static final String BLOCKING_WATCHDOG = "com.android.bullsora.blockUsage";
  public static final String SCHEDULE_ACTION = "com.android.bullsora.schedule";
  public static final String REMOTE_ACTION = "com.android.bullsora.remote";
  public static final String NEW_DAY_ACTION = "com.android.bullsora.newDay";
  public static final String TRACK_USAGE = "com.android.bullsora.trackUsage";

  private static List<String> EXCLUDED_TASKS = Arrays.asList(
    "com.android.launcher",
    "com.bullsora.kidtimer",
    "com.nick.kitkatlauncher"
  );

  private static String LAST_TASK_NAME;

  private static int LAST_TASK_USAGE = 0;

  private static boolean blockedOfSchedule;

  private static boolean blockedOfUsage;

  private static int totalUsage = 0;

  private static final int DUMP_USAGE_STATS_CYCLES = 60;

  private static boolean hasDataToDump;
  private static int runCycle = 0;
  private static DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

  public static final HttpGet
      httpGet = new HttpGet("http://bull-kidtimer.herokuapp.com/override/consume");

  static {
    httpGet.setHeader("Accept", "application/json");
  }

  private static Integer overrideMinutes;
  private static Long overrideStart;
  private static boolean isOverrideBlock;
  private static boolean isOverrideAllow;

  public static void blockUsageIfNecessary(Context context) {
    String topTaskPackage = getTopTaskPackage(context);
    Log.i("monitor", "Top package is " + topTaskPackage);
    if (EXCLUDED_TASKS.contains(topTaskPackage)) {
      return;
    }

//    Log.i("Overrides", "BLock: " + isOverrideBlock + " Allow: " + isOverrideAllow);
    if (isOverrideAllow) {
      return;
    }

    if (isOverrideBlock || blockedOfSchedule || blockedOfUsage) {
      blockUsage(context);
    }
  }

  private static String getTopTaskPackage(Context context) {
    ActivityManager
        activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
    return runningTasks.get(0).baseActivity.getPackageName();
  }

  private static void resetOverrides() {
    overrideMinutes = null;
    overrideStart = null;
    isOverrideAllow = false;
    isOverrideBlock = false;
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


  public static void blockUsage(Context context) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    context.startActivity(intent);
  }


  public static void checkSchedule() {
    if (blockedOfUsage) {
      return;
    }

    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 10);

    long stopUsageAt = calendar.getTimeInMillis();

    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 0);
    long resumeUsageAt = calendar.getTimeInMillis();

    long currentTime = System.currentTimeMillis();
    blockedOfSchedule = currentTime > stopUsageAt && currentTime < resumeUsageAt;
  }

  public static void checkRemoteManagement() {
    try {
      HttpResponse response = httpClient.execute(httpGet);

      String responseString = getString(response.getEntity().getContent());
      if (responseString.trim().equals("")) {
        checkOverrideStillActive();
      } else {
        setNewOverride(responseString);
      }

    } catch (Exception e) {
//      Log.e("ActivityMonitor", "Error while fetching the remote overrides", e);
    }
  }

  private static void setNewOverride(String responseString) throws JSONException {
    JSONObject result = new JSONObject(responseString);
    overrideMinutes = result.getInt("minutes");
    String overrideType = result.getJSONObject("type").getString("name");
    isOverrideAllow = overrideType.equals("ALLOW");
    isOverrideBlock = overrideType.equals("DENY");

    overrideStart = System.currentTimeMillis();
  }

  private static void checkOverrideStillActive() {
    if (isOverrideBlock || isOverrideAllow) {
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis > overrideStart + (overrideMinutes * 60 * 1000)) {
        resetOverrides();
      }
    }
  }

  private static String getString(InputStream content) throws IOException {
    BufferedReader r = new BufferedReader(new InputStreamReader(content));
    StringBuilder total = new StringBuilder();
    String line;
    while ((line = r.readLine()) != null) {
      total.append(line);
    }
    return total.toString();
  }

  public static void resetUsage() {
    totalUsage = 0;
    blockedOfUsage = false;
  }

  public static void trackUsage(Context context) {
    String topTaskPackage = getTopTaskPackage(context);

    if (EXCLUDED_TASKS.contains(topTaskPackage)) {
      dumpUsage(context);
      return;
    }

    runCycle++;
    totalUsage += USAGE_TRACKING_PERIOD;
    hasDataToDump = true;
//    Log.i("Manager", "Total usage " + totalUsage);

    if (runCycle == DUMP_USAGE_STATS_CYCLES) {
      dumpUsage(context);
      runCycle = 0;
    }

    if (totalUsage > MAX_USAGE_IN_SEC) {
      blockedOfUsage = true;
    }
  }
}
