package com.bullsora;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class SchedulerService extends IntentService {


  public static final int TASKS = 1;
  public static final int SCHEDULE_PERIOD = 2 * 1000;

  private static SchedulerService instance = null;

  public static boolean isInstanceCreated() {
    return instance != null;
  }//met

  @Override
  public void onCreate() {
    Log.i("Scheduler", "Service created, instance set");
    super.onCreate();
    instance = this;
  }

  @Override
  public void onDestroy()
  {
    super.onDestroy();
    instance = null;
  }

  private Handler repeatingScheduler = new Handler();


  private Runnable getCurrentAppTask = new Runnable() {
    @Override
    public void run() {

      fetchCurrentActivity();

      repeatingScheduler.postDelayed(getCurrentAppTask, SCHEDULE_PERIOD);
    }
  };

  public SchedulerService(String name) {
    super(name);
  }

  public SchedulerService() {
    super("KidsScheduler");
  }

  private void fetchCurrentActivity() {
    ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
    List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(TASKS);
    String topActivityName = runningTasks.get(0).baseActivity.getPackageName();
    Log.i("Scheduler", "Top activity is: " + topActivityName);

    if (topActivityName.startsWith("com.android.mms")) {
      Log.i("Shceduler", "Block mms");
      Intent intent = new Intent(this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

      startActivity(intent);
    }
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i("Scheduler", "The scheduler has started");
    getCurrentAppTask.run();
  }
}
