package com.example.android.popmovies;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.example.android.popmovies.adapter.ViewPagerAdapter;
import com.example.android.popmovies.fragments.OverviewFragment;
import com.example.android.popmovies.fragments.ReviewFragment;
import com.example.android.popmovies.fragments.TrailerFragment;

public class DetailActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OverviewFragment(), getResources().getString(R.string.overview_fragment_title));
        adapter.addFragment(new TrailerFragment(), getResources().getString(R.string.trailers_fragment_title));
        adapter.addFragment(new ReviewFragment(), getResources().getString(R.string.review_fragment_title));
        viewPager.setAdapter(adapter);
    }
}
