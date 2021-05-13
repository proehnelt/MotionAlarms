package com.example.motionalarms;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateAlarm extends AppCompatActivity implements View.OnClickListener {
    private AlarmManager alarmManager;
    private Calendar calendar;
    private Spinner hour;
    private Spinner minute;
    private Spinner am_pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_alarm);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Button b = findViewById(R.id.alarm_back);
        Button a = findViewById(R.id.create_new_alarm);
        b.setOnClickListener(this);
        a.setOnClickListener(this);
        calendar = Calendar.getInstance();
        hour = findViewById(R.id.alarm_hour);
        minute = findViewById(R.id.alarm_minute);
        am_pm = findViewById(R.id.alarm_ampm);
        if (calendar.get(Calendar.HOUR) == 0) hour.setSelection(11);
        else hour.setSelection(calendar.get(Calendar.HOUR) - 1);
        minute.setSelection(calendar.get(Calendar.MINUTE));
        if (calendar.get(Calendar.AM_PM) > 0) am_pm.setSelection(1);
        else am_pm.setSelection(0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_new_alarm:
                CalendarView day_month_year = findViewById(R.id.date);
                EditText message = findViewById(R.id.alarm_message);
                CheckBox recurring = findViewById(R.id.recurring_alarm);
                CheckBox location = findViewById(R.id.alarm_use_location);
                int hour_of_day = Integer.valueOf(hour.getSelectedItem().toString());

                if (hour.getSelectedItem().equals("12") && am_pm.getSelectedItem().equals("am")) hour_of_day = 0;
                if (am_pm.getSelectedItem().equals("pm") && !(hour.getSelectedItem().equals("12"))) hour_of_day += 12;
                if (message.getText().toString().equals("")) message.setText("An alarm is going off");

                Toast.makeText(this, "ALARM SET", Toast.LENGTH_SHORT).show();
                calendar.setTimeInMillis(day_month_year.getDate());
                calendar.set(Calendar.HOUR_OF_DAY, hour_of_day);
                calendar.set(Calendar.MINUTE, Integer.valueOf(minute.getSelectedItem().toString()));
                Intent intent = new Intent(this, MyBroadcastReceiver.class);
                intent.putExtra("message", message.getText().toString());
                intent.putExtra("title", "ALARM");

                long time = (calendar.getTimeInMillis() - (calendar.getTimeInMillis() % 60000));
                if(System.currentTimeMillis() > time) {
                    if (Calendar.AM_PM == 0) time += 43200000;
                    else time += 86400000;
                }

                boolean using_location = false;
                if (location.isChecked()) {
                    using_location = true;
                    intent.putExtra("location", HomePage.location);
                }
                intent.putExtra("using_location", using_location);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, HomePage.count++, intent, 0);

                if (recurring.isChecked()) alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, 86400000, pendingIntent);
                else alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            case R.id.alarm_back:
                this.finish();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }
}
