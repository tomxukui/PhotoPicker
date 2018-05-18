package me.iwf.photopicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.R;
import me.iwf.photopicker.entity.PhotoDirectory;

/**
 * Created by donglua on 15/6/28.
 */
public class PopupDirectoryListAdapter extends BaseAdapter {

    private List<PhotoDirectory> mDirectories;
    private RequestManager mGlide;

    public PopupDirectoryListAdapter(RequestManager glide, List<PhotoDirectory> directories) {
        mDirectories = directories;
        mGlide = glide;

        if (mDirectories == null) {
            mDirectories = new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        return mDirectories.size();
    }

    @Override
    public PhotoDirectory getItem(int position) {
        return mDirectories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDirectories.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater mLayoutInflater = LayoutInflater.from(parent.getContext());
            convertView = mLayoutInflater.inflate(R.layout.picker_item_list_popup_directory, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bindData(mDirectories.get(position));
        return convertView;
    }

    class ViewHolder {

        public ImageView ivCover;
        public TextView tvName;
        public TextView tvCount;

        public ViewHolder(View rootView) {
            ivCover = rootView.findViewById(R.id.iv_dir_cover);
            tvName = rootView.findViewById(R.id.tv_dir_name);
            tvCount = rootView.findViewById(R.id.tv_dir_count);
        }

        public void bindData(PhotoDirectory directory) {
            RequestOptions options = new RequestOptions();
            options.dontAnimate()
                    .dontTransform()
                    .override(800, 800)
                    .placeholder(R.drawable.picker_ic_placeholder_img)
                    .error(R.drawable.picker_ic_broken_img);
            mGlide.setDefaultRequestOptions(options)
                    .load(directory.getCoverPath())
                    .thumbnail(0.1f)
                    .into(ivCover);

            tvName.setText(directory.getName());
            tvCount.setText(String.format("%d张", directory.getPhotos().size()));
        }

    }

}
