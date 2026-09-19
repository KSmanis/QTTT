package com.gmail.smanis.konstantinos.qttt;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

final class OpeningBook {
    private static final String TAG = "OpeningBook";
    private static final String DATABASE_NAME = "opening-book-v1.db";
    static FutureTask<SQLiteDatabase> databaseTask;

    private OpeningBook() {}

    static synchronized void initialize(Context context) {
        if (databaseTask != null) {
            return;
        }

        Context applicationContext = context.getApplicationContext();
        databaseTask = new FutureTask<>(() -> open(applicationContext));
        new Thread(databaseTask, "opening-book-init").start();
    }

    static List<Move> lookup(Context context, State state) {
        try {
            SQLiteDatabase db = database(context);
            try (Cursor cursor =
                    db.rawQuery(
                            "SELECT utility_value, utility_depth, moves "
                                    + "FROM opening_book WHERE history = ?",
                            new String[] {state.moveHistory()})) {
                if (!cursor.moveToFirst()) {
                    return Collections.emptyList();
                }
                return state.openingMoves(
                        new Utility(cursor.getInt(0), cursor.getInt(1)), cursor.getString(2));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CancellationException();
        } catch (ExecutionException | SQLiteException e) {
            Log.e(TAG, "Opening book unavailable", e);
            return Collections.emptyList();
        }
    }

    static SQLiteDatabase database(Context context)
            throws ExecutionException, InterruptedException {
        initialize(context);
        return databaseTask.get();
    }

    private static SQLiteDatabase open(Context context) throws IOException {
        File file = context.getDatabasePath(DATABASE_NAME);
        if (!file.exists()) {
            copy(context, file);
        }
        return SQLiteDatabase.openDatabase(
                file.getPath(),
                null,
                SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    private static void copy(Context context, File target) throws IOException {
        File parent = target.getParentFile();
        if (parent == null || (!parent.isDirectory() && !parent.mkdirs())) {
            throw new IOException("Cannot create database directory");
        }

        File temporary = new File(parent, target.getName() + ".tmp");
        try (InputStream input = new BufferedInputStream(context.getAssets().open(DATABASE_NAME));
                FileOutputStream fileOutput = new FileOutputStream(temporary);
                BufferedOutputStream output = new BufferedOutputStream(fileOutput)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
            output.flush();
            fileOutput.getFD().sync();
        }
        if (!temporary.renameTo(target)) {
            throw new IOException("Cannot install opening book");
        }
    }
}
