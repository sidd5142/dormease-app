package com.example.hostel1.ui.attendance

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.hostel1.Integration.Test
import com.example.hostel1.R
import com.example.hostel1.databinding.FragmentDatePickerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DatePicker : Fragment() {

    private lateinit var btnDatePicker: Button
    private lateinit var tvSelectedDate: TextView
    private val calendar = Calendar.getInstance()

    private var _binding: FragmentDatePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDatePickerBinding.inflate(inflater, container, false)
        val view = binding.root

        btnDatePicker = binding.btnDatePicker
        tvSelectedDate = binding.tvSelectedDate

        btnDatePicker.setOnClickListener {
            showDatePicker()
        }

        return view
    }

    private fun showDatePicker() {
        // Create a DatePickerDialog
        val datePickerDialog = DatePickerDialog(
            requireActivity(), { _, year, monthOfYear, dayOfMonth ->
                // Create a new Calendar instance to hold the selected date
                val selectedDate = Calendar.getInstance()
                // Set the selected date using the values received from the DatePicker dialog
                selectedDate.set(year, monthOfYear, dayOfMonth)
                // Create a SimpleDateFormat to format the date as "dd/MM/yyyy"
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                // Format the selected date into a string
                val formattedDate = dateFormat.format(selectedDate.time)
                // Update the TextView to display the selected date with the "Selected Date: " prefix
                tvSelectedDate.text = "Selected Date: $formattedDate"

                // Navigate to Test activity and pass the selected date
//                val action = DatePickerFragmentDirections.actionDatePickerToTest(formattedDate)
//                findNavController().navigate(action)
                val action = Intent(requireActivity(), Test::class.java)
                action.putExtra("date",formattedDate )
                startActivity(action)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Show the DatePicker dialog
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
