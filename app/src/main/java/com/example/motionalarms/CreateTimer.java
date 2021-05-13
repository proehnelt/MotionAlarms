package com.example.motionalarms;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class CreateTimer extends AppCompatActivity implements View.OnClickListener {
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_timer);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Button b = findViewById(R.id.timer_back);
        Button t = findViewById(R.id.create_new_timer);
        b.setOnClickListener(this);
        t.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_new_timer:
                Spinner hour = findViewById(R.id.timer_hour);
                Spinner minute = findViewById(R.id.timer_minute);
                EditText message = findViewById(R.id.timer_message);
                Calendar calendar = Calendar.getInstance();
                CheckBox location = findViewById(R.id.timer_use_location);
                int hours_from_now = Integer.valueOf(hour.getSelectedItem().toString());
                int minutes_from_now = Integer.valueOf(minute.getSelectedItem().toString());

                calendar.add(Calendar.MINUTE, minutes_from_now);
                calendar.add(Calendar.HOUR_OF_DAY, hours_from_now);

                if (message.getText().toString().equals("")) message.setText("A timer is going off");

                Toast.makeText(this, "TIMER SET", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MyBroadcastReceiver.class);
                intent.putExtra("message", message.getText().toString());
                intent.putExtra("title", "TIMER");

                boolean using_location = false;
                if (location.isChecked()) {
                    using_location = true;
                    intent.putExtra("location", HomePage.location);
                }
                intent.putExtra("using_location", using_location);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, HomePage.count++, intent, 0);

                long time = calendar.getTimeInMillis();
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            case R.id.timer_back:
                this.finish();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }
}
