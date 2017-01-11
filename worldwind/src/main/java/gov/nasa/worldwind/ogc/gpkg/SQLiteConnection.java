/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.gpkg;

import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.TimeUnit;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

public class SQLiteConnection {

    protected String pathName;

    protected int flags;

    protected long keepAliveTime;

    protected Handler handler;

    protected SQLiteDatabase database;

    protected final Object lock = new Object();

    protected static final int CONNECTION_TIMEOUT = 1;

    public SQLiteConnection(String pathName, int flags, long keepAliveTime, TimeUnit unit) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SQLiteConnection", "constructor", "missingPathName"));
        }

        this.pathName = pathName;
        this.flags = flags;
        this.keepAliveTime = unit.toMillis(keepAliveTime);
        this.handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return SQLiteConnection.this.handleMessage(msg);
            }
        });
    }

    public String getPathName() {
        return this.pathName;
    }

    public int getFlags() {
        return this.flags;
    }

    public long getKeepAliveTime() {
        return this.keepAliveTime;
    }

    public void setKeepAliveTime(long time, TimeUnit unit) {
        this.keepAliveTime = unit.toMillis(time);
        this.handler.removeMessages(CONNECTION_TIMEOUT);
        this.handler.sendEmptyMessageDelayed(CONNECTION_TIMEOUT, this.keepAliveTime);
    }

    public SQLiteDatabase openDatabase() {
        synchronized (this.lock) {
            if (this.database == null) {
                this.database = SQLiteDatabase.openDatabase(this.pathName, null, this.flags);

                Logger.logMessage(Logger.INFO, "SQLiteConnection", "openDatabase",
                    "SQLite connection opened " + this.pathName);
            }

            this.database.acquireReference();
            this.handler.removeMessages(CONNECTION_TIMEOUT);
            this.handler.sendEmptyMessageDelayed(CONNECTION_TIMEOUT, this.keepAliveTime);

            return this.database;
        }
    }

    protected void onConnectionTimeout() {
        synchronized (this.lock) {
            WWUtil.closeSilently(this.database);

            Logger.logMessage(Logger.INFO, "SQLiteConnection", "onConnectionTimeout",
                "SQLite connection keep alive timeout " + this.pathName);

            if (this.database.isOpen()) {
                Logger.logMessage(Logger.WARN, "SQLiteConnection", "onConnectionTimeout",
                    "SQLite connection open after timeout " + this.pathName);
            }

            this.database = null;
        }
    }

    protected boolean handleMessage(Message msg) {
        if (msg.what == CONNECTION_TIMEOUT) {
            this.onConnectionTimeout();
        }

        return false;
    }
}
