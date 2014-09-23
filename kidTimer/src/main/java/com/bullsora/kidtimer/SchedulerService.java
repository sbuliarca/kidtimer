package com.bullsora.kidtimer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class SchedulerService extends IntentService {


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

      ActivityMonitor.checkUsage(SchedulerService.this);

      repeatingScheduler.postDelayed(getCurrentAppTask, ActivityMonitor.SCHEDULE_PERIOD * 1000);
    }
  };

  public SchedulerService(String name) {
    super(name);
  }

  public SchedulerService() {
    super("KidsScheduler");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i("Scheduler", "The scheduler has started");
    getCurrentAppTask.run();
  }
}
