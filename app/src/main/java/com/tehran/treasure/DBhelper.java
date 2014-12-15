package com.tehran.treasure;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBhelper extends SQLiteOpenHelper {
    public static final int DB_VERSION = 1;
    public static final String TABLE_PLACES = "_tblPlaces";
    public static final String C_PLACE_ID = BaseColumns._ID;
    public static final String C_PLACE_NAME = "plcName";
    public static final String C_PLACE_DETAILS = "plcDetails";
    public static final String C_PLACE_ADDRESS = "plcAddress";
    public static final String C_PLACE_LAT = "plcLatitude";
    public static final String C_PLACE_LON = "plcLongitude";
    public static final String C_PLACE_QUESTION_ID = "plcQuestionID";
    public static final String C_PLACE_ANSWER = "plcAnswer";
    public static final String TABLE_QUESTIONS = "_tblQuestions";
    public static final String C_QUESTION_ID = BaseColumns._ID;
    public static final String C_QUESTION_PLACE_ID = "placeID";
    public static final String C_QUESTION_TEXT = "qstText";
    public static final String C_QUESTION_ANS1 = "qstAnswer1";
    public static final String C_QUESTION_ANS2 = "qstAnswer2";
    public static final String C_QUESTION_ANS3 = "qstAnswer3";
    public static final String C_QUESTION_ANS4 = "qstAnswer4";
    public static String DATABASE_PATH = "/data/data/com.tehran.treasure/databases/";
    private static String DB_NAME = "image.iso";
    final Context context;
    final private String tag = this.getClass().getName();
    private SQLiteDatabase dataBase;

    public DBhelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;

        // checking database and open it if exists
        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();

            } catch (IOException e) {
                throw new Error("Error copying database...");
            }
            // Toast.makeText(context, "Initial database is created",
            // Toast.LENGTH_LONG).show();
        }
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DATABASE_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String dbPath = DATABASE_PATH + DB_NAME;
        dataBase = SQLiteDatabase.openDatabase(dbPath, null,
                SQLiteDatabase.OPEN_READWRITE);
    }

    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        boolean exist = false;
        try {
            String dbPath = DATABASE_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            Log.v(tag, "Database doesn't exist...");
        }

        if (checkDB != null) {
            exist = true;
            checkDB.close();
        }
        return exist;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        File file = new File(DATABASE_PATH + DB_NAME);
        file.delete();

        if (checkDataBase()) {
            openDataBase();
        } else {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();

            } catch (IOException e) {
                throw new Error("Error copying database...");
            }
            // Toast.makeText(context, "Initial database is created",
            // Toast.LENGTH_LONG).show();
        }
    }

    public Cursor selectPlace(int placeID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s, %s, %s FROM %s WHERE %S=%s",
                C_PLACE_ID, C_PLACE_NAME, C_PLACE_DETAILS, C_PLACE_ADDRESS,
                C_PLACE_LAT, C_PLACE_LON, C_PLACE_QUESTION_ID, TABLE_PLACES,
                C_PLACE_ID, placeID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        return cur;
    }

    public Cursor selectQuestion(int questionID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %S=%s",
                C_QUESTION_ID, C_QUESTION_TEXT, C_QUESTION_ANS1,
                C_QUESTION_ANS2, C_QUESTION_ANS3, C_QUESTION_ANS4,
                TABLE_QUESTIONS, C_QUESTION_ID, questionID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        return cur;
    }

    public int getPlaceQuestionCount(int placeID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format("SELECT %s FROM %s WHERE %S=%s",
                C_QUESTION_ID, TABLE_QUESTIONS, C_QUESTION_PLACE_ID, placeID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        return cur.getCount();
    }

    public Cursor selectAllPlaceQuestions(int placeID) {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format(
                "SELECT %s, %s, %s, %s, %s, %s FROM %s WHERE %s=%s",
                C_QUESTION_ID, C_QUESTION_TEXT, C_QUESTION_ANS1,
                C_QUESTION_ANS2, C_QUESTION_ANS3, C_QUESTION_ANS4,
                TABLE_QUESTIONS, C_QUESTION_PLACE_ID, placeID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        return cur;
    }

    public void insertPlaceQuestion(int placeID, int questionID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(C_PLACE_QUESTION_ID, questionID);

        String wClause = C_PLACE_ID + "=?";
        String[] args = {String.valueOf(placeID)};
        db.update(TABLE_PLACES, cv, wClause, args);

        db.close();
    }

    public void insertPlaceAnswer(int placeID, int answer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(C_PLACE_ANSWER, answer);

        String wClause = C_PLACE_ID + "=?";
        String[] args = {String.valueOf(placeID)};
        db.update(TABLE_PLACES, cv, wClause, args);

        db.close();
    }

    public int getAnswersCount() {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format("SELECT %s FROM %s WHERE %S NOT NULL",
                C_PLACE_ID, TABLE_PLACES, C_PLACE_ANSWER);
        Cursor cur = db.rawQuery(sql, new String[]{});

        return cur.getCount();
    }

    public boolean[] getAnsweredBlocks() {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String.format("SELECT %s FROM %s ORDER BY %S",
                C_PLACE_ANSWER, TABLE_PLACES, C_PLACE_ID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        boolean[] result = new boolean[cur.getCount()];
        int i = 0;

        while (cur.moveToNext()) {
            try {
                if (!cur.getString(cur.getColumnIndex(C_PLACE_ANSWER)).equals(
                        ""))
                    result[i] = false;
            } catch (Exception e) {
                result[i] = true;
            }

            i++;
        }

        return result;
    }

    public String getBlocksDealInfo() {
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = String
                .format("SELECT %s, %s FROM %s WHERE %s NOT NULL AND %s NOT NULL ORDER BY %s",
                        C_PLACE_QUESTION_ID, C_PLACE_ANSWER, TABLE_PLACES,
                        C_PLACE_QUESTION_ID, C_PLACE_ANSWER, C_PLACE_ID);
        Cursor cur = db.rawQuery(sql, new String[]{});

        String result = "B.";
        while (cur.moveToNext()) {
            result += "\n";
            result += cur.getString(cur.getColumnIndex(C_PLACE_QUESTION_ID));
            result += "\t";
            result += cur.getString(cur.getColumnIndex(C_PLACE_ANSWER));
        }

        return result;
    }
}
