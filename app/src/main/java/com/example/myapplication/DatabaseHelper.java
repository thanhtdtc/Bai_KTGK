package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "tasks.db";
    public static final int DB_VERSION = 3;   // 3: thêm user_id vào bảng tasks

    private static final String CREATE_USERS = "CREATE TABLE users ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "username TEXT NOT NULL UNIQUE, "
            + "password TEXT NOT NULL)";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        db.execSQL("CREATE TABLE tasks ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "title TEXT NOT NULL, "
                + "description TEXT, "
                + "due_date TEXT NOT NULL, "
                + "priority INTEGER NOT NULL, "
                + "done INTEGER NOT NULL DEFAULT 0, "
                + "user_id INTEGER NOT NULL)");

        // Tài khoản mặc định
        long adminId = insertUser(db, "admin", "123456");

        // Dữ liệu mẫu cho admin
        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        insert(db, new Task("Làm bài tập Android", "Hoàn thành bài quản lý công việc", today, 2), adminId);
        insert(db, new Task("Mua sách lập trình", "", today, 1), adminId);
        insert(db, new Task("Dọn dẹp phòng", "", today, 0), adminId);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_USERS);
            insertUser(db, "admin", "123456");
        }
        if (oldVersion < 3) {
            // Công việc cũ được gán cho admin (id = 1)
            db.execSQL("ALTER TABLE tasks ADD COLUMN user_id INTEGER NOT NULL DEFAULT 1");
        }
    }

    // ---------- users ----------
    private long insertUser(SQLiteDatabase db, String username, String password) {
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password", password);
        return db.insert("users", null, v);   // trả về -1 nếu trùng username
    }

    public long insertUser(String username, String password) {
        return insertUser(getWritableDatabase(), username, password);
    }

    public boolean isUsernameExists(String username) {
        Cursor c = getReadableDatabase().query("users", new String[]{"id"},
                "username = ?", new String[]{username}, null, null, null);
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    /** Đăng nhập: trả về id tài khoản nếu đúng, -1 nếu sai. */
    public int login(String username, String password) {
        Cursor c = getReadableDatabase().query("users", new String[]{"id"},
                "username = ? AND password = ?",
                new String[]{username, password}, null, null, null);
        int id = -1;
        if (c.moveToFirst()) {
            id = c.getInt(0);
        }
        c.close();
        return id;
    }

    // ---------- tasks ----------
    private ContentValues toValues(Task t) {
        ContentValues v = new ContentValues();
        v.put("title", t.getTitle());
        v.put("description", t.getDescription());
        v.put("due_date", t.getDueDate());
        v.put("priority", t.getPriority());
        v.put("done", t.isDone() ? 1 : 0);
        return v;
    }

    private long insert(SQLiteDatabase db, Task t, long userId) {
        ContentValues v = toValues(t);
        v.put("user_id", userId);
        return db.insert("tasks", null, v);
    }

    public long insertTask(Task t, int userId) {
        return insert(getWritableDatabase(), t, userId);
    }

    public int updateTask(Task t) {   // không đổi user_id
        return getWritableDatabase().update("tasks", toValues(t), "id = ?",
                new String[]{String.valueOf(t.getId())});
    }

    public int updateDone(int id, boolean done) {
        ContentValues v = new ContentValues();
        v.put("done", done ? 1 : 0);
        return getWritableDatabase().update("tasks", v, "id = ?",
                new String[]{String.valueOf(id)});
    }

    public int deleteTask(int id) {
        return getWritableDatabase().delete("tasks", "id = ?",
                new String[]{String.valueOf(id)});
    }

    private Task fromCursor(Cursor c) {
        return new Task(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("description")),
                c.getString(c.getColumnIndexOrThrow("due_date")),
                c.getInt(c.getColumnIndexOrThrow("priority")),
                c.getInt(c.getColumnIndexOrThrow("done")) == 1);
    }

    public Task getTaskById(int id) {
        Cursor c = getReadableDatabase().query("tasks", null, "id = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Task t = null;
        if (c.moveToFirst()) {
            t = fromCursor(c);
        }
        c.close();
        return t;
    }

    /**
     * Chỉ lấy công việc của tài khoản userId.
     * status: 0 = tất cả, 1 = chưa thực hiện, 2 = đã hoàn thành
     * priority: -1 = mọi mức, 0/1/2 = Thấp/Trung bình/Cao
     */
    public ArrayList<Task> getTasks(int userId, String keyword, int status, int priority) {
        StringBuilder where = new StringBuilder("user_id = ?");
        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (keyword != null && !keyword.trim().isEmpty()) {
            where.append(" AND title LIKE ?");
            args.add("%" + keyword.trim() + "%");
        }
        if (status == 1) {
            where.append(" AND done = 0");
        } else if (status == 2) {
            where.append(" AND done = 1");
        }
        if (priority >= 0) {
            where.append(" AND priority = ?");
            args.add(String.valueOf(priority));
        }

        ArrayList<Task> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query("tasks", null, where.toString(),
                args.toArray(new String[0]), null, null, "id DESC");
        while (c.moveToNext()) {
            list.add(fromCursor(c));
        }
        c.close();
        return list;
    }

    // ---------- Thống kê theo từng tài khoản ----------
    private int count(String sql, int userId) {
        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        int n = 0;
        if (c.moveToFirst()) {
            n = c.getInt(0);
        }
        c.close();
        return n;
    }

    public int countAll(int userId) {
        return count("SELECT COUNT(*) FROM tasks WHERE user_id = ?", userId);
    }

    public int countDone(int userId) {
        return count("SELECT COUNT(*) FROM tasks WHERE user_id = ? AND done = 1", userId);
    }
}