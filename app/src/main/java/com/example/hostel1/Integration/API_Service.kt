package com.example.hostel1.Integration

import android.widget.EditText
import com.example.hostel1.Complaint.RegisterComp.Status.Complaint
import com.example.hostel1.Database.User
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface API_Service {
    @Headers("Accept: application/json")
//    @GET("users/")
    @GET("dorm/user/")
    suspend fun getUsers(): Response<List<User>>

    @POST("attendence/add-grievance/")
    fun makePostComplaint(@Body data: Complaint): Call<Unit>


    @POST("dorm/login/")
    fun makePostRequest(@Body data: Map<String, String>): Call<Unit>


    @POST("dorm/user/")
    fun makePostRegister(@Body data: Map<String, EditText>): Call<Unit>

    @POST("dorm/logout/")
//    fun makePostLogOut(@Body data: Map<String, String>): Call<Unit>
    fun makePostLogOut(@Header("Cookie") sessionCookie: String?): Call<Unit>
    fun makePostLogOut(sessionCookie: List<String>?): Call<Unit>

     @POST("attendence/mark-attendance/")
     fun makePostAttendance(@Body data: AttendanceRequest): Call<Unit>

     @GET("attendence/absenties/")
     fun getAbsent(): Call<List<UserData>>

    @GET("dorm/complaints")
    fun getComplaint():Call<List<Complain>>
}