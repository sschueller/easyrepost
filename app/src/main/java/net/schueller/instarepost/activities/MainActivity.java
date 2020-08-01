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
package net.schueller.instarepost.activities;

import android.Manifest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import android.net.Uri;

import android.os.Build;
import android.os.Environment;

import android.os.Handler;

import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;

import androidx.core.app.NotificationCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import org.json.JSONObject;
import java.io.File;
import java.util.List;

import net.schueller.instarepost.helpers.Parser;
import net.schueller.instarepost.network.ClipBoardProcessor;
import net.schueller.instarepost.services.CBWatcherService;
import net.schueller.instarepost.ui.EndlessRecyclerViewScrollListener;
import net.schueller.instarepost.InstarepostApplication;
import net.schueller.instarepost.ui.ItemClickSupport;
import net.schueller.instarepost.models.Post;
import net.schueller.instarepost.R;
import net.schueller.instarepost.adapters.PostAdapter;
import net.schueller.instarepost.receivers.RepostReceiver;
import net.schueller.instarepost.receivers.ShareReceiver;

import static net.schueller.instarepost.helpers.NotificationID.getID;


public class MainActivity extends AppCompatActivity {

    static String TAG = "MainActivity";

    String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "instarepost" + File.separator;

    private boolean AppRated;

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    private String ratePref = "apprated";


    public List<Post> mPosts;

    public PostAdapter rvAdapter;


    private Intent CBListenerIntent;

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat
                .checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED
                || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        Log.v(TAG, "SDK: " + Build.VERSION.SDK_INT);

        super.onResume();

