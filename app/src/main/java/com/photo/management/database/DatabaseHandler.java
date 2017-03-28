package com.photo.management.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.photo.management.dao.Photo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "photoManager";

    // Contacts table name
    private static final String TABLE_PHOTO = "photo";

    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_REF_ID = "pic_reference_id";
    private static final String KEY_PATH = "pic_path";
    private static final String KEY_TAG = "tag_name";
    private static final String KEY_DOWNLOAD = "isDownLoad";
    private static final String KEY_DATE = "date";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_PHOTO + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_REF_ID + " INTEGER," + KEY_PATH + " TEXT," + KEY_DATE + " TEXT," + KEY_TAG + " BOOLEAN," + KEY_DOWNLOAD + " BOOLEAN" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);

        // Create tables again
        onCreate(db);
    }

    public void tagImageDb(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, photo.getTagName());
        values.put(KEY_REF_ID, photo.getRefId());
        values.put(KEY_PATH, photo.getPicPath());
        values.put(KEY_TAG, photo.isTag());
        values.put(KEY_DOWNLOAD, photo.isDownload());
        values.put(KEY_DATE, photo.getDate());

        // Inserting Row
        db.insert(TABLE_PHOTO, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new contact
/*    void addContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_PH_NO, contact.getPhoneNumber()); // Contact Phone

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // Getting single contact
    Contact getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                        KEY_NAME, KEY_PH_NO}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)),
                cursor.getString(1), cursor.getString(2));
        // return contact
        return contact;
    }

    // Getting All Contacts
    public List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<Contact>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Contact contact = new Contact();
                contact.setID(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Updating single contact
    public int updateContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PH_NO, contact.getPhoneNumber());

        // updating row
        return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(contact.getID())});
    }

    // Deleting single contact
   */
    public int getContactsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT  * FROM " + TABLE_PHOTO;

        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();

        // return count
        return cursor.getCount();
    }

    public ArrayList<Photo> getAllPics() {
        ArrayList<Photo> contactList = new ArrayList<Photo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PHOTO;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Photo contact = new Photo();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setTagName(cursor.getString(1));
                contact.setRefId(Integer.parseInt(cursor.getString(2)));
                contact.setPicPath(cursor.getString(3));
                contact.setDate(cursor.getString(4));
                if (cursor.getString(5).equalsIgnoreCase("0"))
                    contact.setTag(false);
                else contact.setTag(true);
                if (cursor.getString(6).equalsIgnoreCase("0"))
                    contact.setDownload(false);
                else contact.setDownload(true);
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }


    public int updateContact(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, photo.getTagName());
        values.put(KEY_REF_ID, photo.getRefId());
        values.put(KEY_PATH, photo.getPicPath());
        values.put(KEY_TAG, photo.isTag());
        values.put(KEY_DOWNLOAD, photo.isDownload());
        values.put(KEY_DATE, photo.getDate());

        // updating row
        return db.update(TABLE_PHOTO, values, KEY_REF_ID + " = ?",
                new String[]{String.valueOf(photo.getRefId())});
    }
    public void deletePicture(int refId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTO, KEY_REF_ID + " = ?",
                new String[]{String.valueOf(refId)});
        db.close();
    }

    public Photo getPicDetail(int refId) {
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_PHOTO + " where pic_reference_id=" + refId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        Photo contact = null;
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                contact = new Photo();
                contact.setId(Integer.parseInt(cursor.getString(0)));
                contact.setTagName(cursor.getString(1));
                contact.setRefId(Integer.parseInt(cursor.getString(2)));
                contact.setPicPath(cursor.getString(3));
                contact.setDate(cursor.getString(4));
                if (cursor.getString(5).equalsIgnoreCase("0"))
                    contact.setTag(false);
                else contact.setTag(true);
                if (cursor.getString(6).equalsIgnoreCase("0"))
                    contact.setDownload(false);
                else contact.setDownload(true);
                // Adding contact to list
            } while (cursor.moveToNext());
        }

        // return contact list
        return contact;
    }

    public int updateImagePath(int refId, String path) {
        //String selectQuery = "SELECT  * FROM " + TABLE_PHOTO + " where pic_reference_id=" + refId;
        //String selectQuery = "UPDATE "+TABLE_PHOTO +" SET " + KEY_PATH+ " = '"+path+"' WHERE pic_reference_id=" + refId;

        ///SQLiteDatabase db = this.getWritableDatabase();
       //Cursor cursor = db.rawQuery(selectQuery, null);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_PATH, path);

        // updating row
        return db.update(TABLE_PHOTO, values, KEY_REF_ID + " = ?",
                new String[]{String.valueOf(refId)});
    }
}