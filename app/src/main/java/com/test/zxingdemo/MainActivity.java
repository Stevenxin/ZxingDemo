package com.test.zxingdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.test.zxingdemo.zxing.CaptureActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mResultString;
    private ImageView mResultPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        findViewById(R.id.btn_scan_QRCode).setOnClickListener(this);
        mResultString = (TextView) findViewById(R.id.tv_result_string);
        mResultPhoto = (ImageView) findViewById(R.id.iv_result_photo);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan_QRCode:
                Intent intent = new Intent(this, CaptureActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            Bundle bundle = data.getExtras();
            String resultString = bundle.getString("result");
            if (!TextUtils.isEmpty(resultString)) {
                mResultString.setText(resultString);
            }
            Parcelable bitmap = data.getParcelableExtra("bitmap");
            if (bitmap != null) {
                mResultPhoto.setImageBitmap((Bitmap) bitmap);
            }
        }
    }
}
