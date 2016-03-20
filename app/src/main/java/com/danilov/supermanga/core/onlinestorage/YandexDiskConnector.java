package com.danilov.supermanga.core.onlinestorage;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.danilov.supermanga.core.application.MangaApplication;
import com.danilov.supermanga.core.onlinestorage.yandex.Credentials;
import com.danilov.supermanga.core.service.OnlineStorageProfileService;
import com.danilov.supermanga.core.util.Utils;
import com.yandex.disk.rest.DownloadListener;
import com.yandex.disk.rest.ProgressListener;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;
import com.yandex.disk.rest.json.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Semyon on 20.03.2016.
 */
public class YandexDiskConnector extends OnlineStorageConnector {

    private Executor executor = Executors.newSingleThreadExecutor();

    private Credentials credentials = null;
    private RestClient restClient = null;

    public YandexDiskConnector(final StorageConnectorListener connectorListener) {
        super(connectorListener);
    }

    @Override
    public int getSentSuccessCode() {
        return OnlineStorageProfileService.YANDEX_SENT_SUCCESS;
    }

    @Override
    public int getConnectedCode() {
        return OnlineStorageProfileService.YANDEX_CONNECTED;
    }

    @Override
    public int getNeedConfirmationCode() {
        return OnlineStorageProfileService.YANDEX_NEED_CONFIRMATION;
    }

    @Override
    public int getDownloadedCode() {
        return OnlineStorageProfileService.YANDEX_DOWNLOADED;
    }

