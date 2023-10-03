package com.example.proyectoedia.publicacion;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoedia.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdaptadorComentarios extends RecyclerView.Adapter<AdaptadorComentarios.MyHolder> {

    Context context;
    List<ModeloComentarios> listaComentarios;

    public AdaptadorComentarios(Context context, List<ModeloComentarios> listaComentarios) {
        this.context = context;
        this.listaComentarios = listaComentarios;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //El inflador
        View view = LayoutInflater.from(context).inflate(R.layout.lista_comentarios,parent,false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int i) {
            //obtener los datos
        String uid = listaComentarios.get(i).getUid();
        String nombre = listaComentarios.get(i).getuNombre();
       // String uid = listaComentarios.get(i).getuEmail();
        String cId = listaComentarios.get(i).getcId();
        String comentario = listaComentarios.get(i).getpComentario();
        String horaDia = listaComentarios.get(i).getHoraDia();
        String imagen = listaComentarios.get(i).getuDp();


        //Convertimos el tiempo a la fecha actual.
        /*Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(horaDia));
        String pTiempo = DateFormat.format("dd/MM/yyyy", calendar).toString();*/

        holder.nombreTv.setText(nombre);
        holder.comentarioTv.setText(comentario);
        holder.horaDiaTv.setText(horaDia);

        try{
            Picasso.get().load(imagen).placeholder(R.drawable.icon_person).into(holder.avatarIv);
        }catch (Exception e){

        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView nombreTv, comentarioTv, horaDiaTv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            avatarIv = itemView.findViewById(R.id.avatarIv);
            nombreTv = itemView.findViewById(R.id.nombreTv);
            comentarioTv = itemView.findViewById(R.id.comentarioTv);
            horaDiaTv = itemView.findViewById(R.id.fechaHoraTv);


        }
    }
}
