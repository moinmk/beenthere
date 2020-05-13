package com.example.beenthere.getstarted;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.beenthere.R;


//======custom class to make sliding effect
public class SliderAdapter extends PagerAdapter {


    Context context;
    LayoutInflater layoutInflater;
    TextView heading,subtext;
    ImageView image;

    public SliderAdapter(Context context){
        this.context=context;
    }


    public int[] images={R.drawable.getstarted__search_place,R.drawable.getstarted__chat,R.drawable.getstarted__add_location};
    public String[] headings={"search any place","chat with friend","add place as visited"};
    public String[] subtexts={"search for a place you are interested in and we will  show the list of your friends who have been there.",
            "get in touch with friends by chat to ask for suggestions or opinions about any place they have visited.",
            "mark a place as visited so your friends can see that you visited that places and can ask for your opinions."
    };
    @Override
    public int getCount() {

        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(RelativeLayout)object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        layoutInflater=(LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view=layoutInflater.inflate(R.layout.getstarted_layout,container,false);
        heading=view.findViewById(R.id.heading);
        subtext=view.findViewById(R.id.subtext);
        image=view.findViewById(R.id.image);

        image.setImageResource(images[position]);
        heading.setText(headings[position]);
        subtext.setText(subtexts[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
