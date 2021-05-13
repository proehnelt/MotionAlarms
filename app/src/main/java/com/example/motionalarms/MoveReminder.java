package com.example.motionalarms;

import android.app.AlarmManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MoveReminder extends AppCompatActivity implements View.OnClickListener {
    private Spinner hours;
    private Spinner minutes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_reminder);
        hours = findViewById(R.id.move_hour);
        minutes = findViewById(R.id.move_minute);
        hours.setSelection(HomePage.move_reminder_hours);
        minutes.setSelection(HomePage.move_reminder_minutes - 1);
        Button b = findViewById(R.id.move_back);
        Button m = findViewById(R.id.set_move_reminder);
        b.setOnClickListener(this);
        m.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_move_reminder:
                HomePage.move_reminder_hours = Integer.valueOf(hours.getSelectedItem().toString());
                HomePage.move_reminder_minutes = Integer.valueOf(minutes.getSelectedItem().toString());
                HomePage.setCalendar();
                HomePage.alarmManager.cancel(HomePage.promptReminder);
                HomePage.alarmManager.setExact(AlarmManager.RTC_WAKEUP, HomePage.move_reminder_time, "", HomePage.promptReminder, null);
                Toast.makeText(this, "REMINDER SET", Toast.LENGTH_SHORT).show();

            case R.id.move_back:
                MoveReminder.this.finish();
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + v.getId());
        }
    }
}