        setContentView(R.layout.main_activity);
        verifyStoragePermissions(this);
        drawList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);

        // Set an icon in the ActionBar
        menu.findItem(R.id.action_open_instagram).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_instagram)
                        .colorRes(R.color.cardview_light_background)
                        .actionBarSize());

        menu.findItem(R.id.action_help).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_question)
                        .colorRes(R.color.cardview_light_background)
                        .actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_settings:
                //Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.action_open_instagram:
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                if (launchIntent != null) {

                    Toast.makeText(this, getString(R.string.toast_launching_instagram), Toast.LENGTH_SHORT).show();
                    startActivity(launchIntent);//null pointer check in case package name was not found
                } else {

                    Toast.makeText(this, getString(R.string.toast_please_install_instagram), Toast.LENGTH_LONG)
                            .show();
                }
                break;
            case R.id.action_help:

                Intent introIntent = new Intent(this, IntroActivity.class);
                startActivity(introIntent);

                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                ClipBoardProcessor cbp = new ClipBoardProcessor(getApplicationContext());
                if (cbp.processUri(sharedText)) {
                    // go back to instagram
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
                    if (launchIntent != null) {

                        Toast.makeText(this, getString(R.string.toast_launching_instagram), Toast.LENGTH_SHORT)
                                .show();
                        startActivity(launchIntent);//null pointer check in case package name was not found
                    } else {

                        Toast.makeText(this, getString(R.string.toast_please_install_instagram), Toast.LENGTH_LONG)
                                .show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.toast_not_a_valid_instagram_uri), Toast.LENGTH_LONG)
                            .show();
                }

            }
        }

        Iconify.with(new FontAwesomeModule());

        createNotificationChannel();

        // Obtain the shared Tracker instance.
        InstarepostApplication application = (InstarepostApplication) getApplication();

        verifyStoragePermissions(this);

        drawList();

        if (Build.VERSION.SDK_INT < 29) {

            // start clipboard listener
            CBListenerIntent = new Intent(this, CBWatcherService.class);
            startService(CBListenerIntent);

        }

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

    }

    @Override
    protected void onDestroy() {

        // kill clipboard listener if options is off stopService().
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean persistentCBService = sharedPref.getBoolean("pref_persistent_cb_listener", true);
        if (!persistentCBService) {
            stopService(CBListenerIntent);
        }

        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private void showHelpCard(boolean show) {
        LinearLayout emptyCard = ((LinearLayout) findViewById(R.id.rate_card_empty_list));
        SwipeRefreshLayout refreshLayout = ((SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout));

        if (show) {
            emptyCard.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
        } else {
            emptyCard.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.VISIBLE);
        }
    }

    public void loadNextData(int page) {
        final List<Post> morePosts = Post.createPostsList(10, page);
        rvAdapter.addAll(morePosts);

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                int curSize = rvAdapter.getItemCount();
                rvAdapter.notifyItemRangeInserted(curSize, morePosts.size() - 1);
            }
        };
        handler.post(r);
    }

    private void drawList() {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        AppRated = sharedPref.getBoolean(ratePref, false);

        RecyclerView rvItems = (RecyclerView) findViewById(R.id.rvPosts);
        rvItems.setHasFixedSize(true);

        rvAdapter = new PostAdapter(this);

        // load init
        loadNextData(0);

        if (rvAdapter.getItemCount() > 3 && !AppRated) {
            rateCard();
        }

        if (rvAdapter.getItemCount() < 1) {
            showHelpCard(true);

        } else {

            showHelpCard(false);

            rvItems.setAdapter(rvAdapter);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            rvItems.setLayoutManager(linearLayoutManager);

            rvAdapter.notifyDataSetChanged();

            final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    drawList();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });

            rvItems.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    // dont load more when refreshing
                    if (!swipeRefreshLayout.isRefreshing()) {
                        loadNextData(page);
                    } else {
                        Log.v(TAG, "refreshing!!");
                    }

                }
            });

            ItemClickSupport.addTo(rvItems)
                    .setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {

                            final int position2 = position;

                            DialogInterface.OnClickListener dialogClickListener
                                    = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            //Yes button clicked
                                            // Clicking on items
                                            Post post = rvAdapter.getItem(position2);
                                            // delete post.filePath
                                            File file = new File(filePath + post.getImageFile());
                                            if (!file.delete()) {
                                                Log.e(TAG, "File Delete failed. " + filePath + post.getImageFile());
                                            }
                                            rvAdapter.removeAt(position2);
                                            rvAdapter.notifyItemRemoved(position2);

                                            if (rvAdapter.getItemCount() < 1) {
                                                showHelpCard(true);
                                            }

                                            post.delete();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                            builder.setMessage(getString(R.string.dialog_are_you_sure_you_want_to_delete))
                                    .setPositiveButton(getString(R.string.dialog_yes), dialogClickListener)
                                    .setNegativeButton(getString(R.string.dialog_no), dialogClickListener).show();
                            return true;
                        }
                    });
        }


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("EasyRepost", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void presentHeadsUpNotification(Context context, int icon, String filepath, Post post) {

        // set new id if seperate intent is desired
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean multipleRepostIntents = sharedPref.getBoolean("pref_multiple_repost_intent", true);
        int notification_id = multipleRepostIntents ? getID() : 0;

        try {
            JSONObject postMetaJSON = new JSONObject(post.getJsonMeta());

            // JSON
            String caption = Parser.getCaption(postMetaJSON);
            //String profile_pic_url = "";
            String username = Parser.getUsername(postMetaJSON);
            boolean isVideo = Parser.isVideo(postMetaJSON);

            try {

                Intent repostIntent = new Intent(context, RepostReceiver.class);
                Intent shareIntent = new Intent(context, ShareReceiver.class);

                repostIntent.putExtra("filepath", filepath);
                repostIntent.putExtra("notificationId", notification_id);
                repostIntent.putExtra("postId", post.getId());

                shareIntent.putExtra("filepath", filepath);
                shareIntent.putExtra("notificationId", notification_id);
                shareIntent.putExtra("postId", post.getId());

                PendingIntent pendingIntentRepost = PendingIntent
                        .getBroadcast(context, notification_id, repostIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                PendingIntent pendingIntentShare = PendingIntent
                        .getBroadcast(context, notification_id, shareIntent, PendingIntent.FLAG_CANCEL_CURRENT);

                //PendingIntent contentIntent = PendingIntent.getActivity(this, 0, share, 0);

                if (!isVideo) {
                    // Create the URI from the media
                    final File myImageFile = new File(filepath);
                    Bitmap bitmap_image = BitmapFactory.decodeFile(myImageFile.getAbsolutePath());

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                            "EasyRepost");
                    notificationBuilder.setAutoCancel(true)
                            .setContentTitle(username)
                            .setContentText(caption)
                            .setSmallIcon(icon)
                            .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap_image))
                            .setAutoCancel(true)
                            .addAction(android.R.drawable.ic_menu_share, context.getString(R.string.view_details),
                                    pendingIntentRepost)
                            .addAction(android.R.drawable.ic_menu_share, context.getString(R.string.heads_up_share),
                                    pendingIntentShare)
                            .setContentIntent(pendingIntentRepost)
                            .setVibrate(new long[]{100, 0, 100})

                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setPriority(Notification.PRIORITY_MAX);

                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notification_id, notificationBuilder.build());


                } else {

                    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,
                            "EasyRepost");
                    notificationBuilder.setAutoCancel(true)
                            .setContentTitle(username)
                            .setContentText(caption)
                            .setSmallIcon(icon)
                            .setAutoCancel(true)
                            .addAction(android.R.drawable.ic_menu_share, context.getString(R.string.view_details),
                                    pendingIntentRepost)
                            .addAction(android.R.drawable.ic_menu_share, context.getString(R.string.heads_up_share),
                                    pendingIntentShare)
                            .setContentIntent(pendingIntentRepost)
                            .setVibrate(new long[]{100, 0, 100})
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setWhen(System.currentTimeMillis())
                            .setPriority(Notification.PRIORITY_MAX);

                    NotificationManager notificationManager = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notification_id, notificationBuilder.build());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // rating crap

    CardView rateCard;

    TextView questionTextView;

    Button leftButton;

    Button rightButton;

    int state;

    private void rateCard() {

        rateCard = ((CardView) findViewById(R.id.rate_card_view));

        state = 1;

        questionTextView = ((TextView) findViewById(R.id.question));
        leftButton = ((Button) findViewById(R.id.left_button));
        rightButton = ((Button) findViewById(R.id.right_button));

        setRateTexts();

        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == 1) {
                    // Not really

                    state = 3;
                    setRateTexts();
                } else if (state == 2) {
                    // No, thanks

                    dismisRate();
                } else if (state == 3) {
                    // No, thanks

                    dismisRate();
                }
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == 1) {
                    // Yes!

                    state = 2;
                    setRateTexts();
                } else if (state == 2) {
                    // Ok, sure

                    //Log.v(TAG, "Rate in App store");

                    dismisRate();

                    Uri uri = Uri.parse("market://details?id=" + getPackageName());
                    Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                    // To count with Play market backstack, After pressing back button,
                    // to taken back to our application, we need to add following flags to intent.
                    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                            Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                            Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                    try {
                        startActivity(goToMarket);
                    } catch (ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                    }


                } else if (state == 3) {
                    // Ok, sure
                    //Log.v(TAG, "Give Feedback");

                    dismisRate();

                    Intent Email = new Intent(Intent.ACTION_SEND);
                    Email.setType("text/email");
                    Email.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.feedback_email_address)});
                    Email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                    startActivity(Intent.createChooser(Email, getString(R.string.feedback_chooser)));
                }
            }
        });

        rateCard.setVisibility(View.VISIBLE);
    }


    private void setRateTexts() {
        if (state == 1) {
            questionTextView.setText(R.string.rate_card_state1_question);
            leftButton.setText(R.string.rate_card_btn_not_really);
            rightButton.setText(R.string.rate_card_btn_yes);
        } else if (state == 2) {
            questionTextView.setText(R.string.rate_card_state2_question);
            leftButton.setText(R.string.rate_card_btn_no_thanks);
            rightButton.setText(R.string.rate_card_btn_ok_sure);
        } else if (state == 3) {
            questionTextView.setText(R.string.rate_card_state3_question);
            leftButton.setText(R.string.rate_card_btn_no_thanks);
            rightButton.setText(R.string.rate_card_btn_ok_sure);
        }
    }

    private void dismisRate() {
        //Log.v(TAG, "Dismiss, dont show card again");

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(ratePref, true);
        editor.apply();

        rateCard.setVisibility(View.GONE);
    }

    private static int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.icon_silhouette : R.mipmap.ic_launcher;
    }
}

