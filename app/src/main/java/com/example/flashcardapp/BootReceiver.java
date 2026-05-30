package com.example.flashcardapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        boolean notifOn = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getBoolean("notif", false);
        if (notifOn) NotificationReceiver.schedule(context);
    }
}
