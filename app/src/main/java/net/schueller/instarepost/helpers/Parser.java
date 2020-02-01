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

import android.util.Log;
import net.schueller.instarepost.models.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

public class Parser {

    private static String TAG = "Parser";

    public static ArrayList<Node> getAllNodes(JSONObject sharedData) {

        ArrayList<Node> nodes = new ArrayList<>();
        JSONObject shortCodeMedia = parseBase(sharedData);

        try {
            JSONArray edges = shortCodeMedia
                    .getJSONObject("edge_sidecar_to_children")
                    .getJSONArray("edges");

            for (int i = 0; i < edges.length(); i++) {
                JSONObject nodeGrp = edges.getJSONObject(i);
                JSONObject jsonNode = nodeGrp.getJSONObject("node");
                nodes.add(processNode(jsonNode));
            }

        } catch (JSONException e) {
            Log.v(TAG, "shortCodeMedia: " + shortCodeMedia.toString());
            nodes.add(processNode(shortCodeMedia));
        }
        return nodes;
    }


    private static Node processNode(JSONObject jsonNode) {

        Node node = new Node();
        try {
            if (isVideo(jsonNode)) {
                node.setUrl(jsonNode.getString("video_url"))
                        .setVideo(true);
            } else {
                node.setUrl(jsonNode.getString("display_url"))
                        .setVideo(false);
            }
        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse display_url JSON block");
        }

        return node;
    }

    public static String getDisplayUrl(JSONObject postMetaJSON) {
        String displayUrl = "";
        try {
            displayUrl = parseBase(postMetaJSON).getString("display_url");
        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse JSON findImageNew");
        }
        return displayUrl;
    }

    public static String getCaption(JSONObject postMetaJSON) {

        // entry_data -> PostPage -> graphql -> shortcode_media -> edge_media_to_caption -> edges -> node -> text

        String caption = "";
        try {
            caption = parseBase(postMetaJSON)
                    .getJSONObject("edge_media_to_caption")
                    .getJSONArray("edges")
                    .getJSONObject(0)
                    .getJSONObject("node")
                    .getString("text");
        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse JSON for caption");
        }

        return caption;
    }

    public static String getUsername(JSONObject postMetaJSON) {

        // entry_data -> PostPage -> graphql -> shortcode_media -> owner -> username

        String username = "";
        try {
            username = "@" + parseBase(postMetaJSON)
                    .getJSONObject("owner")
                    .getString("username");
        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse JSON for username");
        }

        return username;
    }

    public static boolean isVideo(JSONObject jsonNode) {

        // entry_data -> PostPage -> graphql -> shortcode_media -> video_url

        boolean isVideo = false;

        try {
            isVideo = jsonNode.has("is_video") && jsonNode.getBoolean("is_video");
        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse is_video JSON block");
        }

        return isVideo;
    }


    private static JSONObject parseBase(JSONObject sharedData) {

        // entry_data -> PostPage -> graphql -> shortcode_media

        JSONObject shortcodeMedia = new JSONObject();

        try {
            shortcodeMedia = sharedData
                    .getJSONObject("entry_data")
                    .getJSONArray("PostPage")
                    .getJSONObject(0)
                    .getJSONObject("graphql")
                    .getJSONObject("shortcode_media");

        } catch (JSONException e) {
            Log.v(TAG, "Unable to parse shortcode_media JSON block");
        }

        return shortcodeMedia;
    }

}

