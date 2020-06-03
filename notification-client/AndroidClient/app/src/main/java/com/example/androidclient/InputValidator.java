package com.example.androidclient;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public abstract class InputValidator implements TextWatcher {

    private TextView tv;

    public InputValidator(TextView tv) {
        this.tv = tv;
    }

    public abstract void validate(String text, TextView tv);

    @Override
    public void afterTextChanged(Editable s) {
        String text = tv.getText().toString();
        validate(text, tv);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
}
