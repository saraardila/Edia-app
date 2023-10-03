package com.example.proyectoedia.notificaciones;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Top_Notificacion extends ContextWrapper {

    private static  final  String ID = "algun_id";

    private static final String NAME ="FirebaseAPP";

    private NotificationManager notificationManager;

    public Top_Notificacion(Context base) {
        super(base);
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            crearCanal();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void crearCanal() { //-->> Las acciones del movil cuando llega la notificacion

        NotificationChannel notificationChannel = new NotificationChannel(ID, NAME, NotificationManager.IMPORTANCE_DEFAULT);
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager(){
        if(notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getNotifications(String title, String body, PendingIntent pIntent,
                                                 Uri soundUri, String icon){
        return  new Notification.Builder(getApplicationContext(),ID)
                .setContentIntent(pIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setSmallIcon(Integer.parseInt(icon));
    }
}
