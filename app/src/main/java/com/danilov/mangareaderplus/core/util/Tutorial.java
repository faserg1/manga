package com.danilov.mangareaderplus.core.util;

/**
 * Created by Semyon on 26.11.2014.
 */
public class Tutorial {

    private Promise<Void> closePromise;

    public Tutorial show() {
        System.out.println(this);
        return this;
    }

    public Promise<Tutorial> onClose(final Promise.Action<Void, Tutorial> onClose) {
        return closePromise.then(onClose);
    }

    public static void main(String[] args) {
        Tutorial t = new Tutorial();
        t.show().onClose(new Promise.Action<Void, Tutorial>() {
            @Override
            public Tutorial action(final Void data, final boolean success) {
                return new Tutorial();
            }
        }).then(new Promise.Action<Tutorial, Void>() {
            @Override
            public Void action(final Tutorial data, final boolean success) {
                data.show();
                return null;
            }
        });
    }

}
