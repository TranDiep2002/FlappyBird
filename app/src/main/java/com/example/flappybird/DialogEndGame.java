package com.example.flappybird;

import android.content.Context;

import com.example.flappybird.base.BaseDialog;
import com.example.flappybird.databinding.DialogEndGameBinding;

public class DialogEndGame extends BaseDialog<DialogEndGameBinding> {

    public DialogEndGame(Context context,String message,OnEventView eventView) {
        super(context);
        DialogEndGameBinding binding = DialogEndGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        binding.txtContent.setText(message);

        binding.bntCancel.setOnClickListener(v->eventView.OnCancel());
        binding.btnSave.setOnClickListener(v->eventView.OnRestart());

    }

    public interface OnEventView{
        void OnCancel();
        void OnRestart();
    }
}
