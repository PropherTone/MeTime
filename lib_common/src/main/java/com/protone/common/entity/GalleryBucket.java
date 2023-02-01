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

    public GalleryBucket(String type) {
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GalleryBucket that = (GalleryBucket) o;

        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
