package com.rosetta.sample.tweeter1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import twitter4j.Tweet;
import twitter4j.TwitterException;
import android.app.ListActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class TwitteringListActivity extends ListActivity {

    private static final String TAG = "TwitteringListActivity";
    private TextView tweets;

    private List<Map<String, Object>> listData;

    /**
     * Called when the activity is first created.
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);
        this.tweets = (TextView) findViewById(R.id.tweetsDisplay);

        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put(TAG, "Susan");
        listData = new ArrayList<Map<String,Object>>();
        listData.add(map1);

        SimpleAdapter adapter = new SimpleAdapter(this, listData, R.layout.tweet_widget,
                new String[] { "name", "when", /* "icon" */ }, // from
                new int[] { R.id.tweetering, R.id.tweetTime, /* R.id.userIcon */ }  // to

        );
        setListAdapter(adapter);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();

        TwitterServiceImpl ts = new TwitterServiceImpl();
        String queryString = "from:untiedt OR from:Tom_Adamski";
        try {
            List<Tweet> list = ts.search(queryString, 3);
            StringBuilder buff = new StringBuilder();
            for (Tweet tweet : list) {
                buff.append(tweet.getFromUser())
                    .append(" ")
                    .append(tweet.getText())
                    .append("\n");

                // listview
                Map<String, Object> map1 = new HashMap<String, Object>();
                map1.put("name", tweet.getText());
                map1.put("when", tweet.getCreatedAt().toString());

//                URL url = new URL(tweet.getProfileImageUrl());
//                URLConnection http = url.openConnection();
//                InputStream is = http.getInputStream();
//                Bitmap image = BitmapFactory.decodeStream(is);
//                ImageView imageView = new ImageView(this);
//                imageView.setImageBitmap(image);
//                map1.put("icon", imageView);
                listData.add(map1);
            }
            tweets.setText(buff.toString());
            Log.i(TAG, "twittered...");
        } catch (TwitterException e) {
            tweets.setText(e.toString());
//        } catch (MalformedURLException e) {
//            Log.e(TAG, "Image load uri fail", e);
//        } catch (IOException e) {
//            Log.e(TAG, "Image load read fail", e);
        }

    }

}
