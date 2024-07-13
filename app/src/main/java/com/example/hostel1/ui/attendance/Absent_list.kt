package com.example.hostel1.ui.attendance

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.hostel1.Database.UserViewModel
import com.example.hostel1.Integration.RetrofitClient.apiService
import com.example.hostel1.Integration.UserData
import com.example.hostel1.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Absent_list : AppCompatActivity() {

    private lateinit var tableLayout: TableLayout
    private lateinit var presentCountTextView: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var progressBar: ProgressBar

    private lateinit var selectedDates: String

    private var presentCount = 0
    private var totalStudents = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absent_list)

        tableLayout = findViewById(R.id.tableLayout)
        presentCountTextView = findViewById(R.id.tvPresentCount)
        progressBar = findViewById(R.id.progressBar) // Initialize ProgressBar

        // Retrieve selected date from intent extras
        val selectedDate = intent.getStringExtra("date")

        if (selectedDate != null) {
            selectedDates = selectedDate
        }

        // Display selected date in TextView
        findViewById<TextView>(R.id.tvSelectedDate).apply {
            text = "Selected Date: $selectedDate"
            setTextColor(resources.getColor(R.color.white))
        }

        sendRequest()

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        findViewById<Button>(R.id.submitButton)?.setOnClickListener {
            showAttendanceSummary()
        }
    }

    private fun sendRequest() {
        val call: Call<List<UserData>> = apiService.getAbsent()

        call.enqueue(object : Callback<List<UserData>> {
            override fun onResponse(call: Call<List<UserData>>, response: Response<List<UserData>>) {
                if (response.isSuccessful) {
                    progressBar.visibility = View.GONE
                    response.body()?.let {
                        Log.d("Main", "success! $it")
                        if (it.isNotEmpty()) {
                            populateTable(it)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<UserData>>, t: Throwable) {
                Log.e("Main", "Failed mate ${t.message}")
            }
        })
    }

    private fun populateTable(users: List<UserData>) {
        // Clear existing rows except the header row
        val childCount = tableLayout.childCount
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1)
        }

        totalStudents = users.size

        for ((index, userData) in users.withIndex()) {
            val student = userData.student
            val tableRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            val serialNumber = TextView(this).apply {
                text = (index + 1).toString()
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            }

            val roomNo = TextView(this).apply {
                text = student.room_no
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            }

            val studentName = TextView(this).apply {
                text = "${student.first_name} ${student.last_name}"
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            }

            val fatherName = TextView(this).apply {
                text = student.f_name
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            }

            val mobileNo = TextView(this).apply {
                text = student.contact
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
            }

            tableRow.addView(serialNumber)
            tableRow.addView(roomNo)
            tableRow.addView(studentName)
            tableRow.addView(fatherName)
            tableRow.addView(mobileNo)

            tableLayout.addView(tableRow)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updatePresentCount() {
        presentCountTextView.text = "Present Count: $presentCount"
    }

    private fun showAttendanceSummary() {
        val absentCount = totalStudents - presentCount
        val message = "Present: $presentCount\nAbsent: $absentCount\n Total: $totalStudents"

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Attendance Summary")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
