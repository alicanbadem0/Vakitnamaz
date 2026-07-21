package com.niyet.ibadet;

import android.app.AlarmManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestBatteryOptimizationExemption();
        requestExactAlarmPermission();
    }

    // Pil optimizasyonundan muaf tutulma izni — ilk açılışta bir kez sorar
    private void requestBatteryOptimizationExemption() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.content.SharedPreferences prefs =
                        getSharedPreferences("vakitnamaz_native", MODE_PRIVATE);
                boolean alreadyAsked = prefs.getBoolean("battery_opt_asked", false);
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName()) && !alreadyAsked) {
                    prefs.edit().putBoolean("battery_opt_asked", true).apply();
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            // Bazı OEM'ler bu intent'i desteklemeyebilir — sessizce geç
        }
    }

    // "Alarmlar ve Hatırlatıcılar" izni Android 12 ve üzeri için gerekli — widget'ın dakikalık
    // güncelleme alarmı bu izin olmadan sistem tarafından geciktirilebilir.
    // Bu, pil optimizasyonundan AYRI bir izin — ikisi de gerekli.
    private void requestExactAlarmPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                android.content.SharedPreferences prefs =
                        getSharedPreferences("vakitnamaz_native", MODE_PRIVATE);
                boolean alreadyAsked = prefs.getBoolean("exact_alarm_asked", false);
                if (am != null && !am.canScheduleExactAlarms() && !alreadyAsked) {
                    prefs.edit().putBoolean("exact_alarm_asked", true).apply();
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        } catch (Exception e) {
            // Bazı OEM'ler bu intent'i desteklemeyebilir — sessizce geç
        }
    }
}
