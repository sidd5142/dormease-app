package com.example.hostel1.Integration

import UserRepository
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.hostel1.Database.AppDatabase
import com.example.hostel1.Database.UserEntity
import com.example.hostel1.Database.UserViewModel
import com.example.hostel1.Database.UserViewModelFactory
import com.example.hostel1.R
import com.example.hostel1.ui.attendance.Absent_list
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Test : AppCompatActivity() {
    private lateinit var tableLayout: TableLayout
    private lateinit var presentCountTextView: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var selectAllCheckBox: CheckBox
    private val checkBoxList = mutableListOf<CheckBox>()
    private lateinit var progressBar: ProgressBar

    private lateinit var selectedDates : String


    private var student_id : Int?=null
    private var presentCount = 0
    private var totalStudents = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        tableLayout = findViewById(R.id.tableLayout)
        presentCountTextView = findViewById(R.id.tvPresentCount)
        selectAllCheckBox = findViewById(R.id.selectAllCheckBox)
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

        // Initialize Room database
        val appDatabase = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "AppDatabase"
        ).fallbackToDestructiveMigration()
            .build()

        // Initialize Retrofit API service
        val apiService = Retrofit.Builder()
            .baseUrl("https://5fe8-125-21-249-98.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(API_Service::class.java)

        // Initialize UserRepository with API service and Room database
        val repository = UserRepository(apiService, appDatabase)

        // Initialize UserViewModel with UserRepository
        val viewModelFactory = UserViewModelFactory(repository)
        userViewModel = ViewModelProvider(this, viewModelFactory).get(UserViewModel::class.java)

        // Show progress bar
        progressBar.visibility = View.VISIBLE

        // Fetch users
//        userViewModel.fetchUsers()
        try {
            userViewModel.fetchUsers()
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Log.e("FetchUsersError", "Error fetching users: ${e.message}")
        }

        // Observe users LiveData for updates
        userViewModel.users.observe(this, Observer { users ->
            // Update your UI with the fetched data
            totalStudents = users.size
            populateTable(users)

            progressBar.visibility = View.GONE

        })

        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            checkBoxList.forEach { it.isChecked = isChecked }
        }

        findViewById<Button>(R.id.submitButton)?.setOnClickListener {
            showAttendanceSummary()
        }
    }

    private fun populateTable(users: List<UserEntity>) {
        // Clear existing rows except the header row
        val childCount = tableLayout.childCount
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1)
        }

        checkBoxList.clear()

        for ((index, user) in users.withIndex()) {
            val tableRow = TableRow(this).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            val checkBox = CheckBox(this).apply {
                tag = user.id
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        presentCount++
                    } else {
                        presentCount--
                    }
                    updatePresentCount()
                }
            }

            checkBoxList.add(checkBox)

            val serialNumber = TextView(this).apply {
                text = (index + 1).toString()
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val roomNo = TextView(this).apply {
                text = user.room_no.toString()
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val studentName = TextView(this).apply {
                text = user.name
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val fatherName = TextView(this).apply {
                text = user.f_name
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val mobileNo = TextView(this).apply {
                text = user.contact
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            student_id = user.id

            tableRow.addView(checkBox)
            tableRow.addView(serialNumber)
            tableRow.addView(roomNo)
            tableRow.addView(studentName)
            tableRow.addView(fatherName)
            tableRow.addView(mobileNo)

            tableLayout.addView(tableRow)
        }
    }

    private fun updatePresentCount() {
        presentCountTextView.text = "Present Count: $presentCount"
    }

    private fun showAttendanceSummary() {
        val absentCount = totalStudents - presentCount
        val message = "Present: $presentCount\nAbsent: $absentCount\n Total: $totalStudents"

        logDataAsJson()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Attendance Summary")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
//            startActivity(Intent(this,Absent_list::class.java))
            val data = mapOf("identifier" to "Sidd", "password" to "next")

            sendAbsentDetails(logDataAsJson())
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun sendAbsentDetails(data1: AttendanceRequest) {
        val call = RetrofitClient.apiService.makePostAttendance(data1)
        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Log.d("Attend", "Attendance Send Successfully : ${response.message()}")
                    println("message is, ${response.isSuccessful}");
                    println("Response, ${response.headers()}")
                    val responseBody = response.body()

//                    val message = responseBody?.message
//
//                    Log.d("Attendance", "Message: $message")

                    startActivity(Intent(this@Test, Absent_list::class.java))
                    finish()
                } else {
                    Log.e("AttendFault", "Failed to send attendance. Error code: ${response.code()}")
                    println("Failed to send attendance. Error code: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.e("AttendFault", "POST request failed", t)
                println("Network request failed. Please try again.")
            }
        })
    }



    private fun logDataAsJson(): AttendanceRequest {
        var formattedDate = selectedDates

        try {
            val inputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsedDate: Date? = inputDateFormat.parse(selectedDates)

            if (parsedDate != null) {
                val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                formattedDate = outputDateFormat.format(parsedDate)
            }
        } catch (e: Exception) {
            Log.e("AttendanceData", "Error parsing or formatting date: ${e.message}")
        }

        val attendanceData = mutableListOf<AttendanceRecord>()

        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            val checkBox = row.getChildAt(0) as CheckBox
            val studentId = checkBox.tag as Int

            val isPresent = checkBox.isChecked
            val atdStatus = if (isPresent) "P" else "A"

            val studentData = AttendanceRecord(
                student = studentId,
                atd_status = atdStatus
            )

            attendanceData.add(studentData)
        }

        return AttendanceRequest(
            date = formattedDate,
            atd_rec = attendanceData
        )
    }
}
