package com.example.flappybird;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.flappybird.base.Constant;
import com.example.flappybird.base.GameViewModel;
import com.example.flappybird.databinding.ActivityTestBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;

public class TestActivity extends AppCompatActivity {

    private ActivityTestBinding binding;
    public GameViewModel viewModel;
    Button bt;
    int i = 0;
    ScoreAdapter scoreAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
        scoreAdapter = new ScoreAdapter();

        binding.button.setOnClickListener(v -> {
            getRankUser(users -> {
                scoreAdapter.setData(users);
            });
        });

        binding.rcvFiendNear.setAdapter(scoreAdapter);


    }


    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    private void getRankUser(Consumer<ArrayList<User>> cb) {
        ArrayList users = new ArrayList<>();
        firestore.collection(Constant.UserPlay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                        users.add(dc.toObject(User.class));
                    }
                    ;
                    Collections.sort(users, Comparator.comparingInt((User o) -> o.MaxScore));
                    cb.accept(users);

                });

    }
}