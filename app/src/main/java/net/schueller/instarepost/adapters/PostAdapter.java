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
package net.schueller.instarepost.adapters;

import android.content.Context;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.joanzapata.iconify.widget.IconTextView;
import com.squareup.picasso.Picasso;

import net.schueller.instarepost.R;
import net.schueller.instarepost.helpers.Intents;
import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.models.Post;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private ArrayList<Post> postList = new ArrayList<>();

    private Context mContext;

    // Pass in the contact array into the constructor
    public PostAdapter(Context context) {
        this.mContext = context;
    }


    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder viewHolder, int position) {

        final int currentPosition = viewHolder.getAdapterPosition();

        try {
            JSONObject postMetaJSON = new JSONObject(postList.get(currentPosition).getJsonMeta());

            viewHolder.captionTextView.setText(Parser.getCaption(postMetaJSON));
            viewHolder.usernameTextView.setText(Parser.getUsername(postMetaJSON));

            String subPath = "instarepost";
            File path = new File(Environment.getExternalStorageDirectory(), subPath);

            final File myImageFile = new File(path, postList.get(currentPosition).getImageFile());
            if (myImageFile.exists() && postList.get(currentPosition).getIsVideo() == 0) {

                Picasso.with(this.mContext).load(myImageFile)
//                        .error(R.drawable.common_full_open_on_phone)
//                        .placeholder(R.drawable.common_full_open_on_phone)
                        .into(viewHolder.postImageView);

                viewHolder.isVideo.setVisibility(View.INVISIBLE);

            } else if (postList.get(currentPosition).getIsVideo() == 1) {
                String imageUrl = Parser.getDisplayUrl(postMetaJSON);

                Picasso.with(this.mContext).load(imageUrl)
//                        .error(R.drawable.common_full_open_on_phone)
//                        .placeholder(R.drawable.common_full_open_on_phone)
                        .into(viewHolder.postImageView);

                viewHolder.isVideo.setVisibility(View.VISIBLE);
            }

            // image/video click
            viewHolder.postImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intents.view(v.getContext(), postList.get(currentPosition).getId());
                }
            });

            viewHolder.usernameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intents.openUsername(v.getContext(), postList.get(currentPosition).getId());
                }
            });

            viewHolder.repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intents.rePost(v.getContext(), postList.get(currentPosition).getId());
                }
            });

            viewHolder.shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intents.share(v.getContext(), postList.get(currentPosition).getId());
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public Post getItem(int position) {
        return postList.get(position);
    }

    public void addAll(List<Post> lst) {
        postList.addAll(lst);
    }

    public void removeAt(int position) {
        postList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, postList.size());
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {

        ImageView postImageView;

        TextView captionTextView, usernameTextView, isVideo;

        Button repostButton, shareButton;

        PostViewHolder(View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.image);
            captionTextView = itemView.findViewById(R.id.caption);
            usernameTextView = itemView.findViewById(R.id.username);
            isVideo = ((IconTextView) itemView.findViewById(R.id.isVideo));
            shareButton = itemView.findViewById(R.id.share_button);
            repostButton = itemView.findViewById(R.id.repost_button);
        }
    }

}