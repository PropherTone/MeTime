package com.protone.common.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GalleryBucket {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "galleryBucketId")
    private Long galleryBucketId;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "image")
    private boolean image;

    public GalleryBucket(String type, boolean image) {
        this.type = type;
        this.image = image;
    }

    public Long getGalleryBucketId() {
        return galleryBucketId;
    }

    public void setGalleryBucketId(Long galleryBucketId) {
        this.galleryBucketId = galleryBucketId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GalleryBucket that = (GalleryBucket) o;

        if (image != that.image) return false;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (image ? 1 : 0);
        return result;
    }
}
