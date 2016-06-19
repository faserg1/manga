package com.danilov.supermanga.core.strategy;

import android.app.Fragment;
import android.os.Bundle;

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