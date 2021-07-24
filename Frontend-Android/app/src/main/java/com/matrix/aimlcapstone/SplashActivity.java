package com.matrix.aimlcapstone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

public class SplashActivity extends AppCompatActivity {
    ImageView gif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        gif = findViewById(R.id.gif);

//        Glide.with(this).asGif()
//                .load("https://media.giphy.com/media/EpTuO1ZrLHzHi/giphy.gif")
//                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
//                                .into(gif);

//        Glide.with(this).asGif().load("https://media.giphy.com/media/EpTuO1ZrLHzHi/giphy.gif").into(gif);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(SplashActivity.this,TutorialsActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, 4500);

    }
}
