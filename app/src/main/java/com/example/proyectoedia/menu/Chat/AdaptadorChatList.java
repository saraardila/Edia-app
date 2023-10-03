package com.example.proyectoedia.menu.Chat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoedia.R;
import com.example.proyectoedia.menu.Buscador.ModeloUsuarios;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class AdaptadorChatList extends RecyclerView.Adapter<AdaptadorChatList.MyHolder> {

    Context context;
    List<ModeloUsuarios> usuariosList;
    private HashMap<String, String> ultimoMensajeMap;

    public AdaptadorChatList(Context context, List<ModeloUsuarios> usuariosList) {
        this.context = context;
        this.usuariosList = usuariosList;
        //this.ultimoMensajeMap = ultimoMensajeMap;
        ultimoMensajeMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(context).inflate(R.layout.lista_chat_list, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        final String idUsuario2 = usuariosList.get(i).getUid();
        String usuarioImagen = usuariosList.get(i).getImagen();
        String nombreUsuario = usuariosList.get(i).getName();
        //String nombreUsuario = usuariosList.get(i).getNombre();
        String ultimoMensaje = ultimoMensajeMap.get(idUsuario2);

        myHolder.nombreTv.setText(nombreUsuario);

        if(ultimoMensaje == null || ultimoMensaje.equals("default")){
        //if(ultimoMensaje != null){
            myHolder.ultimoMensajeTv.setVisibility(View.GONE);
        }else{
            myHolder.ultimoMensajeTv.setVisibility(View.VISIBLE);
            myHolder.ultimoMensajeTv.setText(ultimoMensaje);
        }

        try {
            Picasso.get().load(usuarioImagen).placeholder(R.drawable.icon_person).into(myHolder.perfilImagenIv);
        }catch (Exception e){
            Picasso.get().load(R.drawable.icon_person).into(myHolder.perfilImagenIv);
        }

        if(usuariosList.get(i).getEstado().equals("online")){
            // Picasso.get().load(R.drawable.circulo_online).into(myHolder.estadoIv);
            myHolder.estadoIv.setImageResource(R.drawable.circulo_online);
        }else{
            //Picasso.get().load(R.drawable.circulo_offline).into(myHolder.estadoIv);
            myHolder.estadoIv.setImageResource(R.drawable.circulo_offline);
        }

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("idUsuario", idUsuario2);
                context.startActivity(intent);
            }
        });
    }

    public  void  fijarUltimoMensaje(String userId, String ultimoMensaje){

        ultimoMensajeMap.put(userId, ultimoMensaje);
    }

    @Override
    public int getItemCount() {
        return usuariosList.size();
    }


    class MyHolder extends RecyclerView.ViewHolder{

        ImageView perfilImagenIv, estadoIv;
        TextView nombreTv, ultimoMensajeTv;

        public MyHolder(@NonNull View itemView){
            super(itemView);

            perfilImagenIv = itemView.findViewById(R.id.perfilImagenIv);
            estadoIv = itemView.findViewById(R.id.estadoIv);
            nombreTv = itemView.findViewById(R.id.nombreTv);
            ultimoMensajeTv = itemView.findViewById(R.id.ultimoMensajeTv);
        }
    }
}
