package me.iwf.photopicker.adapter;

import android.app.Activity;
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
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

/**
 * Created by donglua on 15/6/21.
 */
public class PhotoPagerAdapter extends PagerAdapter {

    private List<String> paths;
    private RequestManager mGlide;

    public PhotoPagerAdapter(RequestManager glide, List<String> paths) {
        this.paths = paths;
        this.mGlide = glide;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Context context = container.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.picker_item_pager_photo, container, false);
        final ImageView iv_img = itemView.findViewById(R.id.iv_img);

        final String path = paths.get(position);
        final Uri uri;
        if (path.startsWith("http")) {
            uri = Uri.parse(path);
        } else {
            uri = Uri.fromFile(new File(path));
        }

        boolean canLoadImage = AndroidLifecycleUtils.canLoadImage(context);

        if (canLoadImage) {
            final RequestOptions options = new RequestOptions();
            options.dontAnimate()
                    .dontTransform()
                    .override(800, 800)
                    .error(R.drawable.picker_ic_broken_img);

            mGlide.setDefaultRequestOptions(options).load(uri)
                    .thumbnail(0.1f)
                    .into(iv_img);
        }

        iv_img.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (context instanceof Activity) {
                    if (!((Activity) context).isFinishing()) {
                        ((Activity) context).onBackPressed();
                    }
                }
            }

        });
        
        container.addView(itemView);
        return itemView;
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mGlide.clear((View) object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
