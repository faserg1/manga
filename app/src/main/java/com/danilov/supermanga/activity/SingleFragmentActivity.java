package com.danilov.supermanga.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;

import com.danilov.supermanga.R;
import com.danilov.supermanga.core.util.Constants;
import com.danilov.supermanga.fragment.DownloadManagerFragment;

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

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

    }

}
