package com.danilov.supermanga.core.repository.special.test;

import android.content.Context;

import com.danilov.supermanga.core.repository.special.JavaScriptEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by Semyon on 21.01.2016.
 */
public class JSTestEngine extends JavaScriptEngine {

    private Context context;

    public JSTestEngine(final Context context, final String name, final String filePath) {
        super(name, filePath);
        this.context = context;
    }


    @Override
    public Reader getScript() {
        try {
            return new BufferedReader(new InputStreamReader(context.getAssets().open("testjs/mangafox.js")));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getLanguage() {
        return null;
    }

    @Override
    public boolean requiresAuth() {
        return false;
    }
}
