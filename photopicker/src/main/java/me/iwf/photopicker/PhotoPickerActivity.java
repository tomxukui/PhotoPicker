package me.iwf.photopicker;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.adapter.PhotoGridAdapter;
import me.iwf.photopicker.adapter.PopupDirectoryListAdapter;
import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.entity.PhotoDirectory;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.event.OnPhotoClickListener;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;
import me.iwf.photopicker.utils.ImageCaptureManager;
import me.iwf.photopicker.utils.MediaStoreHelper;

public class PhotoPickerActivity extends AppCompatActivity {

    private static final int COUNT_MAX = 4;//目录弹出框的一次最多显示的目录数目
    private static final int SCROLL_THRESHOLD = 30;

    private TextView tv_title;
    private RecyclerView recyclerView;
    private TextView tv_dir;

    private ImageCaptureManager mCaptureManager;
    private PhotoGridAdapter mPhotoGridAdapter;
    private ListPopupWindow mListPopupWindow;
    private RequestManager mGlideRequestManager;

    private PopupDirectoryListAdapter mDirPopupListAdapter;//所有photos的路径
    private List<PhotoDirectory> mDirectories;//传入的已选照片
    private ArrayList<String> mOriginalPhotos;

    private boolean mShowCamera;
    private boolean mShowGif;
    private boolean mPreviewEnabled;
    private int mMaxCount;
    private int mColumnNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picker_activity_photo_picker);
        initData();
        initActionBar();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picker_menu_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.action_done) {
            ArrayList<String> selectedPhotos = mPhotoGridAdapter.getSelectedPhotoPaths();
            if (selectedPhotos != null && selectedPhotos.size() > 0) {
                Intent intent = new Intent();
                intent.putStringArrayListExtra(PhotoPicker.EXTRA_SELECTED_PHOTOS, selectedPhotos);
                setResult(RESULT_OK, intent);
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case ImageCaptureManager.REQUEST_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    mCaptureManager.galleryAddPic();
                    if (mDirectories.size() > 0) {
                        String path = mCaptureManager.getCurrentPhotoPath();
                        PhotoDirectory directory = mDirectories.get(MediaStoreHelper.INDEX_ALL_PHOTOS);
                        directory.getPhotos().add(MediaStoreHelper.INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
                        directory.setCoverPath(path);
                        mPhotoGridAdapter.notifyDataSetChanged();
                    }
                }
            }
            break;

            default:
                break;

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mCaptureManager.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCaptureManager.onRestoreInstanceState(savedInstanceState);
    }

    private void initData() {
        mShowCamera = getIntent().getBooleanExtra(PhotoPicker.EXTRA_SHOW_CAMERA, true);
        mShowGif = getIntent().getBooleanExtra(PhotoPicker.EXTRA_SHOW_GIF, false);
        mPreviewEnabled = getIntent().getBooleanExtra(PhotoPicker.EXTRA_PREVIEW_ENABLED, true);
        mMaxCount = getIntent().getIntExtra(PhotoPicker.EXTRA_MAX_COUNT, PhotoPicker.DEFAULT_MAX_COUNT);
        mColumnNumber = getIntent().getIntExtra(PhotoPicker.EXTRA_GRID_COLUMN, PhotoPicker.DEFAULT_COLUMN_NUMBER);
        mOriginalPhotos = getIntent().getStringArrayListExtra(PhotoPicker.EXTRA_ORIGINAL_PHOTOS);

        mGlideRequestManager = Glide.with(this);
        mDirectories = new ArrayList<>();
        if (mOriginalPhotos == null) {
            mOriginalPhotos = new ArrayList<>();
        }

        mPhotoGridAdapter = new PhotoGridAdapter(this, mGlideRequestManager, mDirectories, mOriginalPhotos, mColumnNumber);
        mPhotoGridAdapter.setShowCamera(mShowCamera);
        mPhotoGridAdapter.setPreviewEnable(mPreviewEnabled);
        mPhotoGridAdapter.setOnItemCheckListener(new OnItemCheckListener() {

            @Override
            public boolean onItemCheck(int position, Photo photo, int selectedItemCount) {
                if (mMaxCount <= 1) {
                    List<String> photos = mPhotoGridAdapter.getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        mPhotoGridAdapter.notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > mMaxCount) {
                    Toast.makeText(PhotoPickerActivity.this, String.format("最多可以选择%d张", mMaxCount), Toast.LENGTH_LONG).show();
                    return true;
                }

                setTitleView(selectedItemCount);
                return true;
            }

        });
        mPhotoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {

            @Override
            public void onClick(View v, int position, boolean showCamera) {
                int index = showCamera ? position - 1 : position;
                List<String> photoPaths = mPhotoGridAdapter.getCurrentPhotoPaths();
                String photoPath = photoPaths.get(index);

                Intent intent = new Intent(PhotoPickerActivity.this, PhotoZoomActivity.class);
                intent.putExtra(PhotoZoomActivity.EXTRA_SELECTED_PHOTO, photoPath);
                startActivity(intent);
            }

        });
        mPhotoGridAdapter.setOnCameraClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openCamera();
            }

        });

        mDirPopupListAdapter = new PopupDirectoryListAdapter(mGlideRequestManager, mDirectories);

        mCaptureManager = new ImageCaptureManager(this);

        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(PhotoPicker.EXTRA_SHOW_GIF, mShowGif);
        MediaStoreHelper.getPhotoDirs(this, mediaStoreArgs, new MediaStoreHelper.PhotosResultCallback() {

            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                mDirectories.clear();
                mDirectories.addAll(dirs);
                mPhotoGridAdapter.notifyDataSetChanged();
                mDirPopupListAdapter.notifyDataSetChanged();
                adjustHeight();
            }

        });
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_title = findViewById(R.id.tv_title);
        setTitleView(mOriginalPhotos.size());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示默认标题
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示返回键
        }
    }

    private void initView() {
        tv_dir = findViewById(R.id.tv_dir);
        tv_dir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mListPopupWindow.isShowing()) {
                    mListPopupWindow.dismiss();

                } else if (!isFinishing()) {
                    adjustHeight();
                    mListPopupWindow.show();
                }
            }

        });

        recyclerView = findViewById(R.id.recyclerView);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(mColumnNumber, OrientationHelper.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mPhotoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (Math.abs(dy) > SCROLL_THRESHOLD) {
                    mGlideRequestManager.pauseRequests();
                } else {
                    resumeRequestsIfNotDestroyed();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    resumeRequestsIfNotDestroyed();
                }
            }

        });

        mListPopupWindow = new ListPopupWindow(this);
        mListPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        mListPopupWindow.setAnchorView(tv_dir);
        mListPopupWindow.setAdapter(mDirPopupListAdapter);
        mListPopupWindow.setModal(true);
        mListPopupWindow.setDropDownGravity(Gravity.BOTTOM);
        mListPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListPopupWindow.dismiss();
                PhotoDirectory directory = mDirectories.get(position);
                tv_dir.setText(directory.getName());
                mPhotoGridAdapter.setCurrentDirectoryIndex(position);
                mPhotoGridAdapter.notifyDataSetChanged();
            }

        });
    }

    private void resumeRequestsIfNotDestroyed() {
        if (AndroidLifecycleUtils.canLoadImage(this)) {
            mGlideRequestManager.resumeRequests();
        }
    }

    private void setTitleView(int count) {
        if (mMaxCount > 1) {
            tv_title.setText(String.format("选择图片(%d/%d)", count, mMaxCount));

        } else {
            tv_title.setText("选择图片");
        }
    }

    private void openCamera() {
        try {
            Intent intent = mCaptureManager.dispatchTakePictureIntent();
            startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void adjustHeight() {
        if (mDirPopupListAdapter == null) {
            return;
        }

        int count = mDirPopupListAdapter.getCount();
        count = count < COUNT_MAX ? count : COUNT_MAX;

        if (mListPopupWindow != null) {
            mListPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.picker_item_directory_height));
        }
    }

}
