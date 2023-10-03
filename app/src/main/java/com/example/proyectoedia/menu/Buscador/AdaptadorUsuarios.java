package com.example.proyectoedia.menu.Buscador;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoedia.R;
import com.example.proyectoedia.menu.perfil.PerfilListaPublicacionActivity;
import com.example.proyectoedia.menu.Chat.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdaptadorUsuarios extends RecyclerView.Adapter<AdaptadorUsuarios.MyHolder>{

    Context context;
    List<ModeloUsuarios> usuariosList;

    //--->>constructor
    public AdaptadorUsuarios(Context context, List<ModeloUsuarios> usuariosList) {
        this.context = context;
        this.usuariosList = usuariosList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        //-->>Inflador del layout lista usuarios

        View view = LayoutInflater.from(context).inflate(R.layout.lista_usuarios, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        final String idUsuario = usuariosList.get(i).getUid();

        //--> Traer los datos

        String imagenUsuario = usuariosList.get(i).getImagen();

        String nombreUsuario  = usuariosList.get(i).getName();

        final String  emailUsuario = usuariosList.get(i).getEmail();

        //--> Settear los datos

        myHolder.mNombreTv.setText(nombreUsuario);
        myHolder.mEmailTv.setText(emailUsuario);


        try{
            Picasso.get().load(imagenUsuario)
                    .placeholder(R.drawable.search_icon)
                    .into(myHolder.mAvatarIv);
        }catch (Exception e){

        }

        //-->>On Click

        myHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setItems(new String[]{"Perfil", "Chat"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if(which == 0){
                            //Click sobre perfil.
                            Intent intent = new Intent(context, PerfilListaPublicacionActivity.class);
                            intent.putExtra("uid", idUsuario);
                            context.startActivity(intent);
                        }
                        if(which == 1){
                            //Click sobre un usuario de la lista para comenzar a chatear con él
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("idUsuario", idUsuario);
                            context.startActivity(intent);
                        }
                    }
                });
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return usuariosList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView mAvatarIv;
        TextView mNombreTv, mEmailTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            mAvatarIv = itemView.findViewById(R.id.avatarIv);
            mNombreTv = itemView.findViewById(R.id.nombreTv);
            mEmailTv = itemView.findViewById(R.id.emailTv);

        }
    }

}
