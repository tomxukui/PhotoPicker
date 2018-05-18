package me.iwf.photopicker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import me.iwf.photopicker.adapter.PhotoPagerAdapter;

/**
 * Created by donglua on 15/6/24.
 */
public class PhotoPagerActivity extends AppCompatActivity {

    private TextView tv_title;
    private ViewPager viewPager;

    private PhotoPagerAdapter mPagerAdapter;

    private int mCurrentIndex;
    private ArrayList<String> mPhotoPaths;
    private boolean mShowDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.__picker_activity_photo_pager);
        initData();
        initActionBar();
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.__picker_menu_preview, menu);
        menu.findItem(R.id.action_delete).setVisible(mShowDelete);
        return true;
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(PhotoPicker.KEY_SELECTED_PHOTOS, mPhotoPaths);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.action_delete) {
            final int index = viewPager.getCurrentItem();

            if (mPhotoPaths.size() <= 1) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.__picker_confirm_to_delete)
                        .setPositiveButton(R.string.__picker_yes, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                mPhotoPaths.remove(index);
                                mPagerAdapter.notifyDataSetChanged();

                                finish();
                            }

                        })
                        .setNegativeButton(R.string.__picker_cancel, null)
                        .show();
            } else {
                Snackbar.make(viewPager, R.string.__picker_deleted_a_photo, Snackbar.LENGTH_SHORT).show();

                mPhotoPaths.remove(index);
                mPagerAdapter.notifyDataSetChanged();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        mCurrentIndex = getIntent().getIntExtra(PhotoPreview.EXTRA_CURRENT_ITEM, 0);
        mPhotoPaths = getIntent().getStringArrayListExtra(PhotoPreview.EXTRA_PHOTOS);
        mShowDelete = getIntent().getBooleanExtra(PhotoPreview.EXTRA_SHOW_DELETE, true);
        if (mPhotoPaths == null) {
            mPhotoPaths = new ArrayList<>();
        }

        mPagerAdapter = new PhotoPagerAdapter(Glide.with(this), mPhotoPaths);
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_title = findViewById(R.id.tv_title);
        tv_title.setText("图片");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);//不显示默认标题
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示返回键
        }
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(mPagerAdapter);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setCurrentItem(mCurrentIndex);
    }

}
