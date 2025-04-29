package com.example.busmanagementapplication;

import android.view.View;
import android.widget.AdapterView;

public class SimpleItemSelectedListener implements AdapterView.OnItemSelectedListener {
    private final OnItemSelected callback;

    public interface OnItemSelected {
        void onItemSelected(int position);
    }

    public SimpleItemSelectedListener(OnItemSelected callback) {
        this.callback = callback;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        callback.onItemSelected(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
