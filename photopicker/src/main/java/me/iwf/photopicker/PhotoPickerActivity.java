package me.iwf.photopicker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.fragment.ImagePagerFragment;
import me.iwf.photopicker.fragment.PhotoPickerFragment;

import static android.widget.Toast.LENGTH_LONG;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_COLUMN_NUMBER;
import static me.iwf.photopicker.PhotoPicker.DEFAULT_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_GRID_COLUMN;
import static me.iwf.photopicker.PhotoPicker.EXTRA_MAX_COUNT;
import static me.iwf.photopicker.PhotoPicker.EXTRA_ORIGINAL_PHOTOS;
import static me.iwf.photopicker.PhotoPicker.EXTRA_PREVIEW_ENABLED;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_CAMERA;
import static me.iwf.photopicker.PhotoPicker.EXTRA_SHOW_GIF;
import static me.iwf.photopicker.PhotoPicker.KEY_SELECTED_PHOTOS;

public class PhotoPickerActivity extends AppCompatActivity {

    private PhotoPickerFragment pickerFragment;
    private ImagePagerFragment imagePagerFragment;
    private MenuItem menuDoneItem;

    private boolean mShowCamera;
    private boolean mShowGif;
    private boolean mPreviewEnabled;
    private int mMaxCount;
    private int mColumnNumber;
    private boolean mMenuIsInflated;
    private ArrayList<String> mOriginalPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_picker);
        initData();
        initActionBar();
        initView();
    }

    private void initData() {
        mShowCamera = getIntent().getBooleanExtra(EXTRA_SHOW_CAMERA, true);
        mShowGif = getIntent().getBooleanExtra(EXTRA_SHOW_GIF, false);
        mPreviewEnabled = getIntent().getBooleanExtra(EXTRA_PREVIEW_ENABLED, true);
        mMaxCount = getIntent().getIntExtra(EXTRA_MAX_COUNT, DEFAULT_MAX_COUNT);
        mColumnNumber = getIntent().getIntExtra(EXTRA_GRID_COLUMN, DEFAULT_COLUMN_NUMBER);
        mOriginalPhotos = getIntent().getStringArrayListExtra(EXTRA_ORIGINAL_PHOTOS);
        mMenuIsInflated = false;
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("选择图片");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initView() {
        pickerFragment = (PhotoPickerFragment) getSupportFragmentManager().findFragmentByTag("tag");
        if (pickerFragment == null) {
            pickerFragment = PhotoPickerFragment.newInstance(mShowCamera, mShowGif, mPreviewEnabled, mColumnNumber, mMaxCount, mOriginalPhotos);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, pickerFragment, "tag")
                    .commit();
            getSupportFragmentManager().executePendingTransactions();
        }

        pickerFragment.getPhotoGridAdapter().setOnItemCheckListener(new OnItemCheckListener() {

            @Override
            public boolean onItemCheck(int position, Photo photo, final int selectedItemCount) {

                menuDoneItem.setEnabled(selectedItemCount > 0);

                if (mMaxCount <= 1) {
                    List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                    if (!photos.contains(photo.getPath())) {
                        photos.clear();
                        pickerFragment.getPhotoGridAdapter().notifyDataSetChanged();
                    }
                    return true;
                }

                if (selectedItemCount > mMaxCount) {
                    Toast.makeText(getActivity(), getString(R.string.__picker_over_max_count_tips, mMaxCount),
                            LENGTH_LONG).show();
                    return false;
                }
                if (mMaxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, selectedItemCount, mMaxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }

                return true;
            }

        });
    }

    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it complete.
     */
    @Override
    public void onBackPressed() {
        if (imagePagerFragment != null && imagePagerFragment.isVisible()) {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void addImagePagerFragment(ImagePagerFragment imagePagerFragment) {
        this.imagePagerFragment = imagePagerFragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, this.imagePagerFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * 刷新右上角按钮文案
     */
    public void updateTitleDoneItem() {
        if (mMenuIsInflated) {
            if (pickerFragment != null && pickerFragment.isResumed()) {
                List<String> photos = pickerFragment.getPhotoGridAdapter().getSelectedPhotos();
                int size = photos == null ? 0 : photos.size();
                menuDoneItem.setEnabled(size > 0);
                if (mMaxCount > 1) {
                    menuDoneItem.setTitle(getString(R.string.__picker_done_with_count, size, mMaxCount));
                } else {
                    menuDoneItem.setTitle(getString(R.string.__picker_done));
                }

            } else if (imagePagerFragment != null && imagePagerFragment.isResumed()) {//预览界面 完成总是可点的，没选就把默认当前图片
                menuDoneItem.setEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mMenuIsInflated) {
            getMenuInflater().inflate(R.menu.__picker_menu_picker, menu);
            menuDoneItem = menu.findItem(R.id.action_done);
            if (mOriginalPhotos != null && mOriginalPhotos.size() > 0) {
                menuDoneItem.setEnabled(true);
                menuDoneItem.setTitle(
                        getString(R.string.__picker_done_with_count, mOriginalPhotos.size(), mMaxCount));
            } else {
                menuDoneItem.setEnabled(false);
            }
            mMenuIsInflated = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.action_done) {
            Intent intent = new Intent();
            ArrayList<String> selectedPhotos = null;
            if (pickerFragment != null) {
                selectedPhotos = pickerFragment.getPhotoGridAdapter().getSelectedPhotoPaths();
            }
            //当在列表没有选择图片，又在详情界面时默认选择当前图片
            if (selectedPhotos.size() <= 0) {
                if (imagePagerFragment != null && imagePagerFragment.isResumed()) {
                    // 预览界面
                    selectedPhotos = imagePagerFragment.getCurrentPath();
                }
            }
            if (selectedPhotos != null && selectedPhotos.size() > 0) {
                intent.putStringArrayListExtra(KEY_SELECTED_PHOTOS, selectedPhotos);
                setResult(RESULT_OK, intent);
                finish();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public PhotoPickerActivity getActivity() {
        return this;
    }

    public boolean isShowGif() {
        return mShowGif;
    }

    public void setShowGif(boolean showGif) {
        mShowGif = showGif;
    }

}
