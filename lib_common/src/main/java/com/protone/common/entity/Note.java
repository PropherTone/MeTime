package com.protone.common.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.protone.common.utils.converters.ListTypeConverter;

@Entity
@TypeConverters(ListTypeConverter.class)
public class Note {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "noteId")
    private Long noteId;

    @ColumnInfo(name = "Note_Title")
    private String title;

    @ColumnInfo(name = "Note_Text")
    private String text;

    @ColumnInfo(name = "Note_TitleImage")
    private String imagePath;

    @ColumnInfo(name = "Note_Time")
    private Long time;

    @ColumnInfo(name = "Note_RichCode")
    private int richCode;

    @Ignore
    public Note() {
    }

    public Note(@NonNull String title, String text, String imagePath, Long time, int richCode) {
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.time = time;
        this.richCode = richCode;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public int getRichCode() {
        return richCode;
    }

    public void setRichCode(int richCode) {
        this.richCode = richCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        if (!noteId.equals(note.noteId)) return false;
        if (richCode != note.richCode) return false;
        if (!title.equals(note.title)) return false;
        if (text != null && !text.equals(note.text)) return false;
        if (imagePath == null || note.imagePath == null || !imagePath.equals(note.imagePath))
            return false;
        return time.equals(note.time);
    }

    @Override
    public int hashCode() {
        int result = noteId != null ? noteId.hashCode() : 0;
        result = 31 * result + title.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + imagePath.hashCode();
        result = 31 * result + time.hashCode();
        result = 31 * result + richCode;
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Note{" +
                "id=" + noteId +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", time='" + time + '\'' +
                ", richCode=" + richCode +
                '}';
    }
}
