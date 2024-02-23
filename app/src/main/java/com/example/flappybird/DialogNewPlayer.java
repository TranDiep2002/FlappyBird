package com.example.flappybird;

import android.content.Context;

import com.example.flappybird.base.BaseDialog;
import com.example.flappybird.databinding.DialogEndGameBinding;
import com.example.flappybird.databinding.DialogInitUserBinding;

public class DialogNewPlayer extends BaseDialog<DialogInitUserBinding> {

    public DialogNewPlayer(Context context, OnEventView eventView) {
        super(context);
        DialogInitUserBinding binding = DialogInitUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        binding.bntCancel.setOnClickListener(v -> {
            eventView.onCancel();
            dismiss();
        });
        binding.btnSave.setOnClickListener(v -> {
            eventView.onSave(binding.edtName.getText().toString());
            dismiss();
        });

    }

    public interface OnEventView {
        void onCancel();

        void onSave(String s);
    }
}
