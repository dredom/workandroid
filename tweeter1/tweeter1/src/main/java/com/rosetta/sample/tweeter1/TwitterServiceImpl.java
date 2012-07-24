/**
 *
 */
package com.rosetta.sample.tweeter1;

import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.util.Log;

/**
 * @author auntiedt
 *
 */
public class TwitterServiceImpl {
    private static final String TAG = "TwitterServiceImpl";

    private Twitter twitterInstance;

    public void publish(){
        Log.i(TAG, "Starting");
       String message="Twitter application using Java http://www.java-tutorial.ch/architecture/twitter-with-java-tutorial";
       String configTreePath = "/";
       try {
           Twitter twitter = new TwitterFactory(configTreePath).getInstance();
           try {
               RequestToken requestToken = twitter.getOAuthRequestToken();
               AccessToken accessToken = null;
               while (null == accessToken) {
                   Log.i(TAG, "Open the following URL and grant access to your account:");
                   Log.i(TAG,requestToken.getAuthorizationURL());
                   try {
                           accessToken = twitter.getOAuthAccessToken(requestToken);
                   } catch (TwitterException te) {
                       if (401 == te.getStatusCode()) {
                           Log.e(TAG, "Unable to get the access token.");
                       } else {
                           te.printStackTrace();
                       }
                   }
               }
               Log.i(TAG, "Got access token.");
               Log.i(TAG, "Access token: " + accessToken.getToken());
               Log.i(TAG, "Access token secret: " + accessToken.getTokenSecret());
           } catch (IllegalStateException ie) {
                   Log.e(TAG, "fail", ie);
               // access token is already available, or consumer key/secret is not set.
               if (!twitter.getAuthorization().isEnabled()) {
                   Log.e(TAG, "OAuth consumer key/secret is not set.");
                   return;
               }
           }
//           Status status = twitter.updateStatus(message);
//           Log.info("Successfully updated the status to [" + status.getText() + "].");
           String queryString = "from:untiedt OR from:Tom_Adamski";
           Query query = new Query(queryString);
           query.setRpp(2);
           QueryResult result = twitter.search(query);
           List<Tweet> list = result.getTweets();
           for (Tweet tweet : list) {
               Log.i(TAG, tweet.getFromUser() + ": " + tweet.getText());
           }
//           Status status = twitter.showStatus(162616968845852672L);
//           Log.info("Status: " + status.getText());
       } catch (TwitterException te) {
           te.printStackTrace();
           Log.e(TAG, "Failed to get timeline: " + te.getMessage());
       }
    }

    public List<Tweet> search(String queryString, int resultsPerPage) throws TwitterException {
        Query query = new Query(queryString);
        query.setRpp(resultsPerPage);
        QueryResult result = getTwitter().search(query);
        List<Tweet> list = result.getTweets();
        return list;
    }

    public Twitter getTwitter() throws TwitterException {
        if (twitterInstance != null) {
            return twitterInstance;
        }
        Twitter twitter = null;
        try {
            twitter = new TwitterFactory().getInstance();
            try {
                RequestToken requestToken = twitter.getOAuthRequestToken();
                AccessToken accessToken = null;
                while (null == accessToken) {
                    Log.i(TAG, "Open the following URL and grant access to your account:");
                    Log.i(TAG,requestToken.getAuthorizationURL());
                    try {
                        accessToken = twitter.getOAuthAccessToken(requestToken);
                    } catch (TwitterException te) {
                        if (401 == te.getStatusCode()) {
                            Log.e(TAG, "Unable to get the access token.");
                        } else {
                            Log.e(TAG, "OAuth failure", te);
                        }
                        throw te;
                    }
                }
                Log.i(TAG, "Got access token.");
                Log.i(TAG, "Access token: " + accessToken.getToken());
                Log.i(TAG, "Access token secret: " + accessToken.getTokenSecret());
            } catch (IllegalStateException ie) {
                // consumer key/secret is not set.
                if (!twitter.getAuthorization().isEnabled()) {
                    Log.e(TAG, "OAuth consumer key/secret is not set.");
                    throw new TwitterException("OAuth consumer key/secret is not set.", ie);
                } else { // access token is already available - ignore
                    Log.w(TAG, "Access token already available?", ie);
                }
            }
        } catch (Throwable tr) {
            Log.e(TAG, "Failed to get Twitter", tr);
            throw new TwitterException("Failed to get a Twitter", tr);
        }
        this.twitterInstance = twitter;
        return twitter;
    }
}
