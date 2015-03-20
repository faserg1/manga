package com.danilov.mangareaderplus.core.view;

import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import java.util.List;

/**
 * Created by Semyon on 17.03.2015.
 */
public interface Switchable {

    public void setNextImageDrawable(final String filePath);

    public void setPreviousImageDrawable(final String filePath);

    public void setInAndOutAnim(final InAndOutAnim inAndOutAnim);

    public void setFactory(android.widget.ViewSwitcher.ViewFactory factory);

    public void setFragmentManager(final FragmentManager fragmentManager);

    public void setSize(final int size);

    public void setUris(final List<String> uris);

    public void setOnPageChangeListener(final ViewPager.OnPageChangeListener listener);

}
