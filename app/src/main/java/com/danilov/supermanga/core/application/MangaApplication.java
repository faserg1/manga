package com.danilov.supermanga.core.application;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.danilov.supermanga.ApplicationComponent;
import com.danilov.supermanga.ApplicationModule;
import com.danilov.supermanga.DaggerApplicationComponent;
import com.danilov.supermanga.R;
import com.danilov.supermanga.core.database.HistoryDAO;
import com.danilov.supermanga.core.database.MangaDAO;
import com.danilov.supermanga.core.database.UpdatesDAO;
import com.danilov.supermanga.core.receiver.AlarmReceiver;
import com.danilov.supermanga.core.repository.RepositoryHolder;
import com.danilov.supermanga.core.repository.special.JSCrud;
import com.danilov.supermanga.core.util.ServiceContainer;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Semyon Danilov on 02.08.2014.
 */
@ReportsCrashes(formKey = "", // will not be used
        mailTo = "senya.danilov@gmail.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class MangaApplication extends Application {


    @SuppressWarnings("NullableProblems")
    // Initialized in onCreate. But be careful if you have ContentProviders
    // -> their onCreate may be called before app.onCreate()
    // -> move initialization to attachBaseContext().
    @NonNull
    private ApplicationComponent applicationComponent;

    public static Context context;

    @NonNull
    // Prevent need in a singleton (global) reference to the application object.
    public static MangaApplication get() {
        return (MangaApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        applicationComponent = prepareApplicationComponent().build();
        applicationComponent.inject(this);

        HistoryDAO historyDAO = new HistoryDAO();
        UpdatesDAO updatesDAO = new UpdatesDAO();
        MangaDAO mangaDAO = new MangaDAO();
        JSCrud jsCrud = new JSCrud();
        ServiceContainer.addService(historyDAO);
        ServiceContainer.addService(updatesDAO);
        ServiceContainer.addService(mangaDAO);
        ServiceContainer.addService(jsCrud);

        RepositoryHolder repositoryHolder = new RepositoryHolder();
        repositoryHolder.init();
        ServiceContainer.addService(repositoryHolder);

//        AccountManager accountManager = AccountManager.get(this);
//        Account[] accounts = accountManager.getAccounts();
//        int length = accounts.length;

        AlarmReceiver.setUpdateAlarm(this);

        //Google Play Service ты офигел!
        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore) {
        }

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            StringWriter errors = new StringWriter();
            ex.printStackTrace(new PrintWriter(errors));
            Toast.makeText(context, errors.toString(), Toast.LENGTH_LONG).show();

            File f = Environment.getExternalStorageDirectory();
            File logFile = new File(f, "mangaerror.log");
            try {
                FileWriter writer = new FileWriter(logFile);
                ex.printStackTrace(new PrintWriter(writer));
            } catch (IOException e) {

            }

            Log.e("MangaFAIL", ex.getMessage());
            ex.printStackTrace();
            defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
        });
    }

    @NonNull
    protected DaggerApplicationComponent.Builder prepareApplicationComponent() {
        return DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this));
    }

    @NonNull
    public ApplicationComponent applicationComponent() {
        return applicationComponent;
    }

    public static Context getContext() {
        return context;
    }

}