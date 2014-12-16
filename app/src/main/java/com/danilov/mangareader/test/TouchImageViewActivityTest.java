package com.danilov.mangareader.test;

import android.app.Activity;
import android.os.Bundle;
import com.danilov.mangareader.R;
import com.danilov.mangareader.core.view.SubsamplingScaleImageView;

/**
 * Created by Semyon Danilov on 16.05.2014.
 */
public class TouchImageViewActivityTest extends Activity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_touch_imageview_activity);
        SubsamplingScaleImageView subsamplingScaleImageView = (SubsamplingScaleImageView) findViewById(R.id.view);
    }
}
