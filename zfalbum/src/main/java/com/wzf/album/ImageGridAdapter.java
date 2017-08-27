package com.wzf.album;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.wzf.album.enitity.ImageEnitity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 描    述：九宫格相册适配器
 * 作    者：wzf
 * ================================================
 */
public class ImageGridAdapter extends BaseAdapter {

    // 是否是单选 true为单选，false为多选
    private boolean SINGLE_CHECK;

    private LayoutInflater inflater;
    private Context context;
    // 当前相册的全部照片
    private List<ImageEnitity> mImageListAll;
    // 多选，选中的图片列表
    private List<ImageEnitity> mImageListChecked = new ArrayList<>();
    private OnCheckListener checkListener;

    // 最多能选择的照片数量
    private int maxCheckPhotoCount;
    // 还能选择的照片数量
    private int allowCheckPhotoCount;

    public ImageGridAdapter(Context context, List<ImageEnitity> imageEnitities,List<ImageEnitity> mImageListChecked, boolean SINGLE_CHECK) {
        this.context = context;
        this.mImageListAll = imageEnitities;
        this.mImageListChecked.addAll(mImageListChecked);
        this.SINGLE_CHECK = SINGLE_CHECK;
        this.maxCheckPhotoCount = Constants.MAX_CHECK_COUNT;
        this.allowCheckPhotoCount = Constants.MAX_CHECK_COUNT;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mImageListAll.size();
    }

    @Override
    public Object getItem(int i) {
        return mImageListAll.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        ImageEnitity imageEnitity = mImageListAll.get(position);
        if (view == null) {
            view = inflater.inflate(R.layout.item_photo, null);
            holder = new ViewHolder();
            holder.ivPhoto = (ImageView) view.findViewById(R.id.iv_image);
            holder.ivToggle = (ImageView) view.findViewById(R.id.iv_toggle_check);
            holder.vBg = view.findViewById(R.id.v_bg);
            view.setTag(holder);
        } else
            holder = (ViewHolder) view.getTag();
        if (SINGLE_CHECK) {
            holder.ivToggle.setVisibility(View.GONE);
            holder.vBg.setVisibility(View.GONE);
        } else {
            setCheckEvent(holder.ivToggle, imageEnitity);
            if (imageEnitity.isSelected()) {
                holder.ivToggle.setImageResource(R.drawable.ic_photo_checked);
                holder.vBg.setVisibility(View.VISIBLE);
            } else {
                holder.ivToggle.setImageResource(R.drawable.ic_photo_uncheck);
                holder.vBg.setVisibility(View.GONE);
            }
        }
        ImageLoader.load(context, holder.ivPhoto, imageEnitity.getPath());
        return view;
    }

    private void setCheckEvent(ImageView ivCheck, final ImageEnitity imageEnitity) {
        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!imageEnitity.isSelected() && allowCheckPhotoCount == 0) {
                    Toast.makeText(context, "最多选择" + maxCheckPhotoCount + "张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                imageEnitity.setSelected(!imageEnitity.isSelected());
                notifyDataSetChanged();
                if (imageEnitity.isSelected()) {
                    mImageListChecked.add(imageEnitity);
                } else {
                    mImageListChecked.remove(imageEnitity);
                }
                if (checkListener != null)
                    checkListener.checkPhotoStatus(mImageListChecked);
            }
        });
    }

    public void changeDataSet(List<ImageEnitity> imageEnitities) {
        this.mImageListAll = imageEnitities;
        notifyDataSetChanged();
    }

    public void setOnCheckListener(OnCheckListener checkListener) {
        this.checkListener = checkListener;
    }

    public void setAllowCheckPhotoCount(int allowCheckPhotoCount) {
        this.allowCheckPhotoCount = allowCheckPhotoCount;
    }

    public void updateImageListChecked(ImageEnitity imageEnitity) {
        if (imageEnitity.isSelected()) {
            mImageListChecked.add(imageEnitity);
        } else {
            mImageListChecked.remove(imageEnitity);
        }
    }

    public interface OnCheckListener {
        void checkPhotoStatus(List<ImageEnitity> imageListChecked);
    }

    class ViewHolder {
        private ImageView ivPhoto;
        private ImageView ivToggle;
        private View vBg;

        public ViewHolder() {
        }
    }
}
