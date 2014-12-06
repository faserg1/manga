package com.danilov.manga.activity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;

import com.danilov.manga.R;
import com.danilov.manga.core.util.Constants;
import com.danilov.manga.fragment.DownloadManagerFragment;
import com.danilov.manga.fragment.RepositoryPickerFragment;

/**
 * Created by Semyon on 15.11.2014.
 */
public class SingleFragmentActivity extends BaseToolbarActivity {

    public static final int DOWNLOAD_MANAGER_FRAGMENT = 0;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int fragmentId = getIntent().getIntExtra(Constants.FRAGMENTS_KEY, 0);
        setContentView(R.layout.manga_singlefragment_activity);

        Fragment fragment = null;

        switch (fragmentId) {
            case DOWNLOAD_MANAGER_FRAGMENT:
                fragment = DownloadManagerFragment.newInstance();
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

    }

}
