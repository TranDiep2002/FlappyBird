package com.example.flappybird.base;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    private SharedPreferences sharedPreferences;
    public SharedPreference(Context context) {

        sharedPreferences = context.getSharedPreferences("SharePrfe", Context.MODE_PRIVATE);
    }

    public interface EditorBlock {
        void invoke(SharedPreferences.Editor editor);
    }
    public void edit(EditorBlock block) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        block.invoke(editor);
        editor.apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public void putBoolean(String key, boolean value) {
        edit(editor -> editor.putBoolean(key, value));
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        edit(editor -> editor.putString(key, value));
    }

    public void clear() {
        edit(SharedPreferences.Editor::clear);
    }
}

