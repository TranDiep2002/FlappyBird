package com.example.flappybird;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flappybird.base.Constant;
import com.example.flappybird.base.GameViewModel;
import com.example.flappybird.base.SharedPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class GameFriendActivity extends AppCompatActivity {

    private GameViewModel viewModel;
    private GameView gameView;
    private TextView textViewScore;

    private boolean isGameOver;

    private boolean isSetNewTimerThreadEnabled;

    private int volumeThreshold;

    private Thread setNewTimerThread;

    private MediaPlayer mediaPlayer;

    private int gameMode;

    private AudioRecorder audioRecorder;

    private static final int TOUCH_MODE = 0x00;
    private static final int VOICE_MODE = 0x01;

    private double latLocation = 0L;
    private double longLocation = 0L;
    private Timer timer;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE: {
                    if (gameView.isAlive()) {
                        isGameOver = false;
                        gameView.update();
                    } else {
                        if (isGameOver) {
                            break;
                        } else {
                            isGameOver = true;
                        }

                        if (gameMode == TOUCH_MODE) {
                            // Cancel the timer
                            timer.cancel();
                            timer.purge();
                        } else {
                            audioRecorder.isGetVoiceRun = false;
                            audioRecorder = null;
                            System.gc();
                        }
                        new DialogScoreRank(GameFriendActivity.this,users,sharedPreference, new DialogScoreRank.OnEventView() {
                            @Override
                            public void OnCancel() {
                                GameFriendActivity.this.onBackPressed();
                            }

                            @Override
                            public void OnRestart() {

                                GameFriendActivity.this.restartGame();
                            }
                        }).show();
                        Map<String, Object> data = new HashMap<>();
                        int score = gameView.getScore();
                        if (gameView.getScore() > viewModel.userLogin.getValue().MaxScore) {
                            data.put("Score", score);
                            data.put("MaxScore", score);
                        } else {
                            data.put("Score", score);
                        }
                        firestore.collection(Constant.UserPlay).document(sharedPreference.getString("UsID", "")).update(data);
                    }

                    break;
                }

                case RESET_SCORE: {
                    textViewScore.setText("0");

                    break;
                }

                default: {
                    break;
                }
            }
        }
    };

    private static final int UPDATE = 0x00;
    private static final int RESET_SCORE = 0x01;

    SharedPreference sharedPreference;
    ScoreAdapter scoreAdapter;
    RecyclerView recyclerView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game_near_friend);
        sharedPreference = new SharedPreference(getApplicationContext());
        viewModel = new ViewModelProvider(this).get(GameViewModel.class);
