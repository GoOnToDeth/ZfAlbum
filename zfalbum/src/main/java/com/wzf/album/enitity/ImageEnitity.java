package com.wzf.album.enitity;

import java.io.Serializable;

/**
 * ================================================
 * 描    述：相册每张照片对象
 * 作    者：wzf
 * 创建日期：2017/1/11
 * 版    本：1.0
 * 修订历史：
 * ================================================
 */
public class ImageEnitity implements Serializable {

    // 图片ID
    private int imageId;
    // 图片的名称
    private String name;
    // 图片路径
    private String path;
    // 是否被选择
    private boolean isSelected;

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return (name != null) ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return (path != null) ? path : "";
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
