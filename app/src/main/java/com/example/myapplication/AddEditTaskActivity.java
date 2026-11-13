package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText edtTitle, edtDescription;
    private TextView tvDueDate;
    private Spinner spPriority;
    private int userId;

    private final Calendar cal = Calendar.getInstance();
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private int taskId = -1;     // -1: thêm mới, >= 0: chỉnh sửa
    private Task current;        // công việc đang chỉnh sửa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = new DatabaseHelper(this);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        tvDueDate = findViewById(R.id.tvDueDate);
        spPriority = findViewById(R.id.spPriority);
        TextView tvHeader = findViewById(R.id.tvHeaderTitle);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnDelete = findViewById(R.id.btnDelete);

        spPriority.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, Task.PRIORITIES));
        spPriority.setSelection(1); // mặc định Trung bình

        taskId = getIntent().getIntExtra("task_id", -1);
        userId = getIntent().getIntExtra("user_id", -1);
        if (taskId >= 0) {
            current = db.getTaskById(taskId);
            if (current == null) {
                Toast.makeText(this, "Không tìm thấy công việc", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            tvHeader.setText("Sửa công việc");
            btnSave.setText("Cập nhật công việc");
            btnDelete.setVisibility(View.VISIBLE);

            edtTitle.setText(current.getTitle());
            edtDescription.setText(current.getDescription());
            spPriority.setSelection(current.getPriority());
            try {
                cal.setTime(fmt.parse(current.getDueDate()));
            } catch (ParseException ignored) {
                // giữ ngày hiện tại nếu dữ liệu ngày bị lỗi
            }
        }
        tvDueDate.setText(fmt.format(cal.getTime()));

        tvDueDate.setOnClickListener(v -> new DatePickerDialog(this,
                (view, year, month, day) -> {
                    cal.set(year, month, day);
                    tvDueDate.setText(fmt.format(cal.getTime()));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show());

        findViewById(R.id.btnHeaderBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void save() {
        String title = edtTitle.getText().toString().trim();
        if (title.isEmpty()) {
            edtTitle.setError("Tên công việc không được để trống");
            edtTitle.requestFocus();
            return;
        }

        String description = edtDescription.getText().toString().trim();
        String dueDate = fmt.format(cal.getTime());
        int priority = spPriority.getSelectedItemPosition();

        if (taskId >= 0) {
            // Giữ nguyên trạng thái hoàn thành khi chỉnh sửa
            Task t = new Task(taskId, title, description, dueDate, priority, current.isDone());
            db.updateTask(t);
            Toast.makeText(this, "Đã cập nhật công việc", Toast.LENGTH_SHORT).show();
        } else {
            db.insertTask(new Task(title, description, dueDate, priority), userId);
            Toast.makeText(this, "Đã thêm công việc", Toast.LENGTH_SHORT).show();
        }
        finish(); // MainActivity.onResume() sẽ tải lại danh sách và thống kê
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa công việc")
                .setMessage("Bạn có chắc muốn xóa công việc này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.deleteTask(taskId);
                    Toast.makeText(this, "Đã xóa công việc", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}