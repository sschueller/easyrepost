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

import static net.schueller.instarepost.models.Post.DOWNLOAD_FAILED;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.schueller.instarepost.R;
import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.models.Node;
import net.schueller.instarepost.models.Post;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MetaDataLoader {

    private static String TAG = "MetaDataLoader";

    public static void GetAndProcessMetaData(Post post, Context context) {

        post.setStatus(0);
        post.save();

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "instarepost" + File.separator;

        String url = post.getUrl();

        Log.v(TAG, "urlStr: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                /* this browser agent thing is important to trick servers into sending us the LARGEST versions of the images */
                .addHeader("user-agent", context.getString(R.string.data_instagram_user_agent))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.v(TAG, "http Error: " + e.getMessage());
                post.setStatus(DOWNLOAD_FAILED);
                post.save();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        post.setStatus(DOWNLOAD_FAILED);
                        post.save();
                        throw new IOException("Unexpected code " + response);
                    }

                    assert responseBody != null;

                    Document doc = Jsoup.parse(responseBody.string());
                    JSONObject jsonObj = new JSONObject();

                    try {
                        Elements js = doc.select("script[type=text/javascript]");
                        Pattern p = Pattern
                                .compile(context.getString(R.string.data_instagram_shared_data_regex_pattern));

                        for (Element Elm : js) {
                            Matcher m = p.matcher(Elm.html());
                            if (m.matches()) {
                                jsonObj = new JSONObject(Objects.requireNonNull(m.group(1)));
                            }
                        }

                        if (!Parser.isPrivate(jsonObj)) {
                            try {
                                // check if collection of images
                                ArrayList<Node> nodes = Parser.getAllNodes(jsonObj);

                                if (nodes.size() > 0) {

                                    // remove temp post
                                    post.delete();

                                    // we have a carousel, download each
                                    for (Node node : nodes) {
                                        Log.v(TAG, "Download: " + node.getUrl());
                                        Downloader.download(context.getApplicationContext(), filePath, node.getUrl(),
                                                node.isVideo(), jsonObj);
                                    }
                                } else {
                                    Log.v(TAG, "No nodes found");
                                }
                            } catch (Exception e) {
                                Log.v(TAG, "Unable to getAllNodes");
                            }
                        } else {
                            new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context,
                                    context.getString(R.string.error_private_posts_no_support),
                                    Toast.LENGTH_LONG).show());
                        }

                    } catch (JSONException e) {
                        Log.v(TAG, "Unable to parse sharedData from html page");
                    }

                }
            }
        });
    }

}
