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
package net.schueller.instarepost.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import net.schueller.instarepost.R;
import net.schueller.instarepost.activities.MainActivity;
import net.schueller.instarepost.models.Post;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(downloadId);

            assert manager != null;
            Cursor cursor = manager.query(query);

            if (cursor.moveToFirst()) {

                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                String uri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI));
                String localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                String filePath = localUri.replace("file://", "");

                String TAG = "DownloadReceiver";
                Log.v(TAG, "COLUMN_URI: " + uri);
                //Log.v(TAG, "COLUMN_LOCAL_URI: "+ localUri);
                //Log.v(TAG, "downloadFilePath: "+ filePath);

                Post post = Post.getByUrl(uri);

                if (DownloadManager.STATUS_FAILED == status) {
                    post.setStatus(Post.DOWNLOAD_FAILED);
                    post.save();
                } else {
                    post.setStatus(Post.DOWNLOAD_COMPLETE);
                    post.save();

                    //Log.v(TAG, "post: "+ post);

                    // get Data from db
                    MainActivity.presentHeadsUpNotification(context, R.mipmap.ic_launcher, filePath, post);
                }
            }

            cursor.close();


        }


    }

}