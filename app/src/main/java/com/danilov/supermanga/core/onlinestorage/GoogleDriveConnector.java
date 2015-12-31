package com.danilov.supermanga.core.onlinestorage;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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

    private class FileCreationRequestCallback implements ResultCallback<DriveApi.DriveContentsResult> {

        private CommandCallback<Boolean> commandCallback;
        private String fileTitle;
        private String text;
        private MimeType mimeType;
        private File file;

        public FileCreationRequestCallback(final String fileTitle, final File file, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
            this.commandCallback = commandCallback;
            this.fileTitle = fileTitle;
            this.file = file;
            this.mimeType = mimeType;
        }

        public FileCreationRequestCallback(final String fileTitle, final String text, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
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
                    FileOutputStream fileOutputStream = new FileOutputStream(driveContents.getParcelFileDescriptor().getFileDescriptor());

                    InputStream inputStream = null;

                    if (file != null) {
                        try {
                            inputStream = new FileInputStream(file);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        inputStream = new ByteArrayInputStream(text.getBytes());
                    }
                    if (inputStream == null) {
                        return;
                    }
                    try {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        int totalBytes = 0;

                        while((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                            fileOutputStream.flush();
                            totalBytes += bytesRead;
                        }
                        Log.e("GOOGLE", "Bytes written: " + totalBytes);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                            .setTitle(fileTitle)
                            .setMimeType(mimeType.getData())
                            .build();
                    DriveFolder.DriveFileResult fileResult = Drive.DriveApi.getRootFolder(googleApiClient)
                            .createFile(googleApiClient, changeSet, driveContents).await();

                    if (!fileResult.getStatus().isSuccess()) {
                        commandCallback.onCommandError("Error while trying to create the file");
                        return;
                    }
                    Drive.DriveApi.requestSync(googleApiClient).await();
                    commandCallback.onCommandSuccess(true);
                }
            });
        }

    }

    @Override
    public void createFile(final String title, final File file, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new FileCreationRequestCallback(title, file, mimeType, commandCallback));
    }

    @Override
    public void createFile(final String title, final String text, final MimeType mimeType, final CommandCallback<Boolean> commandCallback) {
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(new FileCreationRequestCallback(title, text, mimeType, commandCallback));
    }

    @Override
    public void getExistingFile(final String title, final CommandCallback<OnlineFile> commandCallback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Drive.DriveApi.requestSync(googleApiClient).await();
                Query query = new Query.Builder()
                        .addFilter(Filters.contains(SearchableField.TITLE, title))
                        .build();
                DriveApi.MetadataBufferResult result = Drive.DriveApi.query(googleApiClient, query).await();
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
        });
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
        public void rewriteWith(final File file, final CommandCallback<Boolean> commandCallback) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (file != null) {
                        try {
                            InputStream inputStream = new FileInputStream(file);
                            try {
                                boolean b = rewriteInternal(inputStream);
                                commandCallback.onCommandSuccess(b);
                            } catch (Exception e) {
                                commandCallback.onCommandError(e.getMessage());
                            }
                        } catch (FileNotFoundException e) {
                            commandCallback.onCommandError(e.getMessage());
                        }
                    }
                }
            });
        }

        @Override
        public void rewriteWith(final String contents, final CommandCallback<Boolean> commandCallback) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
                    try {
                        boolean b = rewriteInternal(inputStream);
                        commandCallback.onCommandSuccess(b);
                    } catch (Exception e) {
                        commandCallback.onCommandError(e.getMessage());
                    }

                }
            });
        }

        private boolean rewriteInternal(final InputStream inputStream) {
            Drive.DriveApi.requestSync(googleApiClient).await();
            DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_READ_WRITE, null).await();
            if (!driveContentsResult.getStatus().isSuccess()) {
                return false;
            }
            DriveContents driveContents = driveContentsResult.getDriveContents();
            ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            FileOutputStream fileOutputStream = new FileOutputStream(fileDescriptor);
            try {
                fileOutputStream.getChannel().truncate(0);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                    fileOutputStream.flush();
                }
//                fileOutputStream.write("".getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();

                inputStream.close();
                Status await = driveContents.commit(googleApiClient, null).await();
                return await.isSuccess();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void download(final String path, final CommandCallback<Boolean> commandCallback) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        commandCallback.onCommandSuccess(false);
                    }
                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    InputStream inputStream = driveContents.getInputStream();
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(path, false);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        int totalBytes = 0;
                        while((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                            fileOutputStream.flush();
                            totalBytes += bytesRead;
                        }
                        boolean a = totalBytes != 0;
                        commandCallback.onCommandSuccess(true);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void download(final CommandCallback<String> commandCallback) {

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    DriveApi.DriveContentsResult driveContentsResult = driveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null).await();
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        commandCallback.onCommandSuccess("");
                    }
                    DriveContents driveContents = driveContentsResult.getDriveContents();
                    InputStream inputStream = driveContents.getInputStream();
                    try {
                        StringWriter stringWriter = new StringWriter();
                        BufferedWriter writer = new BufferedWriter(stringWriter);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
                            writer.write(new String(buffer, 0, bytesRead), 0, bytesRead);
                            writer.flush();
                        }
                        commandCallback.onCommandSuccess(stringWriter.toString());
                        writer.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

}
