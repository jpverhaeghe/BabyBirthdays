/**
 * Copyright (C) 2012 - TwoTau, LLC
 */

package com.twotao.babybirthdays;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple Birthdays database access helper class. Defines the basic CRUD operations
 * for the Baby Birthdays application, and gives the ability to list all birthdays as well as
 * retrieve or modify a specific birthday.
 * 
 * @author Jim
 *
 */
public class BirthdaysDbAdapter {
    
	public static final String KEY_NAME 		= "name";
    public static final String KEY_BIRTHDATE 	= "birthdate";
    public static final String KEY_ROWID 		= "_id";

    private static final String TAG = "BirthdaysDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table birthdays (_id integer primary key autoincrement, "
        + "name text not null, birthdate integer not null);";

    private static final String DATABASE_NAME = "baby_data";
    private static final String DATABASE_TABLE = "birthdays";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	
        	// TODO:  Check to see if there is a way to save the data if the DB 
        	//			needs to be upgraded (ALTER_TABLE?)
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS birthdays");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public BirthdaysDbAdapter(Context ctx) {
    	
        this.mCtx = ctx;
    }

    /**
     * Open the birthdays database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public BirthdaysDbAdapter open() throws SQLException {
    	
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
    	
        mDbHelper.close();
    }

    /**
     * Create a new birthday using the name and birth date provided. If the birthday is
     * successfully created return the new rowId for that birthday, otherwise return
     * a -1 to indicate failure.
     * 
     * @param name the name of the person who has the birthday
     * @param birthdate the birth date of the birthday
     * @return rowId or -1 if failed
     */
    public long createBirthday(String name, long birthdate) {
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_NAME, name);
        initialValues.put(KEY_BIRTHDATE, birthdate);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the birthday with the given rowId
     * 
     * @param rowId id of birthday to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteBirthday(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all birthdays in the database
     * 
     * @return Cursor over all birthdays
     */
    public Cursor fetchAllBirthdays() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME,
                KEY_BIRTHDATE}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the birthday that matches the given rowId
     * 
     * @param rowId id of birthday to retrieve
     * @return Cursor positioned to matching birthday, if found
     * @throws SQLException if birthday could not be found/retrieved
     */
    public Cursor fetchBirthday(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_NAME, KEY_BIRTHDATE}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        
        return mCursor;
    }

    /**
     * Update the birthday using the details provided. The birthday to be updated is
     * specified using the rowId, and it is altered to use the name and birth date
     * values passed in
     * 
     * @param rowId id of birthday to update
     * @param name value to set birthday name to
     * @param birthdate value to set birthday birth date to
     * @return true if the birthday was successfully updated, false otherwise
     */
    public boolean updateBirthday(long rowId, String name, long birthdate) {
    	
        ContentValues args = new ContentValues();
        args.put(KEY_NAME, name);
        args.put(KEY_BIRTHDATE, birthdate);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
