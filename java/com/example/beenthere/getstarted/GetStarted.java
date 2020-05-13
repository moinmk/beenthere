package com.example.beenthere.getstarted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.beenthere.AllActivityContainer;
import com.example.beenthere.R;

public class GetStarted extends AppCompatActivity {

    ViewPager mSlideViewPager;
    LinearLayout dotsLayout;
    SliderAdapter sliderAdapter;
    Button skipButton,nextButton,getStartedButton;
    TextView[] dots;
    int currentSlide;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        mSlideViewPager=findViewById(R.id.viewpager);
        dotsLayout=findViewById(R.id.dots);
        skipButton=findViewById(R.id.skipButton);
        nextButton=findViewById(R.id.nextButton);
        getStartedButton=findViewById(R.id.getStartedButton);

        sliderAdapter=new SliderAdapter(this);
        mSlideViewPager.setAdapter(sliderAdapter);
        addDotIndicator(0);
        mSlideViewPager.addOnPageChangeListener(viewListener);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSlideViewPager.setCurrentItem(currentSlide+1);
            }
        });

        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GetStarted.this, AllActivityContainer.class));
            }
        });

        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GetStarted.this, AllActivityContainer.class));
            }
        });

    }

    public void addDotIndicator(int position){
        dots=new TextView[3];
        dotsLayout.removeAllViews();
        for(int i=0;i<dots.length;i++){
            dots[i]=new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(45);
            dots[i].setTextColor(getResources().getColor(R.color.colorText));

            dotsLayout.addView(dots[i]);
        }

        if(dots.length>0){
            dots[position].setTextColor(getResources().getColor(R.color.colorSecondary));
        }
    }


    //===========page change listener=============
    ViewPager.OnPageChangeListener viewListener=new ViewPager.OnPageChangeListener(){

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDotIndicator(position);
            currentSlide=position;
            if (position == 2) {
                nextButton.setEnabled(false);
                nextButton.setVisibility(View.INVISIBLE);
                skipButton.setEnabled(false);
                skipButton.setVisibility(View.INVISIBLE);
                getStartedButton.setEnabled(true);
                getStartedButton.setVisibility(View.VISIBLE);
            }
            else{
                nextButton.setEnabled(true);
                nextButton.setVisibility(View.VISIBLE);
                skipButton.setEnabled(true);
                skipButton.setVisibility(View.VISIBLE);
                getStartedButton.setEnabled(false);
                getStartedButton.setVisibility(View.INVISIBLE);

            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
