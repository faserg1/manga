package com.danilov.supermanga.core.onlinestorage;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Semyon on 30.08.2015.
 */
public class GoogleDriveConnector extends OnlineStorageConnector implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected static Executor executor = Executors.newFixedThreadPool(3);

    private GoogleApiClient googleApiClient;
    private boolean isClientAvailable = false;
    private String accountName = "Account name not available";


    public GoogleDriveConnector(final StorageConnectorListener connectorListener) {
        super(connectorListener);
    }

    @Override
    public void init() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Drive.API)
                .addApi(Plus.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
    }

    @Override
    public String getAccountName() {
        return accountName;
    }

    private class FileCreateCallback implements ResultCallback<DriveFolder.DriveFileResult> {

        private CommandCallback<Void> commandCallback;

        private FileCreateCallback(final CommandCallback<Void> commandCallback) {
            this.commandCallback = commandCallback;
        }

        @Override
        public void onResult(DriveFolder.DriveFileResult result) {
            if (!result.getStatus().isSuccess()) {
                commandCallback.onCommandError("Error while trying to create the file");
                return;
            }
            commandCallback.onCommandSuccess(null);
        }
    }

    private class FileCreationRequestCallback implements ResultCallback<DriveApi.DriveContentsResult> {

        private CommandCallback<Void> commandCallback;
        private String fileTitle;
        private String text;
        private MimeType mimeType;
        private File file;

        public FileCreationRequestCallback(final String fileTitle, final File file, final MimeType mimeType, final CommandCallback<Void> commandCallback) {
            this.commandCallback = commandCallback;
            this.fileTitle = fileTitle;
            this.file = file;
            this.mimeType = mimeType;
        }

        public FileCreationRequestCallback(final String fileTitle, final String text, final MimeType mimeType, final CommandCallback<Void> commandCallback) {
            this.commandCallback = commandCallback;
            this.fileTitle = fileTitle;
            this.text = text;
            this.mimeType = mimeType;
        }

        @Override
        public void onResult(final DriveApi.DriveContentsResult result) {
            if (!result.getStatus().isSuccess()) {
                commandCallback.onCommandError("Error while trying to create new file contents");
                return;
            }
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final DriveContents driveContents = result.getDriveContents();
                    OutputStream outputStream = driveContents.getOutputStream();
                    Writer writer = new OutputStreamWriter(outputStream);
                    Reader inputStreamReader = null;
                    if (file != null) {
                        try {
                            inputStreamReader = new BufferedReader(new FileReader(file));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        inputStreamReader = new StringReader(text);
                    }
                    if (inputStreamReader == null) {
                        return;
                    }
                    try {
                        char[] buffer = new char[512];
                        int bytesRead;
                        while((bytesRead = inputStreamReader.read(buffer)) != -1) {
                            writer.write(buffer, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(fileTitle)
                            .setMimeType(mimeType.getData())
                            .build();
                    Drive.DriveApi.getAppFolder(googleApiClient)
                            .createFile(googleApiClient, changeSet, result.getDriveContents())
                            .setResultCallback(new FileCreateCallback(commandCallback));
                }
            });
        }

    }

    @Override
    public void createFile(final String title, final File file, final MimeType mimeType, final CommandCallback<Void> commandCallback) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new FileCreationRequestCallback(title, file, mimeType, commandCallback));
    }

    @Override
    public void createFile(final String title, final String text, final MimeType mimeType, final CommandCallback<Void> commandCallback) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new FileCreationRequestCallback(title, text, mimeType, commandCallback));
    }

    private class QueryFileCallback implements ResultCallback<DriveApi.MetadataBufferResult> {

        private CommandCallback<OnlineFile> commandCallback;

        private QueryFileCallback(final CommandCallback<OnlineFile> commandCallback) {
            this.commandCallback = commandCallback;
        }

        @Override
        public void onResult(DriveApi.MetadataBufferResult result) {
            if (!result.getStatus().isSuccess()) {
                commandCallback.onCommandError("Error while checking for file");
                return;
            }
            MetadataBuffer metadataBuffer = result.getMetadataBuffer();
            if (metadataBuffer.getCount() < 1) {
                commandCallback.onCommandSuccess(null);
                return;
            }
            Metadata metadata = metadataBuffer.get(0);
            DriveFile file = Drive.DriveApi.getFile(googleApiClient, metadata.getDriveId());
            GoogleOnlineFile googleOnlineFile = new GoogleOnlineFile(file);
            commandCallback.onCommandSuccess(googleOnlineFile);
        }
    }

    @Override
    public void getExistingFile(final String title, final CommandCallback<OnlineFile> commandCallback) {
        Query query = new Query.Builder()
                .addFilter(Filters.contains(SearchableField.TITLE, title))
                .build();
        Drive.DriveApi.query(googleApiClient, query)
                .setResultCallback(new QueryFileCallback(commandCallback));
    }

    @Override
    public void onConnected(final Bundle bundle) {
        accountName = Plus.AccountApi.getAccountName(googleApiClient);
        Person currentPerson = Plus.PeopleApi.getCurrentPerson(googleApiClient);
        if (currentPerson != null) {
            accountName = currentPerson.getDisplayName();
        }
        isClientAvailable = true;
        connectorListener.onStorageConnected(this);
    }

    @Override
    public void onConnectionSuspended(final int i) {
        isClientAvailable = false;
        connectorListener.onStorageDisconnected(this);
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        isClientAvailable = false;
        connectorListener.onConnectionFailed(this, connectionResult);
    }

    private class GoogleOnlineFile implements OnlineFile {

        private DriveFile driveFile;

        private GoogleOnlineFile(final DriveFile driveFile) {
            this.driveFile = driveFile;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void saveOnDisk(final File file) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Reader inputStreamReader = null;
                    if (file != null) {
                        try {
                            inputStreamReader = new BufferedReader(new FileReader(file));
                            saveInternal(inputStreamReader);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public void saveOnDisk(final String contents) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Reader inputStreamReader = new StringReader(contents);
                    saveInternal(inputStreamReader);
                }
            });
        }

        private boolean saveInternal(final Reader inputStreamReader) {
            DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_READ_WRITE, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }
            ParcelFileDescriptor parcelFileDescriptor = driveContentsResult.getDriveContents().getParcelFileDescriptor();
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor);
            try {
                fileOutputStream.getChannel().truncate(0);
                Writer writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream));
                char[] buffer = new char[512];
                int bytesRead;
                while((bytesRead = inputStreamReader.read(buffer)) != -1) {
                    writer.write(buffer, 0, bytesRead);
                }
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

    }

}
