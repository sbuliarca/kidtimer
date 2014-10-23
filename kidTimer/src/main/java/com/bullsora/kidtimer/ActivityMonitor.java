package com.bullsora.kidtimer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
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
  public static final int MAX_USAGE_IN_SEC = 45 * 60 ;
  public static final String BLOCKING_WATCHDOG = "com.android.bullsora.blockUsage";
  public static final String SCHEDULE_ACTION = "com.android.bullsora.schedule";
  public static final String REMOTE_ACTION = "com.android.bullsora.remote";
  public static final String NEW_DAY_ACTION = "com.android.bullsora.newDay";
  public static final String TRACK_USAGE = "com.android.bullsora.trackUsage";
  public static final int MISSING_VALUE_MARKER = -1;

  private static List<String> EXCLUDED_TASKS = Arrays.asList(
    "com.android.launcher",
    "com.bullsora.kidtimer",
    "com.nick.kitkatlauncher",
    "com.cyanogenmod.trebuchet",
    "com.android.systemui"
  );

  private static String LAST_TASK_NAME;

  private static int LAST_TASK_USAGE = 0;

  private static boolean shouldReloadFromPrefs = true;

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
    if (shouldReloadFromPrefs) {
      restoreState(context);
      shouldReloadFromPrefs = false;
    }

    if (EXCLUDED_TASKS.contains(topTaskPackage)) {
      return;
    }
    Log.i("monitor", "Top package is " + topTaskPackage);

