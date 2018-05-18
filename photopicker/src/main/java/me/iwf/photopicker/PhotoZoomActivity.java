package me.iwf.photopicker;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import me.iwf.photopicker.widget.TouchImageView;

/**
 * Created by xukui on 2018/5/18.
 */
public class PhotoZoomActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_PHOTO = "EXTRA_SELECTED_PHOTO";

    private TouchImageView iv_img;

    private String mImgPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_zoom);
        initData();
        initView();
    }

    private void initData() {
        mImgPath = getIntent().getStringExtra(EXTRA_SELECTED_PHOTO);
    }

    private void initView() {
        iv_img = findViewById(R.id.iv_img);

        Uri uri;
        if (mImgPath.startsWith("http")) {
            uri = Uri.parse(mImgPath);

        } else {
            uri = Uri.fromFile(new File(mImgPath));
        }

        final RequestOptions options = new RequestOptions();
        options.dontAnimate()
                .dontTransform()
                .override(800, 800)
                .placeholder(R.drawable.__picker_ic_photo_black_48dp)
                .error(R.drawable.__picker_ic_broken_image_black_48dp);
        Glide.with(this)
                .setDefaultRequestOptions(options)
                .load(uri)
                .thumbnail(0.1f)
                .into(iv_img);
    }

}