    @Override
    public void init() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MangaApplication.get());
        String yaUsername = sharedPreferences.getString("YA_USERNAME", "");
        String yaToken = sharedPreferences.getString("YA_TOKEN", "");
        if (TextUtils.isEmpty(yaToken)) {
            connectorListener.onConnectionFailed(this, null);
        } else {
            connectorListener.onStorageConnected(this);
        }
        credentials = new Credentials(yaUsername, yaToken);
        restClient = new RestClient(credentials);
    }

    @Override
    public String getAccountName() {
        return credentials.getUser();
    }

    private String appsPath = null;
    private boolean baseFolderExists = false;
    private boolean appFolderExists = false;
    private static final String APP_PATH = "MangaReader";

    private void validateAppsPath() throws IOException, ServerIOException {
        if (appsPath == null) {
            DiskInfo diskInfo = restClient.getDiskInfo();
            appsPath = diskInfo.getSystemFolders().get("applications");
        }
    }

    private boolean checkAppFolderExists() {
        try {
            validateAppsPath();
        } catch (IOException | ServerIOException e) {
            return false;
        }

        ResourcesArgs baseFolderArgs = new ResourcesArgs.Builder().setPath(appsPath + "/").build();
        Resource baseFolder = null;
        try {
            baseFolder = restClient.getResources(baseFolderArgs);
        } catch (IOException | ServerIOException e) {
            baseFolderExists = false;
            return false;
        }

        ResourcesArgs appFolderArgs = new ResourcesArgs.Builder().setPath(appsPath + "/" + APP_PATH + "/").build();
        try {
            Resource appFolder = restClient.getResources(appFolderArgs);
        } catch (IOException | ServerIOException e) {
            appFolderExists = false;
            return false;
        }

        return true;
    }

    private boolean createAppFolder() {
        try {
            validateAppsPath();
        } catch (IOException | ServerIOException e) {
            return false;
        }
        if (!baseFolderExists) {
            try {
                restClient.makeFolder(appsPath);
            } catch (ServerIOException | IOException e) {
                return false;
            }
        }
        if (!appFolderExists) {
            try {
                restClient.makeFolder(appsPath + "/" + APP_PATH + "/");
            } catch (ServerIOException | IOException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void createFile(final String title, final File file, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
        executor.execute(() -> {
            if (!checkAppFolderExists()) {
                createAppFolder();
            }
            Link link = null;
            try {
                link = restClient.getUploadLink(appsPath + "/" + APP_PATH + "/" + title, true);
            } catch (ServerIOException | WrongMethodException | IOException e) {
                commandCallback.onCommandError(e.getMessage());
                return;
            }
            try {
                restClient.uploadFile(link, false, file, null);
            } catch (IOException | ServerException e) {
                commandCallback.onCommandError(e.getMessage());
                return;
            }
            commandCallback.onCommandSuccess(true);
        });
    }

    @Override
    public void createFile(final String title, final String text, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
        executor.execute(() -> {
            if (!checkAppFolderExists()) {
                createAppFolder();
            }
            Link link = null;
            try {
                link = restClient.getUploadLink(appsPath + "/" + APP_PATH + "/" + title, true);
            } catch (ServerIOException | WrongMethodException | IOException e) {
                commandCallback.onCommandError(e.getMessage());
                return;
            }
            File file = null;
            try {
                file = File.createTempFile("temp_", title);
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(text);
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                commandCallback.onCommandError(e.getMessage());
                return;
            }
            try {
                restClient.uploadFile(link, false, file, null);
            } catch (IOException | ServerException e) {
                commandCallback.onCommandError(e.getMessage());
            } finally {
                file.delete();
            }
            commandCallback.onCommandSuccess(true);
        });
    }

    @Override
    public void getExistingFile(final String title, final CommandCallback<OnlineFile> commandCallback) {
        executor.execute(() -> {
            if (!checkAppFolderExists()) {
                createAppFolder();
            }

            ResourcesArgs fileArgs = new ResourcesArgs.Builder().setPath(appsPath + "/" + APP_PATH + "/" + title).build();
            Resource existingFile;
            try {
                existingFile = restClient.getResources(fileArgs);
            } catch (IOException e) {
                commandCallback.onCommandError(e.getMessage());
                return;
            } catch (ServerIOException e) {
                commandCallback.onCommandSuccess(null);
                return;
            }
            commandCallback.onCommandSuccess(new YandexOnlineFile(existingFile));
        });
    }

    private class YandexOnlineFile implements OnlineFile {

        private Resource file;

        public YandexOnlineFile(final Resource file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void rewriteWith(final File file, final CommandCallback<Boolean> commandCallback) {
            String title = this.file.getName();
            createFile(title, file, null, commandCallback);
        }

        @Override
        public void rewriteWith(final String contents, final CommandCallback<Boolean> commandCallback) {
            String title = this.file.getName();
            createFile(title, contents, null, commandCallback);
        }

        @Override
        public void download(final String path, final CommandCallback<Boolean> commandCallback) {
            try {
                restClient.downloadFile(file.getPath().toString(), new FileDownloadListener(new File(path), null));
            } catch (IOException | ServerException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void download(final CommandCallback<String> commandCallback) {
            File tmp = null;
            try {
                tmp = File.createTempFile("tmpfile", "" + System.currentTimeMillis());
            } catch (IOException e) {
                commandCallback.onCommandError(e.getMessage());
            }
            try {
                restClient.downloadFile(file.getPath().toString(), tmp, null);
                FileReader fileReader = null;
                if (tmp != null) {
                    fileReader = new FileReader(tmp);
                } else {
                    commandCallback.onCommandError("File is empty");
                    return;
                }
                BufferedReader reader = new BufferedReader(fileReader);
                StringBuilder builder = new StringBuilder();
                for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                    builder.append(line);
                }
                commandCallback.onCommandSuccess(builder.toString());
            } catch (IOException | ServerException e) {
                commandCallback.onCommandError(e.getMessage());
            }
        }

    }

    public class FileDownloadListener extends DownloadListener {

        private final File saveTo;
        private final ProgressListener progressListener;

        public FileDownloadListener(File saveTo, ProgressListener progressListener) {
            this.saveTo = saveTo;
            this.progressListener = progressListener;
        }

        @Override
        public OutputStream getOutputStream(boolean append)
                throws FileNotFoundException {
            return new FileOutputStream(saveTo, false);
        }

        @Override
        public long getLocalLength() {
            return 0;
        }

        @Override
        public void updateProgress(long loaded, long total) {
            if (progressListener != null) {
                progressListener.updateProgress(loaded, total);
            }
        }

        @Override
        public boolean hasCancelled() {
            return progressListener != null && progressListener.hasCancelled();
        }
    }

}