package com.example.motionalarms;


import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.Calendar;

public class HomePage extends AppCompatActivity implements View.OnClickListener {
    public static LocationManager locationManager;
    public static PromptReminder promptReminder;
    public static UpdateProgress updateProgress;
    public static Calendar calendar;
    public CheckIfMoved checkIfMoved;
    public static Location location;
    public static AlarmManager alarmManager;
    public static final String NOTIFICATION_ID = "notification";
    public static final String GROUP_ID = "alarms_timers";
    public static int move_reminder_hours = 0;
    public static int move_reminder_minutes = 2;
    private AlertDialog alert;
    private boolean prompted = false;
    private static ProgressBar progressBar;
    public static long move_reminder_time;
    private boolean allowRun = true;
    public static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        progressBar = findViewById(R.id.move_progress);
        Button a = findViewById(R.id.new_alarm);
        Button t = findViewById(R.id.new_timer);
        Button m = findViewById(R.id.move_reminder);
        a.setOnClickListener(this);
        t.setOnClickListener(this);
        m.setOnClickListener(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        promptReminder = new PromptReminder();
        checkIfMoved = new CheckIfMoved();
        updateProgress = new UpdateProgress();
        setCalendar();
        makeChannel(getString(R.string.notification_name), getString(R.string.notification_description), NOTIFICATION_ID);
        checkGPS();
        accessLocation();
        updateProgress.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_alarm:
                new Thread() {
                    @Override
                    public void run(){
                        Intent a = new Intent(HomePage.this, CreateAlarm.class);
                        startActivity(a);
                    }}.start();
                break;
            case R.id.new_timer:
                new Thread() {
                    @Override
                    public void run(){
                        Intent t = new Intent(HomePage.this, CreateTimer.class);
                        startActivity(t);
                    }}.start();
                break;
            case R.id.move_reminder:
                new Thread() {
                    @Override
                    public void run(){
                        Intent m = new Intent(HomePage.this, MoveReminder.class);
                        startActivity(m);
                    }}.start();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }

    public void accessLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},11);
        }
        if (move_reminder_time == 0L) setCalendar();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, move_reminder_time, "", promptReminder, null);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, checkIfMoved);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    public static void setCalendar() {
        calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, move_reminder_hours);
        calendar.add(Calendar.MINUTE, move_reminder_minutes);
        move_reminder_time = calendar.getTimeInMillis();
        progressBar.setMax((move_reminder_hours * 3600) + (move_reminder_minutes * 60));
        progressBar.setProgress(progressBar.getMax());
    }

    private void checkGPS() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPS is disabled in your device. Would you like to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Goto Settings Page To Enable GPS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(callGPSSettingIntent);
                        }
                    });
            alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }

    private void makeChannel(String name, String description, String channel_id){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private class UpdateProgress extends Thread {

        @Override
        public void run() {
            while (progressBar.getProgress() > 0 && allowRun) {
                progressBar.incrementProgressBy(-1);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PromptReminder implements AlarmManager.OnAlarmListener {

        @Override
        public void onAlarm() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomePage.this);
            alertDialogBuilder.setMessage("Get up and walk!").setCancelable(false);
            alert = alertDialogBuilder.create();
            alert.show();
            prompted = true;
            allowRun = false;
        }
    }

    private class CheckIfMoved implements LocationListener{

        @Override
        public void onLocationChanged(Location newLocation) {
            alarmManager.cancel(promptReminder);
            setCalendar();
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, move_reminder_time, "", promptReminder, null);
            location = newLocation;
            if (prompted) {
                alert.dismiss();
                prompted = false;
                allowRun = true;
                updateProgress = new UpdateProgress();
                updateProgress.start();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Toast.makeText(HomePage.this, provider + "'s status changed to " + status + "!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(HomePage.this, "Provider " + provider + " enabled!", Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(HomePage.this, "Provider " + provider + " disabled!", Toast.LENGTH_SHORT).show();
        }
    }
}

