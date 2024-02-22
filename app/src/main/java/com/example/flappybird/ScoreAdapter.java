package com.example.flappybird;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flappybird.databinding.ScoreItemViewBinding;

import java.util.ArrayList;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

    final ArrayList<User> users;

    public ScoreAdapter(ArrayList<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScoreViewHolder(ScoreItemViewBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        holder.onBind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ScoreViewHolder extends RecyclerView.ViewHolder{

        private ScoreItemViewBinding binding;
        public ScoreViewHolder(ScoreItemViewBinding viewBinding) {
            super(viewBinding.getRoot());
            binding= viewBinding;
        }
        public void onBind(User users){
            binding.txtName.setText(users.UserName);
            binding.txtScore.setText(""+users.Score);
        }
    }
}
