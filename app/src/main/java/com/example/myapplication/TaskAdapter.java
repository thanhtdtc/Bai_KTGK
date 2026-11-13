package com.example.myapplication;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TaskAdapter extends ArrayAdapter<Task> {

    public interface OnToggleListener {
        void onToggle(Task task, boolean done);
    }

    private final OnToggleListener listener;

    public TaskAdapter(@NonNull Context context, @NonNull ArrayList<Task> list,
                       @NonNull OnToggleListener listener) {
        super(context, 0, list);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_task, parent, false);
        }
        Task t = getItem(position);

        CheckBox cb = convertView.findViewById(R.id.cbDone);
        TextView tvTitle = convertView.findViewById(R.id.tvTaskTitle);
        TextView tvDesc = convertView.findViewById(R.id.tvTaskDesc);
        TextView tvDue = convertView.findViewById(R.id.tvTaskDue);
        TextView tvPriority = convertView.findViewById(R.id.tvTaskPriority);
        TextView tvStatus = convertView.findViewById(R.id.tvTaskStatus);

        // Gỡ listener cũ trước khi setChecked để dòng được tái sử dụng không bị gọi nhầm
        cb.setOnCheckedChangeListener(null);
        cb.setChecked(t.isDone());
        cb.setOnCheckedChangeListener((btn, checked) -> listener.onToggle(t, checked));

        tvTitle.setText(t.getTitle());
        if (t.isDone()) {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        tvDesc.setText(t.getDescription().isEmpty() ? "(Không có mô tả)" : t.getDescription());
        tvDue.setText("Hạn: " + t.getDueDate());

        // Mức ưu tiên: Thấp (xanh), Trung bình (cam), Cao (đỏ)
        tvPriority.setText("Ưu tiên: " + t.getPriorityName());
        switch (t.getPriority()) {
            case 2:
                tvPriority.setBackgroundResource(R.drawable.bg_chip_warn);
                tvPriority.setTextColor(getContext().getColor(R.color.app_red));
                break;
            case 1:
                tvPriority.setBackgroundResource(R.drawable.bg_chip_mid);
                tvPriority.setTextColor(getContext().getColor(R.color.app_orange));
                break;
            default:
                tvPriority.setBackgroundResource(R.drawable.bg_chip);
                tvPriority.setTextColor(getContext().getColor(R.color.app_primary));
                break;
        }

        // Trạng thái
        if (t.isDone()) {
            tvStatus.setText("Đã hoàn thành");
            tvStatus.setBackgroundResource(R.drawable.bg_chip);
            tvStatus.setTextColor(getContext().getColor(R.color.app_primary));
        } else {
            tvStatus.setText("Chưa thực hiện");
            tvStatus.setBackgroundResource(R.drawable.bg_chip_gray);
            tvStatus.setTextColor(getContext().getColor(R.color.app_gray));
        }
        return convertView;
    }
}