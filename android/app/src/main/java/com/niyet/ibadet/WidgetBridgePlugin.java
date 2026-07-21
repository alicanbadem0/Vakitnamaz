package com.niyet.ibadet;

import android.content.Context;
import android.content.SharedPreferences;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridgePlugin extends Plugin {

    @PluginMethod
    public void updateWidgetData(PluginCall call) {
        JSObject data = call.getData();
        if (data == null) {
            call.reject("Data is null");
            return;
        }

        // JS'ten gelen veriyi doğrudan Widget'ın okuyacağı hafızaya yazıyoruz
        SharedPreferences prefs = getContext().getSharedPreferences("CapacitorStorage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("vakitnamaz_state", data.toString());
        editor.apply();

        // Widget'a "Hemen ekranı yenile" emri gönderiyoruz
        VakitNamazWidget.updateAll(getContext());

        call.resolve();
    }
}