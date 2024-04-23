package com.example.flappybird;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class StartingActivity extends AppCompatActivity {
    int LOCATION_PERMISSION_REQUEST_CODE = 10000;

    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 0x00;

    private int volumeThreshold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }

        // lay toa do dien thoai luu A(x,y) B(x,Y)

        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Get the volume threshold
        SharedPreferences settings = getPreferences(0);
        volumeThreshold = settings.getInt("VolumeThreshold", 50);
    }


    private void showToast(Object mess) {
        Toast.makeText(getApplicationContext(), "" + mess, Toast.LENGTH_LONG).show();
    }

    public void goToTouchActivity(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("Mode", "Touch");
        startActivity(intent);
    }

    public void playNearFriend(View v) {
        Intent intent = new Intent(this, GameFriendActivity.class);
        intent.putExtra("Mode", "Touch");
        startActivity(intent);
    }

    public void goToVoiceActivity(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_PERMISSION_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("Mode", "Voice");
            intent.putExtra("VolumeThreshold", volumeThreshold);
            startActivity(intent);
        }
    }

    public void goToRankAward(View view) {
        Intent intent = new Intent(this, RankActivity.class);
        startActivity(intent);
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted
                    Intent intent = new Intent(this, GameActivity.class);
                    intent.putExtra("Mode", "Voice");
                    startActivity(intent);
                } else {
                    // Permission denied
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setTitle("Permission Denied...");
                    alertDialog.setMessage("Without record audio permission granted, " +
                            "you cannot play with voice control.");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                        }
                    });
                    alertDialog.show();
                }
                return;
            }
        }
    }

    public void adjustVolumeThreshold(View view) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Scroll to adjust the threshold of you voice.");
        alertDialog.setIcon(R.drawable.ic_bird);
        View alertDialogView = LayoutInflater.from(this)
                .inflate(R.layout.alert_dialog_adjust_volume_threshold, null);
        NumberPicker numberPicker = (NumberPicker) alertDialogView.findViewById(R.id.number_picker);
        numberPicker.setMaxValue(300);
        numberPicker.setMinValue(0);
        numberPicker.setValue(volumeThreshold);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker np, int oldValue, int newValue) {
                volumeThreshold = np.getValue();
            }
        });
        alertDialog.setView(alertDialogView);
        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                // Save the change in the SharedPreferences
                SharedPreferences settings = StartingActivity.this.getPreferences(0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt("VolumeThreshold", StartingActivity.this.volumeThreshold);
                editor.apply();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

}
