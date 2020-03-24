package com.swatch.smartwatch;


import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;


import java.util.ArrayList;
import java.util.List;

public class HumidityActivity extends AppCompatActivity {

    ImageView mAnimationView;

    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_humidity);

        mAnimationView = (ImageView)findViewById(R.id.imageViewDetail);

        final List<ImageView> imageViewList = new ArrayList<ImageView>();


        mAnimationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;
                mAnimationView= imageViewList.get(i);
                if(i==0)
                    mAnimationView.setImageResource(R.drawable.drop_image_10_percent);
                if(i==1)
                    mAnimationView.setImageResource(R.drawable.drop_image_20_percent);
                if(i==2)
                    mAnimationView.setImageResource(R.drawable.drop_image_30_percent);
                if(i==3)
                    mAnimationView.setImageResource(R.drawable.drop_image_40_percent);
                if(i==4){
                    i=0;
                }
            }
        });
    }
}
