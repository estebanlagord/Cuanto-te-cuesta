package com.smartpocket.cuantoteroban;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.smartpocket.cuantoteroban.databinding.ActivityHelp2Binding;
import com.smartpocket.cuantoteroban.databinding.HelpFragmentCollectionObjectBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class HelpActivity extends Fragment {
    private static final String[] TAB_TITLES = new String[]{"Introducción", "Oficial", "Turista", "Blue", "Casa de cambio", "Mis Monedas"};
    private static final String[] PAGE_TITLES = new String[]{"Pantalla principal", "Cotización oficial", "Cotización turista", "Cotización blue", "Cotización en casa de cambio", "Mis Monedas"};
    private ActivityHelp2Binding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ActivityHelp2Binding.inflate(inflater);
        Toolbar toolbar = binding.getRoot().findViewById(R.id.my_awesome_toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        actionBar.setTitle("Ayuda");

        MyAdapter myAdapter = new MyAdapter(getParentFragmentManager(), requireContext());
        binding.pager.setAdapter(myAdapter);
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
            CharSequence description = "";

            switch (i) {
                case 0:
                    description = context.getResources().getText(R.string.mainHelp);
                    break;
                case 1:
                    description = context.getResources().getText(R.string.pesosHelp);
                    break;
//            case 2:
//                description = context.getResources().getText(R.string.savingsHelp);
//                break;
                case 2:
                    description = context.getResources().getText(R.string.creditCardHelp);
                    break;
                case 3:
                    description = context.getResources().getText(R.string.blueHelp);
                    break;
                case 4:
                    description = context.getResources().getText(R.string.agencyHelp);
                    break;
//			case 5:
//				description = context.getResources().getText(R.string.payPalHelp);
//				break;
                case 5:
                    description = context.getResources().getText(R.string.chooseCurrencyHelp);
                    break;
                default:
                    break;
            }
            String title = PAGE_TITLES[i];

            return HelpTabFragment.newInstance(title, description);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return HelpActivity.TAB_TITLES[position].toUpperCase(Locale.US);
        }

        public static class HelpTabFragment extends Fragment {
            private static final String KEY_TITLE = "Fragment:Title";
            private static final String KEY_CONTENT = "Fragment:Content";
            private CharSequence title = "";
            private CharSequence description = "";
            private HelpFragmentCollectionObjectBinding binding;

            static HelpTabFragment newInstance(CharSequence title, CharSequence content) {
                HelpTabFragment fragment = new HelpTabFragment();
                fragment.title = title;
                fragment.description = content;
                return fragment;
            }

            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
                    title = savedInstanceState.getCharSequence(KEY_TITLE);
                    description = savedInstanceState.getCharSequence(KEY_CONTENT);
                }
            }

            @Override
            public void onSaveInstanceState(@NotNull Bundle outState) {
                super.onSaveInstanceState(outState);
                outState.putCharSequence(KEY_TITLE, title);
                outState.putCharSequence(KEY_CONTENT, description);
            }

            @Override
            public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                binding = HelpFragmentCollectionObjectBinding.inflate(inflater, container, false);
                return binding.getRoot();
            }

            @Override
            public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
                binding.title.setText(title);
                binding.description.setText(description);
            }

            @Override
            public void onDestroyView() {
                super.onDestroyView();
                binding = null;
            }
        }
    }
}
