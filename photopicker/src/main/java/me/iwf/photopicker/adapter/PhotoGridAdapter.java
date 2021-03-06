package me.iwf.photopicker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import me.iwf.photopicker.entity.Photo;
import me.iwf.photopicker.entity.PhotoDirectory;
import me.iwf.photopicker.event.OnItemCheckListener;
import me.iwf.photopicker.event.OnPhotoClickListener;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;
import me.iwf.photopicker.utils.MediaStoreHelper;

/**
 * Created by donglua on 15/5/31.
 */
public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.ViewHolder> {

    private final static int TYPE_CAMERA = 0;
    private final static int TYPE_PHOTO = 1;

    private LayoutInflater mInflater;
    private RequestManager mGlide;
    private boolean mHasCamera;
    private boolean mPreviewEnable;
    private int mImageSize;
    private int mColumnNumber;

    private OnItemCheckListener mOnItemCheckListener;
    private OnPhotoClickListener mOnPhotoClickListener;
    private View.OnClickListener mOnCameraClickListener;

    public PhotoGridAdapter(Context context, RequestManager requestManager, List<PhotoDirectory> directories, List<String> photos, int colNum) {
        mInflater = LayoutInflater.from(context);
        mGlide = requestManager;

        mColumnNumber = colNum;
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        mImageSize = widthPixels / mColumnNumber;
        mHasCamera = true;
        mPreviewEnable = true;
        mPhotoDirectories = (directories == null ? new ArrayList<PhotoDirectory>() : directories);
        mSelectedPhotos.clear();
        if (photos != null) {
            mSelectedPhotos.addAll(photos);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (showCamera() && position == 0) ? TYPE_CAMERA : TYPE_PHOTO;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.picker_item_grid_photo, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int type = getItemViewType(position);

        switch (type) {

            case TYPE_CAMERA: {
                holder.iv_selector.setVisibility(View.GONE);

                holder.iv_photo.setScaleType(ImageView.ScaleType.CENTER);
                holder.iv_photo.setImageResource(R.mipmap.picker_ic_camera);
                holder.iv_photo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mOnCameraClickListener != null) {
                            mOnCameraClickListener.onClick(view);
                        }
                    }

                });
            }
            break;

            case TYPE_PHOTO: {
                holder.iv_selector.setVisibility(View.VISIBLE);

                List<Photo> photos = getCurrentPhotos();
                final Photo photo = photos.get(showCamera() ? (position - 1) : position);
                final boolean isChecked = isSelected(photo);

                holder.iv_selector.setSelected(isChecked);
                holder.iv_selector.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int pos = holder.getAdapterPosition();
                        boolean isEnable = true;

                        if (mOnItemCheckListener != null) {
                            isEnable = mOnItemCheckListener.onItemCheck(pos, photo, getSelectedPhotos().size() + (isSelected(photo) ? -1 : 1));
                        }
                        if (isEnable) {
                            toggleSelection(photo);
                            notifyItemChanged(pos);
                        }
                    }

                });

                holder.iv_photo.setSelected(isChecked);
                if (AndroidLifecycleUtils.canLoadImage(holder.iv_photo)) {
                    final RequestOptions options = new RequestOptions()
                            .centerCrop()
                            .dontAnimate()
                            .override(mImageSize, mImageSize)
                            .error(R.mipmap.picker_ic_empty);

                    mGlide.setDefaultRequestOptions(options)
                            .load(new File(photo.getPath()))
                            .thumbnail(0.5f)
                            .into(holder.iv_photo);
                }
                holder.iv_photo.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mPreviewEnable) {
                            if (mOnPhotoClickListener != null) {
                                int pos = holder.getAdapterPosition();

                                mOnPhotoClickListener.onClick(view, pos, showCamera());
                            }

                        } else {
                            holder.iv_selector.performClick();
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
        int photosCount = (mPhotoDirectories.size() == 0 ? 0 : getCurrentPhotos().size());
        return showCamera() ? (photosCount + 1) : photosCount;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        mGlide.clear(holder.iv_photo);
        super.onViewRecycled(holder);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView iv_photo;
        final ImageView iv_selector;

        public ViewHolder(View itemView) {
            super(itemView);
            iv_photo = itemView.findViewById(R.id.iv_photo);
            iv_selector = itemView.findViewById(R.id.iv_selector);
        }

    }

    public void setOnItemCheckListener(OnItemCheckListener onItemCheckListener) {
        mOnItemCheckListener = onItemCheckListener;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener onPhotoClickListener) {
        mOnPhotoClickListener = onPhotoClickListener;
    }

    public void setOnCameraClickListener(View.OnClickListener onCameraClickListener) {
        mOnCameraClickListener = onCameraClickListener;
    }

    public List<String> getSelectedPhotoPaths() {
        List<String> selectedPhotoPaths = new ArrayList<>(getSelectedItemCount());

        for (String photo : mSelectedPhotos) {
            selectedPhotoPaths.add(photo);
        }

        return selectedPhotoPaths;
    }

    public void setShowCamera(boolean hasCamera) {
        mHasCamera = hasCamera;
    }

    public void setPreviewEnable(boolean previewEnable) {
        mPreviewEnable = previewEnable;
    }

    public boolean showCamera() {
        return (mHasCamera && currentDirectoryIndex == MediaStoreHelper.INDEX_ALL_PHOTOS);
    }

}
