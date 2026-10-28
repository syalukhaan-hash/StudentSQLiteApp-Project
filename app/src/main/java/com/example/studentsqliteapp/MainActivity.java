package com.example.studentsqliteapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private DatabaseHelper db;
    private EditText etStudentName, etStudentId;
    private SeekBar seekAge;
    private TextView tvAge;
    private ListView listStudents;

    private final ArrayList<Long> studentIds = new ArrayList<>();
    private long selectedId = -1;
    private int studentAge = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        etStudentName = findViewById(R.id.etStudentName);
        etStudentId = findViewById(R.id.etStudentId);
        seekAge = findViewById(R.id.seekAge);
        tvAge = findViewById(R.id.tvAge);
        listStudents = findViewById(R.id.listStudents);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);
        Button btnView = findViewById(R.id.btnView);

        seekAge.setMax(33);
        seekAge.setProgress(0);

        seekAge.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(
                            SeekBar seekBar, int progress,
                            boolean fromUser) {
                        studentAge = progress + 17;
                        tvAge.setText("Student Age: " + studentAge);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

        btnAdd.setOnClickListener(v -> addStudent());
        btnEdit.setOnClickListener(v -> editStudent());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnView.setOnClickListener(v -> loadStudents());

        listStudents.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectedId = studentIds.get(position);
                    selectStudent(selectedId);
                });

        loadStudents();
    }

    private boolean validateInput() {
        String name = etStudentName.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();

        if (name.isEmpty() || studentId.isEmpty()) {
            showMessage("Please enter Student Name and Student ID.");
            return false;
        }

        if (studentAge < 17 || studentAge > 50) {
            showMessage("Student age must be between 17 and 50.");
            return false;
        }

        return true;
    }

    private void addStudent() {
        if (!validateInput()) return;

        try {
            long result = db.addStudent(
                    etStudentName.getText().toString().trim(),
                    etStudentId.getText().toString().trim(),
                    studentAge);

            if (result != -1) {
                showMessage("Student added successfully.");
                clearForm();
                loadStudents();
            } else {
                showMessage("Failed to add student.");
            }
        } catch (SQLiteConstraintException e) {
            showMessage("Student ID already exists.");
        }
    }

    private void editStudent() {
        if (selectedId == -1) {
            showMessage("Select a student from the list first.");
            return;
        }

        if (!validateInput()) return;

        try {
            int result = db.updateStudent(
                    selectedId,
                    etStudentName.getText().toString().trim(),
                    etStudentId.getText().toString().trim(),
                    studentAge);

            if (result > 0) {
                showMessage("Student updated successfully.");
                clearForm();
                loadStudents();
            } else {
                showMessage("Student was not updated.");
            }
        } catch (SQLiteConstraintException e) {
            showMessage("Student ID already exists.");
        }
    }

    private void confirmDelete() {
        if (selectedId == -1) {
            showMessage("Select a student from the list first.");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete this student?")
                .setNegativeButton("Cancel", (dialog, which) ->
                        dialog.dismiss())
                .setPositiveButton("Delete", (dialog, which) -> {
                    int result = db.deleteStudent(selectedId);

                    if (result > 0) {
                        showMessage("Student deleted successfully.");
                        clearForm();
                        loadStudents();
                    } else {
                        showMessage("Failed to delete student.");
                    }
                })
                .show();
    }

    private void selectStudent(long id) {
        try (Cursor cursor = db.getAllStudents()) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) == id) {
                    etStudentName.setText(cursor.getString(1));
                    etStudentId.setText(cursor.getString(2));
                    seekAge.setProgress(cursor.getInt(3) - 17);
                    return;
                }
            }
        }
    }

    private void loadStudents() {
        ArrayList<String> items = new ArrayList<>();
        studentIds.clear();

        try (Cursor cursor = db.getAllStudents()) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                String studentId = cursor.getString(2);
                int age = cursor.getInt(3);

                studentIds.add(id);
                items.add(name + "\nStudent ID: " + studentId
                        + " | Age: " + age);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items);

        listStudents.setAdapter(adapter);
    }

    private void clearForm() {
        etStudentName.setText("");
        etStudentId.setText("");
        seekAge.setProgress(0);
        selectedId = -1;
        listStudents.clearChoices();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }
}
