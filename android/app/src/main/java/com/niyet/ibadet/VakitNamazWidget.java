package com.niyet.ibadet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class VakitNamazWidget extends AppWidgetProvider {

    private static final String TAG = "VakitNamazWidget";
    public static final String ACTION_AUTO_UPDATE = "com.niyet.ibadet.WIDGET_AUTO_UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        scheduleNextUpdate(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();

        if (ACTION_AUTO_UPDATE.equals(action) ||
                Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, VakitNamazWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public static void updateAll(Context context) {
        Intent intent = new Intent(context, VakitNamazWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        context.sendBroadcast(intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.vakitnamaz_widget);

        // Tarih ve Şehir Varsayılanı
        SimpleDateFormat sdf = new SimpleDateFormat("d MMMM", new Locale("tr"));
        views.setTextViewText(R.id.w_date, sdf.format(new Date()));
        views.setTextViewText(R.id.w_city, "İstanbul");

        try {
            SharedPreferences prefs = context.getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);

            String rawJson = prefs.getString("vakitnamaz_state", null);
            if (rawJson == null) rawJson = prefs.getString("niyet_state", null);
            if (rawJson == null) rawJson = prefs.getString("vakit_state", null);

            if (rawJson == null) {
                for (String key : prefs.getAll().keySet()) {
                    String val = prefs.getString(key, "");
                    if (val.contains("Imsak") || val.contains("imsak") || val.contains("Ogle") || val.contains("ogle")) {
                        rawJson = val;
                        break;
                    }
                }
            }

            if (rawJson != null) {
                JSONObject state = new JSONObject(rawJson);

                String city = state.optString("city", state.optString("şehir", ""));
                if (!city.isEmpty()) {
                    views.setTextViewText(R.id.w_city, city);
                }

                JSONObject times = state.optJSONObject("times");
                if (times == null) times = state.optJSONObject("vakitler");
                if (times == null) times = state;

                if (times != null) {
                    String imsak = getFlexibleTime(times, "Imsak", "imsak", "sabah");
                    String gunes = getFlexibleTime(times, "Gunes", "gunes", "güneş", "sunrise");
                    String ogle = getFlexibleTime(times, "Ogle", "ogle", "öğle");
                    String ikindi = getFlexibleTime(times, "Ikindi", "ikindi");
                    String aksam = getFlexibleTime(times, "Aksam", "aksam", "akşam");
                    String yatsi = getFlexibleTime(times, "Yatsi", "yatsi", "yatsı");

                    views.setTextViewText(R.id.w_t_sabah, imsak);
                    views.setTextViewText(R.id.w_t_ogle, ogle);
                    views.setTextViewText(R.id.w_t_ikindi, ikindi);
                    views.setTextViewText(R.id.w_t_aksam, aksam);
                    views.setTextViewText(R.id.w_t_yatsi, yatsi);

                    String[] labels = {"İmsak", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı"};
                    String[] values = {imsak, gunes, ogle, ikindi, aksam, yatsi};

                    long nowMillis = System.currentTimeMillis();
                    long nextVakitMillis = -1;
                    String nextVakitName = "";
                    String nextVakitTimeStr = "";

                    Calendar now = Calendar.getInstance();

                    for (int i = 0; i < values.length; i++) {
                        String timeStr = values[i];
                        if (timeStr != null && timeStr.contains(":")) {
                            String[] parts = timeStr.split(":");
                            int hour = Integer.parseInt(parts[0].trim());
                            int minute = Integer.parseInt(parts[1].trim());

                            Calendar target = Calendar.getInstance();
                            target.set(Calendar.HOUR_OF_DAY, hour);
                            target.set(Calendar.MINUTE, minute);
                            target.set(Calendar.SECOND, 0);
                            target.set(Calendar.MILLISECOND, 0);

                            if (target.before(now)) {
                                target.add(Calendar.DAY_OF_YEAR, 1);
                            }

                            if (nextVakitMillis == -1 || target.getTimeInMillis() < nextVakitMillis) {
                                nextVakitMillis = target.getTimeInMillis();
                                nextVakitName = labels[i];
                                nextVakitTimeStr = timeStr;
                            }
                        }
                    }

                    // --- KERAHAT VAKTİ KONTROLÜ ---
                    // Namaz kılmanın mekruh olduğu 3 zaman dilimi: güneş doğuşu (45 dk),
                    // zeval (öğleden 45 dk önce), güneş batışı (akşamdan 45 dk önce).
                    // Bu aralıktaysak sayaç öğle/akşama değil, kerahatin bitişine sayar.
                    boolean isKerahat = false;
                    if (gunes.contains(":") && ogle.contains(":") && aksam.contains(":")) {
                        int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                        int gunesMin = toMinutes(gunes);
                        int ogleMin = toMinutes(ogle);
                        int aksamMin = toMinutes(aksam);

                        int kerEnd = -1;
                        String kerLabel = "";
                        if (nowMin >= gunesMin && nowMin < gunesMin + 45) {
                            kerEnd = gunesMin + 45; kerLabel = "Güneşin Doğuşu";
                        } else if (nowMin >= ogleMin - 45 && nowMin < ogleMin) {
                            kerEnd = ogleMin; kerLabel = "Zeval Vakti";
                        } else if (nowMin >= aksamMin - 45 && nowMin < aksamMin) {
                            kerEnd = aksamMin; kerLabel = "Güneşin Batışı";
                        }

                        if (kerEnd != -1) {
                            isKerahat = true;
                            Calendar kerEndCal = (Calendar) now.clone();
                            kerEndCal.set(Calendar.HOUR_OF_DAY, (kerEnd / 60) % 24);
                            kerEndCal.set(Calendar.MINUTE, kerEnd % 60);
                            kerEndCal.set(Calendar.SECOND, 0);
                            kerEndCal.set(Calendar.MILLISECOND, 0);

                            nextVakitMillis = kerEndCal.getTimeInMillis();
                            nextVakitName = kerLabel;
                        }
                    }

                    // --- 1 DAKİKALIK KAYMAYI BİREBİR EŞİTLEYEN HESAPLAMA ---
                    if (nextVakitMillis != -1) {
                        long diffMillis = nextVakitMillis - nowMillis;
                        if (diffMillis < 0) diffMillis = 0;

                        long totalSeconds = diffMillis / 1000;
                        long hoursLeft = totalSeconds / 3600;
                        long minsLeft = (totalSeconds % 3600) / 60;

                        views.setTextViewText(R.id.w_next, isKerahat ? nextVakitName : (nextVakitName + " · " + nextVakitTimeStr));
                        views.setTextViewText(R.id.w_cd, String.format(Locale.getDefault(), "%02d:%02d", hoursLeft, minsLeft));

                        if (isKerahat) {
                            views.setTextViewText(R.id.w_label, "KERAHAT VAKTİ");
                            views.setInt(R.id.w_root, "setBackgroundResource", R.drawable.vakitnamaz_widget_bg_kerahat);
                        } else {
                            views.setTextViewText(R.id.w_label, "SIRADAKİ VAKİT");
                            views.setInt(R.id.w_root, "setBackgroundResource", R.drawable.vakitnamaz_widget_bg);
                        }
                    }
                }
            } else {
                views.setTextViewText(R.id.w_next, "Veri Bekleniyor");
                views.setTextViewText(R.id.w_cd, "--:--");
            }
        } catch (Exception e) {
            Log.e(TAG, "Widget veri okuma hatasi: ", e);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // "HH:mm" formatındaki saati gün içindeki dakikaya çevirir (kerahat hesabı için)
    private static int toMinutes(String time) {
        String[] p = time.split(":");
        return Integer.parseInt(p[0].trim()) * 60 + Integer.parseInt(p[1].trim());
    }

    private static String getFlexibleTime(JSONObject json, String... keys) {
        for (String key : keys) {
            if (json.has(key)) {
                return json.optString(key, "--:--");
            }
        }
        return "--:--";
    }

    private static void scheduleNextUpdate(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = buildAlarmPendingIntent(context);
        long nextMinuteMillis = ((System.currentTimeMillis() / 60000) + 1) * 60000;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinuteMillis, pendingIntent);
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinuteMillis, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextMinuteMillis, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextMinuteMillis, pendingIntent);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Alarm hatasi: ", e);
        }
    }

    // scheduleNextUpdate() ile BİREBİR AYNI Intent/requestCode/flag kombinasyonu —
    // PendingIntent eşleşmesi bu değerlere göre olur, cancel() için de aynısı gerekir.
    private static PendingIntent buildAlarmPendingIntent(Context context) {
        Intent intent = new Intent(context, VakitNamazWidget.class);
        intent.setAction(ACTION_AUTO_UPDATE);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags);
    }

    // Widget ana ekrandan TAMAMEN kaldırıldığında (son kopyası silindiğinde) çağrılır.
    // Bu olmadan, dakikada bir kendini tetikleyen alarm zinciri SONSUZA KADAR arka
    // planda çalışmaya devam eder — widget artık ekranda olmasa bile pil tüketir.
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(buildAlarmPendingIntent(context));
        }
    }
}