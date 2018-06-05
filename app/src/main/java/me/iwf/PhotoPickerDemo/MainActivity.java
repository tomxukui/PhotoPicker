package me.iwf.PhotoPickerDemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import com.yanzhenjie.permission.Rationale;

import java.util.ArrayList;
import java.util.List;

import me.iwf.PhotoPickerDemo.permission.DefaultRationale;
import me.iwf.PhotoPickerDemo.permission.PermissionSetting;
import me.iwf.photopicker.PhotoPicker;
import me.iwf.photopicker.PhotoPreview;
import me.iwf.photopicker.event.OnPhotoClickListener;

public class MainActivity extends AppCompatActivity {

    private static final int EXTRA_PICK = 1;
    private static final int EXTRA_PREVIEW = 2;
    private static final int MAX_COUNT = 9;
    private static final int SPAN_COUNT = 4;

    private PhotoAdapter mPhotoAdapter;

    private ArrayList<String> selectedPhotos = new ArrayList<>();
    private Rationale mRationale;
    private PermissionSetting mPermissionSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        requestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case EXTRA_PICK: {
                if (resultCode == RESULT_OK) {
                    List<String> photos = data.getStringArrayListExtra(PhotoPicker.EXTRA_SELECTED_PHOTOS);
                    selectedPhotos.clear();
                    if (photos != null) {
                        selectedPhotos.addAll(photos);
                    }
                    mPhotoAdapter.notifyDataSetChanged();
                }
            }
            break;

            case EXTRA_PREVIEW: {
                if (resultCode == RESULT_OK) {
                    List<String> photos = data.getStringArrayListExtra(PhotoPreview.EXTRA_PHOTO_PATHS);
                    selectedPhotos.clear();
                    if (photos != null) {
                        selectedPhotos.addAll(photos);
                    }
                    mPhotoAdapter.notifyDataSetChanged();
                }
            }
            break;

            default:
                break;

        }
    }

    private void initData() {
        mRationale = new DefaultRationale();
        mPermissionSetting = new PermissionSetting(this);

        mPhotoAdapter = new PhotoAdapter(this, selectedPhotos, MAX_COUNT);
        mPhotoAdapter.setOnAddClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(MAX_COUNT)
                        .setGridColumnCount(SPAN_COUNT)
                        .setShowCamera(true)
                        .setPreviewEnabled(true)
                        .setSelected(selectedPhotos)
                        .start(MainActivity.this, EXTRA_PICK);
            }

        });
        mPhotoAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {

            @Override
            public void onClick(View v, int position, boolean showCamera) {
                PhotoPreview.builder()
                        .setPhotos(selectedPhotos)
                        .setCurrentItem(position)
                        .start(MainActivity.this, EXTRA_PREVIEW);
            }

        });
    }

    private void initView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        recyclerView.setAdapter(mPhotoAdapter);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(MAX_COUNT)
                        .setGridColumnCount(SPAN_COUNT)
                        .setPreviewEnabled(false)
                        .start(MainActivity.this, EXTRA_PICK);
            }

        });

        findViewById(R.id.button_no_camera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(MAX_COUNT)
                        .setShowCamera(false)
                        .setPreviewEnabled(false)
                        .start(MainActivity.this, EXTRA_PICK);
            }

        });

        findViewById(R.id.button_one_photo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setPhotoCount(1)
                        .start(MainActivity.this, EXTRA_PICK);
            }

        });

        findViewById(R.id.button_photo_gif).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PhotoPicker.builder()
                        .setShowCamera(true)
                        .setShowGif(true)
                        .start(MainActivity.this, EXTRA_PICK);
            }

        });
    }

    private void requestPermission() {
        AndPermission.with(this)
                .permission(Permission.Group.STORAGE, Permission.Group.CAMERA)
                .rationale(mRationale)
                .onDenied(new Action() {

                    @Override
                    public void onAction(List<String> permissions) {
                        Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();

                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            mPermissionSetting.showSetting(permissions);
                        }
                    }

                })
                .start();
    }

}
