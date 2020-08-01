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
package net.schueller.instarepost.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;

import androidx.core.content.FileProvider;

import android.webkit.MimeTypeMap;

import com.raizlabs.android.dbflow.sql.language.Select;

import net.schueller.instarepost.BuildConfig;
import net.schueller.instarepost.models.Post_Table;
import net.schueller.instarepost.R;
import net.schueller.instarepost.models.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import in.srain.cube.clipboardcompat.ClipboardManagerCompat;
import in.srain.cube.clipboardcompat.ClipboardManagerCompatFactory;

public class Intents {

    private static String subPath = "instarepost";
    private static String instagramPackage = "com.instagram.android";

    public static void share(Context context, Long postId) {
        Post post = loadPost(postId);

        if (post != null && post.exists()) {

            Uri fileURI = loadResource(post, context);

            if (fileURI != null) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_STREAM, fileURI);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType(getMimeType(fileURI.toString()));

                context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.send_to)));

            }
        }

    }

    public static void view(Context context, Long postId) {
        Post post = loadPost(postId);

        if (post != null && post.exists()) {

            Uri fileURI = loadResource(post, context);

            if (fileURI != null) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setDataAndType(fileURI, getMimeType(fileURI.toString()));

                context.startActivity(Intent.createChooser(intent, context.getResources().getText(R.string.send_to)));

            }
        }
    }

    public static void rePost(Context context, Long postId) {

        Post post = loadPost(postId);

        if (post != null && post.exists()) {

            Uri fileURI = loadResource(post, context);

            if (fileURI != null) {

                try {

                    JSONObject postMetaJSON = new JSONObject(post.getJsonMeta());

                    String caption = Parser.getCaption(postMetaJSON);
                    String username = Parser.getUsername(postMetaJSON);

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    String customCaptionPref = sharedPref.getString("example_text", context.getString(R.string.pref_default_caption));

                    final ClipboardManagerCompat clipboardManager = ClipboardManagerCompatFactory.create(context);

                    String clipText = username + "\n---\n" + caption;

                    if (!"".equals(customCaptionPref)) {
                        assert customCaptionPref != null;
                        clipText = customCaptionPref.replace("%username%", username);
                        clipText = clipText.replace("%caption%", caption);
                        clipText = clipText.replace("%nl%", "\n");
                    }

                    clipboardManager.setText(clipText);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setPackage(instagramPackage);

                    // Set the MIME type
                    intent.setType((post.getIsVideo() == 1) ? "video/*" : "image/*");

                    // Add the URI to the Intent.
                    intent.putExtra(Intent.EXTRA_STREAM, fileURI);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    context.startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void openUsername(Context context, Long postId) {
        Post post = loadPost(postId);

        if (post != null && post.exists()) {

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_VIEW);
            shareIntent.setData(Uri.parse("https://www.instagram.com/_u/" + post.getUsername().substring(1) + "/"));

            context.startActivity(shareIntent);
        }
    }

    private static Post loadPost(Long postId) {
        return new Select().from(Post.class).where(Post_Table.id.eq(postId)).querySingle();
    }

    private static Uri loadResource(Post post, Context context) {
        File path = new File(Environment.getExternalStorageDirectory(), subPath);
        File myFile = new File(path, (post.getIsVideo() == 1) ? post.getVideoFile() : post.getImageFile());
        if (myFile.exists()) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", myFile);
        } else {
            return null;
        }
    }

    // url = file path or whatever suitable URL you want.
    private static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
