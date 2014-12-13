package com.smartpocket.cuantoteroban;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

public final class HelpFragment extends Fragment {
    private static final String KEY_CONTENT = "TestFragment:Content";
    private CharSequence mContent = "???";
    
    public static HelpFragment newInstance(CharSequence content) {
        HelpFragment fragment = new HelpFragment();
        fragment.mContent = content;
        return fragment;
    }
    

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
    }

    @SuppressWarnings("deprecation")
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView text = new TextView(getActivity());
        //text.setGravity(Gravity.CENTER);
        text.setText(mContent);
        //text.setTextSize(15 * getResources().getDisplayMetrics().density);
        text.setTextSize(15);
        text.setTextColor(getResources().getColor(R.color.black));
        text.setBackgroundColor(getResources().getColor(R.color.lightBlue));
        text.setPadding(10, 20, 10, 10);
        //text.setTypeface(MainActivity.TYPEFACE_QARMIC);

        // make links clickable
        text.setMovementMethod(LinkMovementMethod.getInstance());

        
        ScrollView layout = new ScrollView(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        layout.setBackgroundColor(getResources().getColor(R.color.lightBlue));
        layout.addView(text);

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent.toString());
    }
}
