package com.shabydevelop.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private int count = 0;
    private TextView counter;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) count = savedInstanceState.getInt("count", 0);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("אפליקציית הדגמה");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Android Demo");
        subtitle.setTextSize(16);
        subtitle.setGravity(Gravity.CENTER);
        layout.addView(subtitle);

        counter = new TextView(this);
        counter.setTextSize(40);
        counter.setGravity(Gravity.CENTER);
        counter.setPadding(0, dp(28), 0, dp(28));
        counter.setText(Integer.toString(count));
        layout.addView(counter);

        Button increment = new Button(this);
        increment.setText("לחץ כאן");
        increment.setOnClickListener(v -> { count++; counter.setText(Integer.toString(count)); });
        layout.addView(increment);

        Button reset = new Button(this);
        reset.setText("איפוס");
        reset.setOnClickListener(v -> { count = 0; counter.setText("0"); });
        layout.addView(reset);

        setContentView(layout);
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("count", count);
        super.onSaveInstanceState(outState);
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
