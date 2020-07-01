package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class DetailsActivity extends AppCompatActivity {

    public static final int MAX_TWEET_LENGTH = 280;

    ImageView ivProfileImage;
    TextView tvScreenName;
    TextView tvBody;
    TextView tvTimeStamp;
    ImageView ivRetweet;
    ImageView ivLike;
    ImageView ivReply;
    TextView tvLikeCount;
    TextView tvRetweetCount;
    EditText etReply;
    Button btnPublishReply;

    Boolean like;
    Boolean retweet;

    Tweet tweet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        setTweetDetails();
    }

    void setTweetDetails() {
        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvScreenName = findViewById(R.id.tvScreenName);
        tvBody = findViewById(R.id.tvBody);
        tvTimeStamp = findViewById(R.id.tvTimeStamp);
        tvLikeCount = findViewById(R.id.tvLikeCount);
        tvRetweetCount = findViewById(R.id.tvRetweetCount);
        etReply = findViewById(R.id.etReply);

        Glide.with(this).load(tweet.user.profileImageUrl).into(ivProfileImage);
        tvBody.setText(tweet.body);
        tvScreenName.setText(tweet.user.screenName);
        tvTimeStamp.setText(ParseRelativeDate.getRelativeTimeAgo(tweet.createdAt));
        tvLikeCount.setText(String.valueOf(tweet.likeCount));
        tvRetweetCount.setText(String.valueOf(tweet.retweetCount));
        btnPublishReply = findViewById(R.id.btnPublishReply);


        like = tweet.like;
        retweet = tweet.retweet;

        // setting listeners
        ivRetweet = findViewById(R.id.ivRetweet);
        ivReply = findViewById(R.id.ivReply);
        ivLike = findViewById(R.id.ivLike);

        ivRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retweetTweet();
            }
        });
        btnPublishReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replyTweet();
            }
        });
        ivLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeTweet();
            }
        });

        etReply.setText("@" + tweet.user.screenName);
    }


    private void replyTweet() {
        TwitterClient client = TwitterApp.getRestClient(this);
        String tweetContent = etReply.getText().toString();

        if (tweetContent.isEmpty()) {
            Toast.makeText(DetailsActivity.this, "Tweet can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tweetContent.length() > MAX_TWEET_LENGTH) {
            Toast.makeText(DetailsActivity.this, "Tweet is too long", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(DetailsActivity.this, tweetContent, Toast.LENGTH_SHORT).show();
        client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i("DetailsActivity", "onSUccess to publish the reply");
                try {
                    Tweet tweet = Tweet.fromJson(json.jsonObject);
                    Log.i("DetailsActivity", "published reply says: " + tweet.body);
                    Intent intent = new Intent();
                    intent.putExtra("tweet", Parcels.wrap(tweet));
                    setResult(RESULT_OK, intent);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e("DetailsActivity", "onFailure to reply to tweet", throwable);
            }
        });


    }

    public void retweetTweet() {
            TwitterClient client = TwitterApp.getRestClient(this);
            client.retweetTweet(retweet, tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    if (retweet == true) {
                        ivRetweet.setImageDrawable(getResources().getDrawable(R.drawable.ic_vector_retweet));
                    } else {
                        ivRetweet.setImageDrawable(getResources().getDrawable(R.drawable.ic_vector_retweet_stroke));
                    }
                    retweet = !retweet;
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e("DetailsActivity", "this retweet aint working", throwable);
                }
            });
        }


        public void likeTweet() {
            TwitterClient client = TwitterApp.getRestClient(this);
            client.likeTweet(like, tweet.id, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Headers headers, JSON json) {
                    if (like == true) {
                        ivLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_vector_heart_stroke));
                        Toast.makeText(getBaseContext(), "liked", Toast.LENGTH_SHORT).show();
                    } else {
                        ivLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_vector_heart));
                        Toast.makeText(getBaseContext(), "unliked", Toast.LENGTH_SHORT).show();
                    }
                    like = !like;
                }

                @Override
                public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                    Log.e("DetailsActivity", "this like aint working", throwable);

                }
            });
        }

    }




