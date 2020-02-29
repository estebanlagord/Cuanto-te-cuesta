package com.smartpocket.cuantoteroban;

import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class HelpActivity extends Fragment {
    private static final String[] TAB_TITLES = new String[]{"Introducción", "Oficial", "Turista", "Blue", "Casa de cambio", "Mis Monedas"};
    private static final String[] PAGE_TITLES = new String[]{"Pantalla principal", "Cotización oficial", "Cotización turista", "Cotización blue", "Cotización en casa de cambio", "Mis Monedas"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_help2, container, false);
        Toolbar toolbar = view.findViewById(R.id.my_awesome_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setTitle("Ayuda");

        MyAdapter myAdapter = new MyAdapter(getChildFragmentManager(), requireContext());
        ViewPager mPager = view.findViewById(R.id.pager);
        mPager.setAdapter(myAdapter);
    }


    static class MyAdapter extends FragmentPagerAdapter {
        Context context;

        MyAdapter(FragmentManager fm, Context context) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.context = context;
        }

        @Override
        public int getCount() {
            return HelpActivity.TAB_TITLES.length;
        }

        @NotNull
        @Override
        public Fragment getItem(int i) {
            CharSequence content = "";

            switch (i) {
                case 0:
                    content = context.getResources().getText(R.string.mainHelp);
                    break;
                case 1:
                    content = context.getResources().getText(R.string.pesosHelp);
                    break;
//            case 2:
//                content = context.getResources().getText(R.string.savingsHelp);
//                break;
                case 2:
                    content = context.getResources().getText(R.string.creditCardHelp);
                    break;
                case 3:
                    content = context.getResources().getText(R.string.blueHelp);
                    break;
                case 4:
                    content = context.getResources().getText(R.string.agencyHelp);
                    break;
//			case 5:
//				content = context.getResources().getText(R.string.payPalHelp);
//				break;
                case 5:
                    content = context.getResources().getText(R.string.chooseCurrencyHelp);
                    break;
                default:
                    break;
            }

            return HelpTabFragment.newInstance(content);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return HelpActivity.TAB_TITLES[position].toUpperCase(Locale.US);
        }

        public static class HelpTabFragment extends Fragment {
            private static final String KEY_CONTENT = "Fragment:Content";
            private CharSequence mContent = "";

            static HelpTabFragment newInstance(CharSequence content) {
                HelpTabFragment fragment = new HelpTabFragment();
                fragment.mContent = content;
                return fragment;
            }

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
                    mContent = savedInstanceState.getCharSequence(KEY_CONTENT);
                }
            }

            @Override
            public void onSaveInstanceState(@NotNull Bundle outState) {
                super.onSaveInstanceState(outState);
                outState.putCharSequence(KEY_CONTENT, mContent);
            }

            @Override
            public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                // The last two arguments ensure LayoutParams are inflated properly.
                View rootView = inflater.inflate(R.layout.help_fragment_collection_object, container, false);

                TextView text = new TextView(getActivity());
                text.setText(mContent);
                text.setTextAppearance(getActivity(), R.style.Base_TextAppearance_AppCompat_Subhead);
                int padding = getResources().getDimensionPixelSize(R.dimen.padding_large);
                text.setPadding(padding, padding, padding, padding);
                text.setMovementMethod(LinkMovementMethod.getInstance());

                ScrollView scrollView = rootView.findViewById(R.id.helpScrollView);
                scrollView.addView(text);

                return rootView;
            }

        }
    }
}
