package kr.keti.apk_study_keti;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SUB_PROJ extends AppCompatActivity {
    private TextView tv_sub;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.SUB_PROJ);

        tv_sub = findViewById(R.id.tv_sub);

        Intent intent = getIntent();
        String str = intent.getStringExtra("str");

        tv_sub.setText(str);

    }
}