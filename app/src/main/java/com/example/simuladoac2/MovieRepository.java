package com.example.simuladoac2;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.Collections;
import java.util.List;

public class MovieRepository {
    private final FirebaseFirestore database = FirebaseFirestore.getInstance();

    public ListenerRegistration observeMovies(
            EventListener<List<DocumentSnapshot>> listener
    ) {
        return database.collection("movies")
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        listener.onEvent(null, error);
                        return;
                    }
                    listener.onEvent(
                            snapshot == null ? Collections.emptyList() : snapshot.getDocuments(),
                            null
                    );
                });
    }

    public Task<DocumentReference> add(Movie movie) {
        return database.collection("movies").add(movie);
    }

    public Task<Void> update(String id, Movie movie) {
        return database.collection("movies").document(id).set(movie);
    }

    public Task<Void> delete(String id) {
        return database.collection("movies").document(id).delete();
    }
}
