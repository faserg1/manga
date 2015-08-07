package com.danilov.mangareaderplus.core.strategy;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Semyon on 07.08.2015.
 */
public class StrategyHolder extends Fragment {

    public static final String NAME = "StrategyHolder";

    private StrategyDelegate strategyDelegate;

    public static StrategyHolder newInstance(final StrategyDelegate strategyDelegate) {
        StrategyHolder strategyHolder = new StrategyHolder();
        strategyHolder.strategyDelegate = strategyDelegate;
        return strategyHolder;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public StrategyHolder() {
    }

    public StrategyDelegate getStrategyDelegate() {
        return strategyDelegate;
    }

}