// lay log va lat dia chi

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                longLocation = location.getLongitude();
                latLocation = location.getLatitude();
            } else {
                showToast("Last location is null");
            }
        }).addOnFailureListener(e -> {
            showToast(e.getMessage());
            Log.d("Error", "ERRVAL: " + e.getMessage());
        });


        getRankUser();
        initViews();

        isSetNewTimerThreadEnabled = true;
        setNewTimerThread = new Thread(() -> {
            try {
                // Sleep for 3 seconds for the Surface to initialize
                Thread.sleep(3000);
            } catch (Exception exception) {
                exception.printStackTrace();
            } finally {
                if (isSetNewTimerThreadEnabled) {
                    setNewTimer();
                }
            }
        });
        scoreAdapter = new ScoreAdapter();

        getUsers(user -> {
            if (user != null) {
                viewModel.userLogin.postValue(user);
                startGame();
            } else {
                initNewUsers();
            }
        });

        viewModel.usersArrayList.observe(this, data -> {
            User user = viewModel.userLogin.getValue();
            if (user != null) {
                List<User> listData = new User().getNearestUsers(user.LatLocation, user.LongLocation, data);
                scoreAdapter.setData(listData);
            }

        });
        recyclerView.setAdapter(scoreAdapter);

        mediaPlayer = MediaPlayer.create(this, R.raw.sound_score);
        mediaPlayer.setLooping(false);
        if (getIntent().getStringExtra("Mode").equals("Touch")) {
            gameMode = TOUCH_MODE;
        } else {
            gameMode = VOICE_MODE;
            volumeThreshold = getIntent().getIntExtra("VolumeThreshold", 50);
        }


        if (gameMode == TOUCH_MODE) {

            gameView.setOnTouchListener((view, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        gameView.jump();

                        break;

                    case MotionEvent.ACTION_UP:


                        break;

                    default:
                        break;
                }

                return true;
            });
        } else {
            audioRecorder = new AudioRecorder();
            audioRecorder.getNoiseLevel();
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private void startGame() {
        setNewTimerThread.start();

    }


    private void getUsers(Consumer<User> us) {
        String uid=sharedPreference.getString("UsID", "");
        if(uid.equals("")){
            initNewUsers();
        }
        else {
            firestore.collection(Constant.UserPlay).document(uid).get().addOnFailureListener(e -> {
                us.accept(null);
            }).addOnSuccessListener(documentSnapshot -> {
                us.accept(documentSnapshot.toObject(User.class));
            });
        }

    }


    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    int count = 0;

    private void initNewUsers() {
        new DialogNewPlayer(this, new DialogNewPlayer.OnEventView() {
            @Override
            public void onCancel() {
                onBackPressed();
            }

            @Override
            public void onSave(String s) {
                String id = firestore.collection(Constant.UserPlay).document().getId();
                User us = new User(id, longLocation, latLocation, 0, 0, s);
                firestore.collection(Constant.UserPlay).document(id).set(us).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        viewModel.userLogin.postValue(us);
                        sharedPreference.putBoolean(Constant.FirstPlay, false);
                        sharedPreference.putString("UsID", id);
                        startGame();
                    }
                }).addOnFailureListener(e -> showToast(e.getMessage()));

            }
        }).show();


    }
    private void showToast(Object mess) {
        Toast.makeText(getApplicationContext(), "" + mess, Toast.LENGTH_LONG).show();
    }

    int LOCATION_PERMISSION_REQUEST_CODE = 10000;

    ArrayList<User> users;

    private void getRankUser() {
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
                    return Integer.compare(o1.MaxScore,o2.MaxScore);
                }
            });
            scoreAdapter.setData(users);
            viewModel.usersArrayList.postValue(users);
        });
    }

    private class AudioRecorder {

        private static final String TAG = "AudioRecord";

        int SAMPLE_RATE_IN_HZ = 8000;

        int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);

        AudioRecord mAudioRecord;

        boolean isGetVoiceRun;


        Object mLock;

        public AudioRecorder() {
            mLock = new Object();
        }

        public void getNoiseLevel() {
            if (isGetVoiceRun) {
                return;
            }
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
            if (mAudioRecord == null) {
                Log.e(TAG, "mAudioRecord initialization failed.");
            }
            isGetVoiceRun = true;

            new Thread(() -> {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    Log.i(TAG, "分贝值:" + volume);

                    // Jump if the volume is loud enough
                    if (volume > volumeThreshold) {
                        GameFriendActivity.this.gameView.jump();
                        Log.i(TAG, "分贝值: " + volume + "超过了");
                    }

                    synchronized (mLock) {
                        try {
                            mLock.wait(17);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }).start();
        }
    }

    private void initViews() {
        gameView = findViewById(R.id.game_view);
        textViewScore = findViewById(R.id.text_view_score_fiend);
        recyclerView = findViewById(R.id.rcvFiendNear);
    }

    private void setNewTimer() {
        if (!isSetNewTimerThreadEnabled) {
            return;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                // Send the message to the handler to update the UI of the GameView
                GameFriendActivity.this.handler.sendEmptyMessage(UPDATE);

                // For garbage collection
                System.gc();
            }

        }, 0, 17);
    }

    @Override
    protected void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (audioRecorder != null) {
            audioRecorder.isGetVoiceRun = false;
            audioRecorder = null;
        }

        isSetNewTimerThreadEnabled = false;

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        isSetNewTimerThreadEnabled = false;

        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * Updates the displayed score.
     *
     * @param score The new score.
     */
    public void updateScore(int score) {
        textViewScore.setText(String.valueOf(score));
        for (User data : users) {
            if (data.Id.equals(sharedPreference.getString("UsID", ""))) {
                data.Score = score;
                break;
            }
        }
        scoreAdapter.setData(users);
    }

    /**
     * Plays the music for score.
     */
    public void playScoreMusic() {
        if (gameMode == TOUCH_MODE) {
            mediaPlayer.start();
        }
    }

    /**
     * Restarts the game.
     */
    private void restartGame() {
        // Reset all the data of the over game in the GameView
        gameView.resetData();

        // Refresh the TextView for displaying the score
        new Thread(() -> handler.sendEmptyMessage(RESET_SCORE)).start();

        if (gameMode == TOUCH_MODE) {
            isSetNewTimerThreadEnabled = true;
            setNewTimerThread = new Thread(() -> {
                try {
                    // Sleep for 3 seconds
                    Thread.sleep(3000);
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    if (isSetNewTimerThreadEnabled) {
                        setNewTimer();
                    }
                }
            });
            setNewTimerThread.start();
        } else {
            audioRecorder = new AudioRecorder();
            audioRecorder.getNoiseLevel();
        }
    }

    @Override
    public void onBackPressed() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        isSetNewTimerThreadEnabled = false;

        super.onBackPressed();
    }

}