//    Log.i("Overrides", "BLock: " + isOverrideBlock + " Allow: " + isOverrideAllow);
    if (isOverrideAllow) {
      return;
    }

    if (isOverrideBlock || blockedOfSchedule || blockedOfUsage) {
      blockUsage(context);
    }
  }

  private static void backupState(Context context) {
    SharedPreferences.Editor editor = getLocalPrefs(context).edit();
    editor.putBoolean("blockOfSchedule", blockedOfSchedule);
    editor.putBoolean("blockOfUsage", blockedOfUsage);
    editor.putBoolean("isOverrideAllow", isOverrideAllow);
    editor.putBoolean("isOverrideBlock", isOverrideBlock);
    if (overrideStart != null) {
      editor.putLong("overrideStart", overrideStart);
    } else {
      editor.remove("overrideStart");
    }
    if (overrideMinutes != null) {
      editor.putInt("overrideMinutes", overrideMinutes);
    } else {
      editor.remove("overrideMinutes");
    }

    editor.commit();
  }

  private static void restoreState(Context context) {
    Log.i("inf", "Restoring state from prefs");
    SharedPreferences localPrefs = getLocalPrefs(context);

    blockedOfSchedule = localPrefs.getBoolean("blockOfSchedule", false);
    blockedOfUsage = localPrefs.getBoolean("blockOfUsage", false);
    isOverrideAllow = localPrefs.getBoolean("isOverrideAllow", false);
    isOverrideBlock = localPrefs.getBoolean("isOverrideBlock", false);
    totalUsage = localPrefs.getInt("totalUsage", 0);
    long overrideStartVal = localPrefs.getLong("overrideStart", MISSING_VALUE_MARKER);
    if (overrideStartVal != MISSING_VALUE_MARKER) {
      overrideStart = overrideStartVal;
    }

    int overrideMinutesVal = localPrefs.getInt("overrideMinutes", MISSING_VALUE_MARKER);
    if (overrideMinutesVal != MISSING_VALUE_MARKER) {
      overrideMinutes = overrideMinutesVal;
    }
  }

  private static String getTopTaskPackage(Context context) {
    ActivityManager
        activityManager =
        (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
    return runningTasks.get(0).baseActivity.getPackageName();
  }

  private static void resetOverrides(Context context) {
    overrideMinutes = null;
    overrideStart = null;
    isOverrideAllow = false;
    isOverrideBlock = false;
    backupState(context);
  }

  private static void dumpUsage(Context context) {
    if (!hasDataToDump) {
      return;
    }
    SharedPreferences.Editor usageEditor = getLocalPrefs(context).edit();
    usageEditor.putInt("totalUsage", totalUsage);
    usageEditor.commit();
    hasDataToDump = false;
  }

  private static SharedPreferences getLocalPrefs(Context context) {
    return context.getSharedPreferences("kid-timer", Context.MODE_PRIVATE);
  }


  public static void blockUsage(Context context) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

    context.startActivity(intent);
  }


  public static void checkSchedule() {
    if (blockedOfUsage) {
      return;
    }

    Calendar calendar = Calendar.getInstance();
    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
    /*  schedule blocking should not be in place in the week-end  */
    if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY) {
      blockedOfSchedule = false;
      return;
    }

    calendar.set(Calendar.HOUR_OF_DAY, 8);
    calendar.set(Calendar.MINUTE, 10);

    long leaveInTheMorning = calendar.getTimeInMillis();

    calendar.set(Calendar.HOUR_OF_DAY, 15);
    calendar.set(Calendar.MINUTE, 0);
    long afterSchool = calendar.getTimeInMillis();

    calendar.set(Calendar.HOUR_OF_DAY, 21);
    calendar.set(Calendar.MINUTE, 45);
    long bedTime = calendar.getTimeInMillis();

    /*  sleep for at least 8 hours. */
    calendar.set(Calendar.HOUR_OF_DAY, 7);
    calendar.set(Calendar.MINUTE, 30);
    long upInTheMorning = calendar.getTimeInMillis();

    long currentTime = System.currentTimeMillis();
    blockedOfSchedule = (currentTime > leaveInTheMorning && currentTime < afterSchool) || (currentTime < upInTheMorning) || (currentTime > bedTime);
  }

  public static void checkRemoteManagement(Context context) {
    try {
      HttpResponse response = httpClient.execute(httpGet);

      String responseString = getString(response.getEntity().getContent());
      if (responseString.trim().equals("")) {
        checkOverrideStillActive(context);
      } else {
        setNewOverride(responseString, context);
      }

    } catch (Exception e) {
//      Log.e("ActivityMonitor", "Error while fetching the remote overrides", e);
    }
  }

  private static void setNewOverride(String responseString, Context context) throws JSONException {
    JSONObject result = new JSONObject(responseString);
    overrideMinutes = result.getInt("minutes");
    String overrideType = result.getJSONObject("type").getString("name");
    isOverrideAllow = overrideType.equals("ALLOW");
    isOverrideBlock = overrideType.equals("DENY");

    overrideStart = System.currentTimeMillis();
    backupState(context);
  }

  private static void checkOverrideStillActive(Context context) {
    if (isOverrideBlock || isOverrideAllow) {
      long currentTimeMillis = System.currentTimeMillis();
      if (currentTimeMillis > overrideStart + (overrideMinutes * 60 * 1000)) {
        resetOverrides(context);
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

  public static void resetUsage(Context context) {
    totalUsage = 0;
    blockedOfUsage = false;
    backupState(context);
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
      backupState(context);
    }
  }

  public static void logOperatingFields()  {
    StringBuilder builder = new StringBuilder();
    builder.append("\nblock of schedule: ").append(blockedOfSchedule).append("\n");
    builder.append("totalUsage: ").append(totalUsage).append("\n");
    builder.append("blocked of usage: ").append(blockedOfUsage).append("\n");
    builder.append("override : [").append("allow: ").append(isOverrideAllow).append(", deny: ")
        .append(isOverrideBlock).append(", started at: ").append(overrideStart)
        .append(", for minutes: ").append(overrideMinutes).append("]\n");

    String message = builder.toString();
    Log.i("Fields", message);

    try {
      HttpPost remoteLogHttp = new HttpPost("http://bull-kidtimer.herokuapp.com/logEntry/save");
      remoteLogHttp.addHeader("Content-type", "application/json");
      remoteLogHttp.addHeader("Accept", "application/json");
      remoteLogHttp.setEntity(
          new StringEntity("{\"class\":\"com.bullsora.kidtimer.LogEntry\",\"message\":\"" + message
                           + " \"}"));
      HttpResponse response = httpClient.execute(remoteLogHttp);
      response.getEntity().consumeContent();
    } catch (Exception e) {
       // do nothing here
    }
  }
}
