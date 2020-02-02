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
package net.schueller.instarepost.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.ClipboardManager;
import android.content.ClipboardManager.OnPrimaryClipChangedListener;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.models.Node;
import net.schueller.instarepost.models.Post_Table;
import net.schueller.instarepost.R;
import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.network.Downloader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class CBWatcherService extends Service {

    static final String TAG = "CBWatcherService";

    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "instarepost" + File.separator;

    private OnPrimaryClipChangedListener listener = new OnPrimaryClipChangedListener() {
        public void onPrimaryClipChanged() {
            performClipboardCheck();
        }
    };

    @Override
    public void onCreate() {
        ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).addPrimaryClipChangedListener(listener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void performClipboardCheck() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        if (clipboardManager.hasPrimaryClip() && clipboardManager.getPrimaryClip() != null) {
            try {

                String clipboardData = clipboardManager.getPrimaryClip().getItemAt(0).getText().toString();

                Pattern p = Pattern.compile(getString(R.string.data_instagram_regex_pattern));
                Matcher m = p.matcher(clipboardData);

                Log.v(TAG, "clipboardData: " + clipboardData);

                if (m.matches()) {

                    String url = m.group(1);
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    // check item is not already in db
                    List<Post> posts = new Select().from(Post.class).where(Post_Table.url.eq(url)).queryList();

                    if (posts.size() == 0) {
                        try {
                            parsePageHeaderInfo(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

            } catch (Exception e) {
                Log.v(TAG, "Unable match" + e.toString());
            }
        }
    }

    private void parsePageHeaderInfo(String urlStr) {

        final Context mContext = this;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urlStr)
                /* this browser agent thing is important to trick servers into sending us the LARGEST versions of the images */
                .addHeader("user-agent", getString(R.string.data_instagram_user_agent))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.v(TAG, "http Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    assert responseBody != null;

                    Document doc = Jsoup.parse(responseBody.string());

                    JSONObject jsonObj = new JSONObject();

                    try {
                        Elements js = doc.select("script[type=text/javascript]");
                        Pattern p = Pattern.compile(getString(R.string.data_instagram_shared_data_regex_pattern));

                        for (Element Elm : js) {
                            Matcher m = p.matcher(Elm.html());
                            if (m.matches()) {
                                jsonObj = new JSONObject(m.group(1));
                            }
                        }

                        Log.v(TAG, "sharedData: " + jsonObj.toString());

                        if (!Parser.isPrivate(jsonObj)) {
                            try {
                                // check if collection of images
                                ArrayList<Node> nodes = Parser.getAllNodes(jsonObj);

                                if (nodes.size() > 0) {
                                    // we have a carousel, download each
                                    for (Node node : nodes) {
                                        Log.v(TAG, "Download: " + node.getUrl());
                                        Downloader.download(mContext, filePath, node.getUrl(), node.isVideo(), jsonObj);
                                    }
                                } else {
                                    Log.v(TAG, "No nodes found");
                                }
                            } catch (Exception e) {
                                Log.v(TAG, "Unable to getAllNodes");
                            }
                        } else {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(mContext, getString(R.string.error_private_posts_no_support), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    } catch (JSONException e) {
                        Log.v(TAG, "Unable to parse sharedData from html page");
                    }

                }
            }
        });

    }
}