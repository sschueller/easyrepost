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

import static net.schueller.instarepost.helpers.Util.getFullFilePath;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;
import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.models.Post_Hashtag;
import net.schueller.instarepost.R;
import net.schueller.instarepost.helpers.Util;
import net.schueller.instarepost.models.Hashtag;
import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.models.User;

import org.json.JSONObject;
import java.io.File;
import java.util.List;

public class Downloader {

    private static String TAG = "Downloader";

    private static String getApplicationName(Context context) {
        int stringId = context.getApplicationInfo().labelRes;
        return context.getString(stringId);
    }

    public static void download(Context context, String url, final boolean isVideo,
            final JSONObject postMetaJSON) {

        Log.v(TAG, "Download start");

        final String fileName = URLUtil.guessFileName(url, null, null);

        final File myFile = getFullFilePath(fileName, context);

        Uri uri = Uri.fromFile(myFile);

        Log.v(TAG, "Download uri: " + uri.toString());

        if (!myFile.exists()) {


            Log.v(TAG, "new downloadManager");

            DownloadManager downloadManager = (DownloadManager) (context)
                    .getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            request
//                        .setAllowedNetworkTypes(
//                        DownloadManager.Request.NETWORK_WIFI
//                                | DownloadManager.Request.NETWORK_MOBILE)
//                        .setAllowedOverRoaming(false)
                    .setDescription("Downloading via " + getApplicationName(context) + "..")
                    .setTitle(fileName)
                    .setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE)
                    .setDestinationUri(uri);

            Log.v(TAG, "downloadManager enqueue");

            String username = Parser.getUsername(postMetaJSON);
            String caption = Parser.getCaption(postMetaJSON);

            try {

                Log.v(TAG, "to save: " + fileName);

                Post post = new Post();
                post.setImageFile(fileName);
                //post.setUrl(shareUrl);
                post.setIsVideo(isVideo ? 1 : 0);
                if (isVideo) {
                    post.setVideoFile(fileName);
                }
                post.setStatus(Post.DOWNLOAD_DOWNLOADING);
                post.setUrl(url);
                post.setCaption(caption);
                post.setUsername(username);
                post.setJsonMeta(postMetaJSON.toString());

                // add/find user or create new one
                User user = User.getUserByUsername(username);
                if (user == null) {
                    user = new User();
                    user.setUsername(username);
                    user.save();
                }
                post.setUserId(user.getId());

                post.save();

                Log.v(TAG, "post: " + post.getUrl());

                // find add hashtags
                List<String> hashtags = Util.parseHashTags(caption);
                for (String hashtagString : hashtags) {
                    Hashtag hashtag = Hashtag.getHastagByHashtag(hashtagString);
                    if (hashtag == null) {
                        hashtag = new Hashtag();
                        hashtag.setHashtag(hashtagString);
                        hashtag.save();
                    }
                    // add link
                    Post_Hashtag postHashtag = new Post_Hashtag();
                    postHashtag.setHashtag(hashtag);
                    postHashtag.setPost(post);
                    postHashtag.save();
                }

                // start download
                downloadManager.enqueue(request);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Log.v(TAG, "File with name exists: " + fileName);
            Toast.makeText(context, R.string.file_exists, Toast.LENGTH_LONG).show();

        }
    }

}
