package net.schueller.instarepost.network;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.raizlabs.android.dbflow.sql.language.Select;

import net.schueller.instarepost.R;
import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.models.Node;
import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.models.Post_Table;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.CLIPBOARD_SERVICE;
import static net.schueller.instarepost.helpers.Parser.matchInstagramUri;

public class ClipBoardProcessor {

    private static final String TAG = "ClipBoardProcessor";

    private Context mContext;

    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "instarepost" + File.separator;

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
                        parsePageHeaderInfo(parsedUri);
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

    private void parsePageHeaderInfo(String urlStr) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urlStr)
                /* this browser agent thing is important to trick servers into sending us the LARGEST versions of the images */
                .addHeader("user-agent", mContext.getString(R.string.data_instagram_user_agent))
                .build();

        Log.v(TAG, "urlStr: " + urlStr);


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.v(TAG, "http Error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected code " + response);
                    }

                    assert responseBody != null;

                    Document doc = Jsoup.parse(responseBody.string());

                    JSONObject jsonObj = new JSONObject();

                    try {
                        Elements js = doc.select("script[type=text/javascript]");
                        Pattern p = Pattern
                                .compile(mContext.getString(R.string.data_instagram_shared_data_regex_pattern));

                        for (Element Elm : js) {
                            Matcher m = p.matcher(Elm.html());
                            if (m.matches()) {
                                jsonObj = new JSONObject(Objects.requireNonNull(m.group(1)));
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
                                        Downloader.download(mContext.getApplicationContext(), filePath, node.getUrl(),
                                                node.isVideo(), jsonObj);
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
                                    Toast.makeText(mContext,
                                            mContext.getString(R.string.error_private_posts_no_support),
                                            Toast.LENGTH_LONG).show();
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
