package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword, edtConfirm;
    private TextView tvError;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = new DatabaseHelper(this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirm = findViewById(R.id.edtConfirm);
        tvError = findViewById(R.id.tvError);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> register());
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> finish());
    }

    private void register() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString();
        String confirm = edtConfirm.getText().toString();

        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin");
            return;
        }
        if (user.length() < 3) {
            showError("Tên đăng nhập phải có ít nhất 3 ký tự");
            return;
        }
        if (user.contains(" ")) {
            showError("Tên đăng nhập không được chứa khoảng trắng");
            return;
        }
        if (pass.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp");
            return;
        }
        if (db.isUsernameExists(user)) {
            showError("Tên đăng nhập đã tồn tại");
            return;
        }

        long id = db.insertUser(user, pass);
        if (id == -1) {
            showError("Đăng ký thất bại, vui lòng thử lại");
            return;
        }

        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra("username", user);
        setResult(RESULT_OK, data);
        finish();   // quay về đăng nhập, tên đăng nhập đã được điền sẵn
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}