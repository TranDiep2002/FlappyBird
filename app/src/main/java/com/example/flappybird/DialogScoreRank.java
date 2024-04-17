package com.example.flappybird;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.flappybird.base.BaseDialog;
import com.example.flappybird.base.SharedPreference;
import com.example.flappybird.databinding.DialogScoreRankBinding;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class DialogScoreRank extends BaseDialog<DialogScoreRankBinding> {

    private ScoreAdapter adapter;
    int i = 0;

    public DialogScoreRank(Context context, List<User> users, SharedPreference preference, OnEventView eventView) {
        super(context);
        DialogScoreRankBinding binding = DialogScoreRankBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);



        adapter = new ScoreAdapter();
        Glide.with(binding.getRoot()).load(R.raw.animate).into(binding.animationView);
        new Handler(Looper.myLooper()).postDelayed(() -> {
            binding.animationView.setVisibility(View.GONE);
            binding.rcvFiendNear.setVisibility(View.VISIBLE);
        }, 2500);

        for (User us : users) {
            if (Objects.equals(us.Id, preference.getString("UsID", ""))) {
                us.MaxScore = us.Score;
            }
        }

        Collections.sort(users, (o1, o2) -> Integer.compare(o2.MaxScore,o1.MaxScore));

        for (i = 0; i < users.size(); i++) {
            if (users.get(i).Id .equals(preference.getString("UsID", ""))) {
                binding.txtContent.setText("Your rank:" + (i+1));
                binding.txtScore.setText("Total score: " + users.get(i).Score);
                break;
            }
        }
        adapter.setData(users);
        binding.rcvFiendNear.setAdapter(adapter);
        binding.bntCancel.setOnClickListener(v -> {
            eventView.OnCancel();
            dismiss();
        });
        binding.btnSave.setOnClickListener(v -> {
            eventView.OnRestart();
            dismiss();
        });

    }

    public interface OnEventView {
        void OnCancel();

        void OnRestart();
    }
}
