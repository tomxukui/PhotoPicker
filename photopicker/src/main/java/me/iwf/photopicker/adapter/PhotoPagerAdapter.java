package me.iwf.photopicker.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private RequestManager mGlide;
    private List<String> mPaths;
    private LayoutInflater mInflater;

    public PhotoPagerAdapter(Context context, RequestManager glide, List<String> paths) {
        mInflater = LayoutInflater.from(context);
        mGlide = glide;
        mPaths = (paths == null ? new ArrayList<String>() : paths);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View layout = mInflater.inflate(R.layout.picker_item_pager_photo, container, false);

        ImageView iv_img = layout.findViewById(R.id.iv_img);

        String path = mPaths.get(position);
        Uri uri = (path.startsWith("http") ? Uri.parse(path) : Uri.fromFile(new File(path)));

        if (AndroidLifecycleUtils.canLoadImage(container)) {
            RequestOptions options = new RequestOptions()
                    .dontAnimate()
                    .dontTransform()
                    .override(800, 800)
                    .error(R.mipmap.picker_ic_broken_img);

            mGlide.setDefaultRequestOptions(options)
                    .load(uri)
                    .thumbnail(0.1f)
                    .into(iv_img);
        }

        container.addView(layout);

        return layout;
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;

        container.removeView(view);
        mGlide.clear(view);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
