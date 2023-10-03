package com.example.proyectoedia.menu.Chat;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AdaptadorChat extends RecyclerView.Adapter<AdaptadorChat.MyHolder> {

    private static  final  int MSG_TYPE_LEFT = 0;
    private static  final  int MSG_TYPE_RIGHT = 1;
    Context context;
    List<ModeloChat> chatList;
    String imagenUrl;

    FirebaseUser fUser;

    public AdaptadorChat(Context context, List<ModeloChat> chatList, String imagenUrl) {
        this.context = context;
        this.chatList = chatList;
        this.imagenUrl = imagenUrl;

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //Condicional para controlar el fondo dependiendo si envias o recibes mensaje

        if(i==MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.columna_derecha_chat, viewGroup, false);
            return  new MyHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.columna_izquierda_chat, viewGroup, false);
            return  new MyHolder(view);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int i) {
        //-->>Recuperar los datos

        String mensaje = chatList.get(i).getMensaje();
        String fechaHora = chatList.get(i).getHoradia();

        //Convertir la fecha y hora en el formato Date
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        try {
            calendar.setTimeInMillis(Long.parseLong(fechaHora));
        }catch (Exception e){
            e.printStackTrace();;
        }
        String dateTime = android.text.format.DateFormat.format(" dd/MM/yyyy hh:mm aa  ",calendar).toString();

        //settear los datos

        holder.mensajeTv.setText(mensaje);
        holder.fechaHoraTv.setText(dateTime);
        try{
            Picasso.get().load(imagenUrl).into(holder.perfilChatIv);
        }catch (Exception e){
        }

        //-->> onClick dialog para el borrado de un mensaje
        /*holder.mensajeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //-->> Alerta para confirmar el borrado
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Borrado");
                builder.setMessage("¿Estás seguro de borrar este mensaje?");

                //Botón 'borrar'
               /* builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        eliminarMensaje(i);
                    }
                });

                //Botón 'cancelar'
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                //Crear y mostrar el cuadro de dialogo
                builder.create().show();

            }
        })*/;

        //settear el estado del mensaje

        if(i==chatList.size()-1){
            if(chatList.get(i).isVisto()){
                holder.vistoTv.setText("Visto");
            }else{
                holder.vistoTv.setText("Entregado");
            }
        }else{

            holder.vistoTv.setVisibility(View.GONE);
        }

    }

  /*  private void eliminarMensaje(int posicion) {

        final String myID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //-->> Comprobar la fecha del mensaje en la bbdd y si el elegido está en la conversación de ese chat
        //-->> Si es así, borramos el mensaje

        String fechayHora = chatList.get(posicion).getHoradia();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        Query query = dbRef.orderByChild("horadia").equalTo(fechayHora);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                for(DataSnapshot ds: datasnapshot.getChildren()){

                    //-->> Solo eliminar si el id del usuario actual coincide con quien lo está intentando borrar
                    if(ds.child("enviado").getValue().equals(myID)){

                        //Eliminar el mensaje del chat:
                        ds.getRef().removeValue(); //--> accedemos a la bbdd y borramos

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("mensaje", "Este mensaje fue eliminado...");
                        ds.getRef().updateChildren(hashMap); //-->> updateamos la bbdd por el mensaje de que fue eliminado

                        Toast.makeText(context, "Mensaje borrado", Toast.LENGTH_SHORT).show();

                    }else{
                        Toast.makeText(context, "Solo puedes eliminar tus mensajes", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }*/


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        //--->>comprobar si el usuario existe por su ID

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getEnviado().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }
    }

    class MyHolder extends RecyclerView.ViewHolder{

        //Vistas
        ImageView perfilChatIv;
        TextView mensajeTv, fechaHoraTv, vistoTv;
        LinearLayout mensajeLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //Inicializacion de vistas

            perfilChatIv = itemView.findViewById(R.id.perfilChatIv);
            mensajeTv = itemView.findViewById(R.id.mensajeTv);
            fechaHoraTv = itemView.findViewById(R.id.fechaHoraTv);
            vistoTv = itemView.findViewById(R.id.vistoTv);
            mensajeLayout = itemView.findViewById(R.id.mensajeLayout);

        }
    }
}
