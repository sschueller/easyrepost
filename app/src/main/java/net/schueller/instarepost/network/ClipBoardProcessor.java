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
package net.schueller.instarepost.network;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.Select;

import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.models.Post_Table;

import java.util.List;

import static android.content.Context.CLIPBOARD_SERVICE;
import static net.schueller.instarepost.helpers.Parser.matchInstagramUri;
import static net.schueller.instarepost.network.MetaDataLoader.GetAndProcessMetaData;

public class ClipBoardProcessor {

    private static final String TAG = "ClipBoardProcessor";

    private Context mContext;

    public ClipBoardProcessor(Context context) {
        mContext = context;
    }

    public boolean processUri(String uri) {

        String parsedUri = matchInstagramUri(uri, mContext);

        if (parsedUri != null) {

            try {

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                // check item is not already in db
                List<Post> posts = new Select().from(Post.class).where(Post_Table.url.eq(parsedUri)).queryList();

                if (posts.size() == 0) {
                    try {

                        Post post = new Post();
                        post.setStatus(Post.DOWNLOAD_PENDING);
                        post.setUrl(parsedUri);
                        post.save();

                        GetAndProcessMetaData(post, mContext);

                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();

                        return false;
                    }
                }

            } catch (Exception e) {
                Log.v(TAG, "Unable match: " + e.toString());
                return false;
            }
        } else {
            Log.v(TAG, "Unable match: " + uri);
        }
        return false;
    }

    public void performClipboardCheck() {
        ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(CLIPBOARD_SERVICE);

        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip() != null) {

            try {

                String clipboardData = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();

                processUri(clipboardData);

            } catch (Exception e) {
                Log.v(TAG, "Unable match" + e.toString());
            }
        }
    }
}
