package com.example.flappybird;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.flappybird.base.Constant;
import com.example.flappybird.databinding.ActivityRankBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class RankActivity extends AppCompatActivity implements  SensorEventListener{

    private ActivityRankBinding binding;
    ScoreAdapter scoreAdapter;
    private SensorManager sensorManager;

    private Sensor proximitySensor;
    private boolean isPhoneNear = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRankBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        scoreAdapter = new ScoreAdapter();

        binding.progressBar.setVisibility(View.VISIBLE);

        getRankUser(users -> {
            binding.progressBar.setVisibility(View.GONE);
            scoreAdapter.setData(users);
            binding.rcvFiendNear.setAdapter(scoreAdapter);
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ArrayList<User> users;
    private void getRankUser(Consumer<ArrayList<User>> callback) {
        users = new ArrayList<>();
        firestore.collection(Constant.UserPlay).addSnapshotListener((value, error) -> {
            assert value != null;
            users.clear();
            for (DocumentSnapshot documentSnapshot : value.getDocuments()) {
                User us = documentSnapshot.toObject(User.class);
                users.add(us);
            }
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User o1, User o2) {
                    return Integer.compare(o2.MaxScore,o1.MaxScore);
                }
            });
            callback.accept(users);
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Handle sensor events here
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float distance = event.values[2];
            if (distance > 1) {
                showToast( "Đã gắp điện thoại lên!" + distance);
            }
        }
    }
    private void showToast(Object mess) {
        Toast.makeText(getApplicationContext(), "" + mess, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register sensor listener
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listener to save battery
        sensorManager.unregisterListener(this);
    }

}