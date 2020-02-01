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
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import net.schueller.instarepost.AppDatabase;
import net.schueller.instarepost.models.Post_Table;
import net.schueller.instarepost.models.User_Table;

import java.util.List;

@Table(database = AppDatabase.class)

public class User extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id; // package-private recommended, not required

    @Column
    private String username;

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    Post post;

    public List<Post> posts;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "posts")
    public List<Post> getPosts() {
        if (posts == null || posts.isEmpty()) {
            posts = SQLite.select()
                    .from(Post.class)
                    .where(Post_Table.userId.eq(id))
                    .queryList();
        }
        return posts;
    }

    public List<Post> getPosts(DatabaseWrapper databaseWrapper) {
        if (posts == null || posts.isEmpty()) {
            posts = SQLite.select()
                    .from(Post.class)
                    .where(Post_Table.userId.eq(id))
                    .queryList(databaseWrapper);
        }
        return posts;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static User getUserByUsername(String username) {
        return new Select()
                .from(User.class)
                .where(User_Table.username.eq(username))
                .limit(1)
                .querySingle();
    }

    public static User getUserByUsername(String username, DatabaseWrapper databaseWrapper) {
        return new Select()
                .from(User.class)
                .where(User_Table.username.eq(username))
                .limit(1)
                .querySingle(databaseWrapper);
    }
}
