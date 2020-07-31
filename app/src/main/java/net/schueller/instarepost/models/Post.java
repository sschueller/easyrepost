/*
 * Copyright 2020 Stefan Schüller <sschueller@techdroid.com>
 *
 * License: GPL-3.0+
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.schueller.instarepost.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import net.schueller.instarepost.AppDatabase;
import net.schueller.instarepost.models.Post_Table;

import java.util.List;

@Table(database = AppDatabase.class)
@ManyToMany(referencedTable = Hashtag.class)
public class Post extends BaseModel {
    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

    @Column
    @Unique
    private String postId;

    @Column
    private String imageFile;

    @Column
    private String url;

    @Column
    private String caption;

    @Column
    private String username;

    @Column
    private long userId;

    @Column
    private String videoFile;

    @Column
    private int isVideo;

    @Column
    private int status = 1; // status of download, 0 = pending, 1 = downloaded

    @Column
    private String jsonMeta; // private with getters and setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getVideoFile() {
        return videoFile;
    }

    public void setVideoFile(String videoFile) {
        this.videoFile = videoFile;
    }

    public int getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(int isVideo) {
        this.isVideo = isVideo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public String getJsonMeta() {
        return jsonMeta;
    }

    public void setJsonMeta(String jsonMeta) {
        this.jsonMeta = jsonMeta;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static List<Post> createPostsList(int numPosts, int page) {
        return new Select()
                .from(Post.class)
                .where(Post_Table.status.eq(1))
                .orderBy(Post_Table.id, false)
                .offset(page * numPosts)
                .limit(numPosts)
                .queryList();
    }

    public static Post getByUrl(String url) {
        return new Select()
                .from(Post.class)
                .where(Post_Table.url.eq(url))
                .limit(1)
                .querySingle();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
