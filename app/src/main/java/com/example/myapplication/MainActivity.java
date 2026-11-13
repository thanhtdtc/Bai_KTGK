package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private int userId;

    private DatabaseHelper db;
    private final ArrayList<Task> tasks = new ArrayList<>(); // chỉ chứa kết quả truy vấn
    private TaskAdapter adapter;

    private EditText edtSearch;
    private Spinner spStatus, spPriority;
    private TextView tvTotal, tvDone, tvTodo, tvEmpty;
    private ListView lvTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId == -1) {   // chưa đăng nhập thì quay về màn hình đăng nhập
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        edtSearch = findViewById(R.id.edtSearch);
        spStatus = findViewById(R.id.spStatus);
        spPriority = findViewById(R.id.spPriority);
        tvTotal = findViewById(R.id.tvTotal);
        tvDone = findViewById(R.id.tvDone);
        tvTodo = findViewById(R.id.tvTodo);
        tvEmpty = findViewById(R.id.tvEmpty);
        lvTasks = findViewById(R.id.lvTasks);
        Button btnAdd = findViewById(R.id.btnAdd);

        // Tick / bỏ tick ngay trên danh sách -> lưu trạng thái vào SQLite
        adapter = new TaskAdapter(this, tasks, (task, done) -> {
            db.updateDone(task.getId(), done);
            loadData();
        });
        lvTasks.setAdapter(adapter);

        spStatus.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Tất cả", "Chưa thực hiện", "Đã hoàn thành"}));
        spPriority.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Mọi mức ưu tiên", "Thấp", "Trung bình", "Cao"}));

        AdapterView.OnItemSelectedListener reload = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        spStatus.setOnItemSelectedListener(reload);
        spPriority.setOnItemSelectedListener(reload);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadData();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            intent.putExtra("user_id", userId);
            startActivity(intent);
        });

        // Nhấn vào một công việc -> mở màn hình chỉnh sửa
        lvTasks.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, AddEditTaskActivity.class);
            intent.putExtra("task_id", tasks.get(position).getId());
            startActivity(intent);
        });

        // Nhấn giữ -> xóa
        lvTasks.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(tasks.get(position));
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        tasks.clear();
        tasks.addAll(db.getTasks(
                userId,
                edtSearch.getText().toString(),
                spStatus.getSelectedItemPosition(),
                spPriority.getSelectedItemPosition() - 1));
        adapter.notifyDataSetChanged();

        int total = db.countAll(userId);
        int done = db.countDone(userId);
        tvTotal.setText(String.valueOf(total));
        tvDone.setText(String.valueOf(done));
        tvTodo.setText(String.valueOf(total - done));

        boolean empty = tasks.isEmpty();
        tvEmpty.setText(total == 0 ? "Chưa có công việc nào" : "Không tìm thấy công việc phù hợp");
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        lvTasks.setVisibility(empty ? View.GONE : View.VISIBLE);
    }


    private void confirmDelete(Task task) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc muốn xóa \"" + task.getTitle() + "\"?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deleteTask(task.getId());
                    loadData();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}