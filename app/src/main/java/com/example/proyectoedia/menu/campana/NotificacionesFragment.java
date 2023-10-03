package com.example.proyectoedia.menu.campana;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectoedia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificacionesFragment extends Fragment {

    RecyclerView notificacionRv;

    private FirebaseAuth firebaseAuth;
    private ArrayList<ModeloCampana> notificacionesList;
    private AdaptadorCampana adaptadorNotificacion;

    public NotificacionesFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notificaciones, container, false);

        notificacionRv = view.findViewById(R.id.notificacionRv);
        firebaseAuth = FirebaseAuth.getInstance();

        obtenerTodasNotificaciones();

        return view;
    }

    private void obtenerTodasNotificaciones() {
        notificacionesList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Notificaciones")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        notificacionesList.clear();
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            ModeloCampana model = ds.getValue(ModeloCampana.class);

                            notificacionesList.add(model);
                        }

                        adaptadorNotificacion = new AdaptadorCampana(getActivity(), notificacionesList);

                        notificacionRv.setAdapter(adaptadorNotificacion);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}