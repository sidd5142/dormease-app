package com.example.hostel1.Database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val room_no: Int,
    val id:Int,
    val contact: String,
    val name: String,
    val f_name: String
)

data class UserResponse(
    @SerializedName("users")
    val users: List<User>
)

data class User(
    @SerializedName("room_no")
    val room_no: Int,
    @SerializedName("id")
    val id:Int,
    @SerializedName("contact")
    val contact: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("f_name")
    val f_name: String
)
