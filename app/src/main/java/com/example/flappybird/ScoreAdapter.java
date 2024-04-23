package com.example.flappybird;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flappybird.databinding.ScoreItemViewBinding;

import java.util.ArrayList;
import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {

  private   List<User> users= new ArrayList<>();


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
            binding.txtScore.setText(""+users.MaxScore);
            // Đặt tỷ lệ thu nhỏ cho hình ảnh
            // phương thức setScaleX() và setScaleY() được sử dụng để thay đổi tỷ lệ của một View theo hướng chiều ngang (X) và chiều dọc (Y) tương ứng
            float scaleFactor = calculateScaleFactor(users.MaxScore);
            binding.imageView.setScaleX(scaleFactor);
            binding.imageView.setScaleY(scaleFactor);
        }
    }
    // Tính toán tỷ lệ thu nhỏ dựa trên điểm số của người dùng
    private float calculateScaleFactor(int maxScore) {
        // Tính toán tỷ lệ dựa trên điểm số
        // Điểm số càng cao, tỷ lệ càng cao, làm cho hình ảnh càng to
        // Điểm số càng thấp, tỷ lệ càng thấp, làm cho hình ảnh càng nhỏ
        float scaleFactor = 1.0f + ((float) maxScore / 40); // Điều chỉnh số 100 tùy thuộc vào phạm vi điểm số của bạn
        if (scaleFactor == 1.0f) {// nếu điểm số là 0 thì sẽ cho tỉ lệ thấp hẳn : 0.5f để nhìn thấy rõ sự khác biệt
            scaleFactor = 0.5f;
        }
        return scaleFactor;
    }

    public void setData(List<User> users){
        this.users=users;
        notifyDataSetChanged();
    }
}
