package com.example.myapplication;

public class Task {
    public static final String[] PRIORITIES = {"Thấp", "Trung bình", "Cao"};

    private int id;
    private String title;
    private String description;
    private String dueDate;   // dd/MM/yyyy
    private int priority;     // 0: Thấp, 1: Trung bình, 2: Cao
    private boolean done;

    public Task(int id, String title, String description,
                String dueDate, int priority, boolean done) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.done = done;
    }

    // Dùng khi thêm mới (chưa có id, mặc định chưa thực hiện)
    public Task(String title, String description, String dueDate, int priority) {
        this(0, title, description, dueDate, priority, false);
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return dueDate; }
    public int getPriority() { return priority; }
    public boolean isDone() { return done; }
    public String getPriorityName() { return PRIORITIES[priority]; }
}