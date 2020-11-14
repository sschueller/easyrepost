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
package net.schueller.instarepost;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.models.Post_Table;
import net.schueller.instarepost.models.User;
import net.schueller.instarepost.models.User_Table;

import java.util.List;

@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    static final String NAME = "Posts"; // we will add the .db extension

    static final int VERSION = 6;

    @Migration(version = 2, database = AppDatabase.class)
    public static class Migration2 extends AlterTableMigration<Post> {

        public Migration2(Class<Post> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.INTEGER, "userId");
        }
    }

    @Migration(version = 5, database = AppDatabase.class)
    public static class Migration3 extends BaseMigration {

        @Override
        public void migrate(DatabaseWrapper database) {
                            /*
            // get all users IDs and create users
            List<Post> posts = SQLite.select().from(Post.class).where(Post_Table.userId.isNull()).queryList(database);

            for (Post post : posts) {

                // add/find user or create new one
                User user = SQLite.select().from(User.class).where(User_Table.username.eq(post.getUsername())).querySingle(database);



                if (user == null) {
                    user = new User();
                    user.setUsername(post.getUsername());
                    SQLite.insert(User.class).values(user).execute(database);
                    user = SQLite.select().from(User.class).where(User_Table.username.eq(post.getUsername())).querySingle(database);
                }
                try {
                    Long userId = user.getId();
                    SQLite.update(Post.class)
                            .set(Post_Table.userId.eq(userId))
                            .where(Post_Table.id.eq(post.getId()))
                            .execute(database);
                } catch (NullPointerException e) {
                    Log.v("DB Migration", "Failed to add userid");
                }

                // find add hashtags
                List<String> hashtags = Util.parseHashTags(post.getCaption());

                for (String hashtagString : hashtags) {

                    Hashtag hashtag = SQLite.select().from(Hashtag.class).where(Hashtag_Table.hashtag.eq(hashtagString)).querySingle(database);
                    if (hashtag == null) {
                        hashtag = new Hashtag();
                        hashtag.setHashtag(hashtagString);
                        SQLite.insert(Hashtag.class).values(hashtag).execute(database);
                        hashtag = SQLite.select().from(Hashtag.class).where(Hashtag_Table.hashtag.eq(hashtagString)).querySingle(database);
                    }
                    // add link
                    Post_Hashtag postHashtag = new Post_Hashtag();
                    postHashtag.setHashtag(hashtag);
                    postHashtag.setPost(post);
                    SQLite.insert(Post_Hashtag.class).values(postHashtag).execute(database);
                }

            }


*/
                            /*
            List<Post> posts = SQLite.select().from(Post.class).where(Post_Table.userId.isNull()).queryList(database);

            for (Post post : posts) {

                // add/find user or create new one
                //User user = User.getUserByUsername(post.getUsername());
                User user = SQLite.select().from(User.class).where(User_Table.username.eq(post.getUsername())).querySingle(database);

                if (user == null) {
                    user = new User();
                    user.setUsername(post.getUsername());
                    user.save(database);
                }
                post.setUserId(user.getId());
                post.save(database);

                // find add hashtags
                List<String> hashtags = Util.parseHashTags(post.getCaption());
                for (String hashtagString : hashtags) {
                    //Hashtag hashtag = Hashtag.getHastagByHashtag(hashtagString);
                    Hashtag hashtag = SQLite.select().from(Hashtag.class).where(Hashtag_Table.hashtag.eq(hashtagString)).querySingle(database);
                    if (hashtag == null) {
                        hashtag = new Hashtag();
                        hashtag.setHashtag(hashtagString);
                        hashtag.save(database);
                    }
                    // add link
                    Post_Hashtag postHashtag = new Post_Hashtag();
                    postHashtag.setHashtag(hashtag);
                    postHashtag.setPost(post);
                    postHashtag.save(database);
                }


            }
*/

        }

    }


    @Migration(version = 6, database = AppDatabase.class)
    public static class Migration4 extends AlterTableMigration<Post> {

        public Migration4(Class<Post> table) {
            super(table);
        }

        @Override
        public void onPreMigrate() {
            addColumn(SQLiteType.INTEGER, "status");
        }
    }


}
