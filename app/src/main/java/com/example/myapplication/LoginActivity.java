package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername, edtPassword;
    private TextView tvError;
    private DatabaseHelper db;

    // Nhận tên đăng nhập vừa đăng ký xong để điền sẵn vào ô đăng nhập
    private final ActivityResultLauncher<Intent> registerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    edtUsername.setText(result.getData().getStringExtra("username"));
                    edtPassword.setText("");
                    edtPassword.requestFocus();
                    tvError.setVisibility(View.GONE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvError = findViewById(R.id.tvError);
        Button btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> login());
        findViewById(R.id.tvGoRegister).setOnClickListener(v ->
                registerLauncher.launch(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String user = edtUsername.getText().toString().trim();
        String pass = edtPassword.getText().toString();

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }
        int userId = db.login(user, pass);   // -1 nếu sai
        if (userId != -1) {
            tvError.setVisibility(View.GONE);
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("username", user);
            startActivity(intent);
            finish();
        } else {
            showError("Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    private void showError(String msg) {
        tvError.setText(msg);
        tvError.setVisibility(View.VISIBLE);
    }
}