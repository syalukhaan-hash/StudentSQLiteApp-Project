package com.example.studentsqliteapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "students.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_STUDENTS = "students";
    public static final String COL_ID = "_id";
    public static final String COL_NAME = "student_name";
    public static final String COL_STUDENT_ID = "student_id";
    public static final String COL_AGE = "student_age";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_STUDENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_STUDENT_ID + " TEXT NOT NULL UNIQUE, " +
                COL_AGE + " INTEGER NOT NULL CHECK (" +
                COL_AGE + " BETWEEN 17 AND 50))";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENTS);
        onCreate(db);
    }

    public long addStudent(String name, String studentId, int age) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_AGE, age);

        return getWritableDatabase().insert(
                TABLE_STUDENTS, null, values);
    }

    public int updateStudent(long id, String name,
                             String studentId, int age) {
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_STUDENT_ID, studentId);
        values.put(COL_AGE, age);

        return getWritableDatabase().update(
                TABLE_STUDENTS,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int deleteStudent(long id) {
        return getWritableDatabase().delete(
                TABLE_STUDENTS,
                COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public Cursor getAllStudents() {
        return getReadableDatabase().query(
                TABLE_STUDENTS,
                new String[]{
                        COL_ID,
                        COL_NAME,
                        COL_STUDENT_ID,
                        COL_AGE
                },
                null, null, null, null,
                COL_NAME + " COLLATE NOCASE ASC");
    }
}
