package com.shabydevelop.androiddemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Insets;
import android.view.WindowInsets;
import android.graphics.Color;
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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets safe = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(safe.left, safe.top, safe.right, safe.bottom);
                return insets;
            });
        } else {
            root.setFitsSystemWindows(true);
        }

        TextView header = new TextView(this);
        header.setText("מעקב אכילה אישי");
        header.setTextSize(22);
        header.setTextColor(Color.WHITE);
        header.setBackgroundColor(Color.rgb(30, 64, 100));
        header.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        header.setPadding(dp(20), dp(16), dp(20), dp(16));
        if (Build.VERSION.SDK_INT >= 28) header.setAccessibilityHeading(true);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(24), dp(24), dp(24), dp(24));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);


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

        root.addView(layout, new LinearLayout.LayoutParams(-1, 0, 1f));
        setContentView(root);
        root.requestApplyInsets();
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("count", count);
        super.onSaveInstanceState(outState);
    }

    private int dp(int value) {
        return (int) (getResources().getDisplayMetrics().density * value + 0.5f);
    }
}
