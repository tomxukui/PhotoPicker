package me.iwf.PhotoPickerDemo;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.event.OnPhotoClickListener;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private final static int TYPE_ADD = 1;
    private final static int TYPE_PHOTO = 2;

    private LayoutInflater mInflater;

    private List<String> mPhotos;
    private int mMaxCount;
    private OnPhotoClickListener mOnPhotoClickListener;
    private View.OnClickListener mOnAddClickListener;

    public PhotoAdapter(Context context, List<String> photos, int maxCount) {
        mInflater = LayoutInflater.from(context);
        mPhotos = (photos == null ? new ArrayList<String>() : photos);
        mMaxCount = maxCount;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_grid_photo, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        int type = getItemViewType(position);

        switch (type) {

            case TYPE_ADD: {
                holder.iv_photo.setImageResource(R.mipmap.ic_add);
                holder.iv_photo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mOnAddClickListener != null) {
                            mOnAddClickListener.onClick(holder.iv_photo);
                        }
                    }

                });
            }
            break;

            case TYPE_PHOTO: {
                Uri uri = Uri.fromFile(new File(mPhotos.get(position)));

                if (AndroidLifecycleUtils.canLoadImage(holder.iv_photo)) {
                    RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .error(R.mipmap.picker_ic_empty);

                    GlideApp.with(holder.iv_photo)
                            .load(uri)
                            .apply(options)
                            .thumbnail(0.1f)
                            .into(holder.iv_photo);
                }

                holder.iv_photo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mOnPhotoClickListener != null) {
                            mOnPhotoClickListener.onClick(holder.iv_photo, position, false);
                        }
                    }

                });
            }
            break;

            default:
                break;

        }

    }

    @Override
    public int getItemCount() {
        int count = mPhotos.size() + 1;
        if (count > mMaxCount) {
            count = mMaxCount;
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == mPhotos.size() && position != mMaxCount) ? TYPE_ADD : TYPE_PHOTO;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_photo;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
        }

    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        mOnPhotoClickListener = listener;
    }

    public void setOnAddClickListener(View.OnClickListener listener) {
        mOnAddClickListener = listener;
    }

}
