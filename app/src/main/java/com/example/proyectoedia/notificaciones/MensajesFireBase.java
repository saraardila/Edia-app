package com.example.proyectoedia.notificaciones;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.example.proyectoedia.menu.Chat.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MensajesFireBase extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //Obtener el usuario actual del shared preferences

        SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
        String guardarUsuarioActual = sp.getString("Actual_USERID","None");

        String enviado = remoteMessage.getData().get("enviar");
        String usuario = remoteMessage.getData().get("usuario");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if(firebaseUser != null && enviado.equals(firebaseUser.getUid())){

            if(!guardarUsuarioActual.equals(usuario)){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

                    enviarNotificacionTop(remoteMessage);
                }else{

                    enviarNotificacionNormal(remoteMessage);
                }
            }
        }
    }

    private void enviarNotificacionNormal(RemoteMessage remoteMessage) {

        String usuario = remoteMessage.getData().get("usuario");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(usuario.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("idUsuario",usuario);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        //Sonido de la notificacion
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if(i<0){
            j=i;
        }
        notificationManager.notify(j,builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void enviarNotificacionTop(RemoteMessage remoteMessage) {

        String usuario = remoteMessage.getData().get("usuario");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(usuario.replaceAll("[\\D]",""));
        Intent intent = new Intent(this, ChatActivity.class);
        Bundle bundle = new Bundle();

        bundle.putString("idUsuario",usuario);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this,i,intent,PendingIntent.FLAG_ONE_SHOT);

        //Sonido de la notificacion
        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Top_Notificacion notificacion2 = new Top_Notificacion(this);
        Notification.Builder builder = notificacion2.getNotifications(title,body,pIntent,defSoundUri,icon);
        int j = 0;
        if(i<0){
            j=i;
        }
        notificacion2.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        //actualizar el token del usuario
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            //entrar, actualizar token
            actualizarToken(s);
        }
    }

    private void actualizarToken(String tokenRefresh) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);

    }
}
