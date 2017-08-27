package com.wzf.album;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;


import com.wzf.album.enitity.AlbumEnitity;
import com.wzf.album.enitity.ImageEnitity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ================================================
 * 描    述：相册数据源
 * 作    者：wzf
 * 创建日期：2017/1/11
 * 版    本：1.0
 * 修订历史：
 * ================================================
 */
public class AlbumDataSource extends AsyncTask<Void, Object, Object> {

    private OnAlbumBuildFinished listener;
    private Context context;
    // 默认被选中的数据源
    private Map<String, ImageEnitity> mIntentMap = new HashMap<>();

    private List<ImageEnitity> checkedImageList = new ArrayList<>();

    public AlbumDataSource(Context context, Map<String, ImageEnitity> mIntentMap) {
        this.context = context;
        this.mIntentMap = mIntentMap;
    }

    @Override
    protected Object doInBackground(Void... voids) {
        return getAlbums();
    }

    @Override
    protected void onPostExecute(Object o) {
        List<AlbumEnitity> albumList = (List<AlbumEnitity>) o;
        if (listener != null)
            listener.onDataSourceBuildFinish(albumList, checkedImageList);
    }

    public List<AlbumEnitity> getAlbums() {
        // 构造相册索引
        String[] columns = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.PICASA_ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.SIZE, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns,
                MediaStore.Images.Media.MIME_TYPE + "=? or " + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"},
                MediaStore.Images.Media.DATE_MODIFIED + " desc"
        );
        if (cursor == null) return null;
        List<ImageEnitity> imageTotalList = new ArrayList<>();
        List<AlbumEnitity> albumList = new ArrayList<>();
        HashMap<String, AlbumEnitity> albumMap = new HashMap<>();
        while (cursor.moveToNext()) {
            // 照片ID
            int imageId = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            // 图片路径
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            // 相册ID
            String albumId = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID));
            // 相册名称
            String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));

            // 单张照片
            ImageEnitity imageEnitity = new ImageEnitity();
            imageEnitity.setImageId(imageId);
            imageEnitity.setPath(path);
            if (mIntentMap.get(path) == null) {
                imageEnitity.setSelected(false);
            } else {
                imageEnitity.setSelected(true);
                checkedImageList.add(imageEnitity);
            }

            // 相册对象
            AlbumEnitity mPhotoAlbum = albumMap.get(albumId);
            if (mPhotoAlbum == null) {
                mPhotoAlbum = new AlbumEnitity();
                mPhotoAlbum.setTopImageEnitity(imageEnitity);
                mPhotoAlbum.setAlbumName(albumName);
                mPhotoAlbum.setImageEnitities(new ArrayList<ImageEnitity>());
                albumMap.put(albumId, mPhotoAlbum);
                albumList.add(mPhotoAlbum);
            }

            mPhotoAlbum.getImageEnitities().add(imageEnitity);
            mPhotoAlbum.setImageCounts(mPhotoAlbum.getImageEnitities().size());

            imageTotalList.add(imageEnitity);
        }
        AlbumEnitity albumEnitity = new AlbumEnitity();
        albumEnitity.setImageCounts(imageTotalList.size());
        albumEnitity.setImageEnitities(imageTotalList);
        albumEnitity.setTopImageEnitity(imageTotalList.get(0));
        albumEnitity.setAlbumName("全部照片");

        albumList.add(0, albumEnitity);
        return albumList;
    }

    public void setOnAlbumBuildFinishedListener(OnAlbumBuildFinished listener) {
        this.listener = listener;
    }

    public interface OnAlbumBuildFinished {
        void onDataSourceBuildFinish(List<AlbumEnitity> albumList, List<ImageEnitity> checkedImageList);
    }
}
