package com.example.proyectoedia.menu.campana;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoedia.R;
import com.example.proyectoedia.publicacion.ComentariosActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdaptadorCampana extends RecyclerView.Adapter<AdaptadorCampana.HolderNotificacion>{

    private Context context;
    private ArrayList<ModeloCampana> notificacionList;
    private FirebaseAuth firebaseAuth;

    public AdaptadorCampana(Context context, ArrayList<ModeloCampana> notificacionList) {
        this.context = context;
        this.notificacionList = notificacionList;

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderNotificacion onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.lista_campana, parent, false);

        return new HolderNotificacion(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderNotificacion holder, int position) {

        final ModeloCampana model = notificacionList.get(position);
        String name = model.getsName();
        String notificacion = model.getNotificacion();
        String image = model.getsImage();
        final String timestamp = model.getTimestamp();
        String senderUid = model.getsUid();
        final String pId = model.getpId();

        //Convertimos el tiempo a la fecha actual.
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(Long.parseLong(timestamp));
        String pTiempo = DateFormat.format("dd/MM/yyyy", calendar).toString();

        //obtendremos el nombre, email, imagen del usuario de la notificación a partir de su uid
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(senderUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String image = ""+ds.child("imagen").getValue();
                            String email = ""+ds.child("email").getValue();

                            model.setsName(name);
                            model.setsEmail(email);
                            model.setsImage(image);

                            holder.nameTv.setText(name);

                            try{
                                Picasso.get().load(image).placeholder(R.drawable.icon_default2).into(holder.avatarIv);
                            }catch (Exception e){
                                holder.avatarIv.setImageResource(R.drawable.icon_default2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.notificacionTv.setText(notificacion);
        holder.timeTv.setText(pTiempo);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ComentariosActivity.class);
                intent.putExtra("postId", pId); //-- para tener los detalles de los post
                context.startActivity(intent);
            }
        });

        //pulsación larga para mostrar la opción de notificación de eliminación
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Borrar");
                builder.setMessage("¿Estás seguro que quiere borrar la notificación?");
                builder.setPositiveButton("Borrar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                       DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                       ref.child(firebaseAuth.getUid()).child("Notificaciones").child(timestamp)
                               .removeValue()
                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                           @Override
                           public void onSuccess(Void aVoid) {
                               Toast.makeText(context, "Borrando notificacion...", Toast.LENGTH_SHORT).show();
                           }
                       })
                               .addOnFailureListener(new OnFailureListener() {
                                   @Override
                                   public void onFailure(@NonNull Exception e) {
                                       Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                   }
                               });
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return false;
            }
        });

        try{
            Picasso.get().load(image).placeholder(R.drawable.icon_default2).into(holder.avatarIv);
        }catch(Exception e){
            holder.avatarIv.setImageResource(R.drawable.icon_default2);
        }
    }

    @Override
    public int getItemCount() {
        return notificacionList.size();
    }


    class HolderNotificacion extends RecyclerView.ViewHolder{

        ImageView avatarIv;
        TextView nameTv, notificacionTv, timeTv;

        public HolderNotificacion(@NonNull View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatarIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            notificacionTv = itemView.findViewById(R.id.notificacionTv);
            timeTv = itemView.findViewById(R.id.timeTv);
        }
    }
}
