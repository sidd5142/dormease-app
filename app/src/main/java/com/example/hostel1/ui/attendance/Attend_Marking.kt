package com.example.hostel1.ui.attendance

import UserRepository
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.hostel1.Database.AppDatabase
import com.example.hostel1.Database.UserEntity
import com.example.hostel1.Database.UserViewModel
import com.example.hostel1.Database.UserViewModelFactory
import com.example.hostel1.Integration.API_Service
import com.example.hostel1.Integration.AttendanceRecord
import com.example.hostel1.Integration.AttendanceRequest
import com.example.hostel1.Integration.RetrofitClient
import com.example.hostel1.R
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Attend_Marking : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var presentCountTextView: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var selectAllCheckBox: CheckBox
    private val checkBoxList = mutableListOf<CheckBox>()
    private lateinit var progressBar: ProgressBar

    private lateinit var selectedDates: String
    private var student_id: Int? = null
    private var presentCount = 0
    private var totalStudents = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            selectedDates = it.getString("date", "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_attend__marking, container, false)

        tableLayout = view.findViewById(R.id.tableLayout)
        presentCountTextView = view.findViewById(R.id.tvPresentCount)
        selectAllCheckBox = view.findViewById(R.id.selectAllCheckBox)
        progressBar = view.findViewById(R.id.progressBar)

        // Display selected date in TextView
        view.findViewById<TextView>(R.id.tvSelectedDate).apply {
            text = "Selected Date: $selectedDates"
            setTextColor(resources.getColor(R.color.white))
        }

        // Initialize Room database
        val appDatabase = Room.databaseBuilder(
            requireContext().applicationContext,
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
        try {
            userViewModel.fetchUsers()
        } catch (e: Exception) {
            progressBar.visibility = View.GONE
            Log.e("FetchUsersError", "Error fetching users: ${e.message}")
        }

        // Observe users LiveData for updates
        userViewModel.users.observe(viewLifecycleOwner, Observer { users ->
            // Update your UI with the fetched data
            totalStudents = users.size
            populateTable(users)
            progressBar.visibility = View.GONE
        })

        selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            checkBoxList.forEach { it.isChecked = isChecked }
        }

        view.findViewById<Button>(R.id.submitButton)?.setOnClickListener {
            showAttendanceSummary()
        }

        return view
    }

    private fun populateTable(users: List<UserEntity>) {
        // Clear existing rows except the header row
        val childCount = tableLayout.childCount
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1)
        }

        checkBoxList.clear()

        for ((index, user) in users.withIndex()) {
            val tableRow = TableRow(context).apply {
                layoutParams = TableRow.LayoutParams(
                    TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT
                )
            }

            val checkBox = CheckBox(context).apply {
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

            val serialNumber = TextView(context).apply {
                text = (index + 1).toString()
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val roomNo = TextView(context).apply {
                text = user.room_no.toString()
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val studentName = TextView(context).apply {
                text = user.name
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val fatherName = TextView(context).apply {
                text = user.f_name
                setTextColor(resources.getColor(R.color.white))
                setPadding(8, 8, 8, 8)
                setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            }

            val mobileNo = TextView(context).apply {
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

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Attendance Summary")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
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
                    println("message is, ${response.isSuccessful}")
                    println("Response, ${response.headers()}")
                    val responseBody = response.body()
                    // Handle the response if needed
                    startActivity(Intent(requireContext(), Absent_list::class.java))
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
