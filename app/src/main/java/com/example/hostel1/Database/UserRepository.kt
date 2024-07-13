import android.util.Log
import com.example.hostel1.Database.AppDatabase
import com.example.hostel1.Database.User
import com.example.hostel1.Database.UserEntity
import com.example.hostel1.Integration.API_Service
import retrofit2.Response

class UserRepository(private val api: API_Service, private val db: AppDatabase) {
    suspend fun getUsersFromApi(): List<UserEntity> {
        try {
            val response: Response<List<User>> = api.getUsers()

            if (response.isSuccessful) {
                val users = response.body() ?: emptyList()

                Log.d("UserRepository", "Response successful. Users: $users")

                val userEntities = users.map { user ->
                    UserEntity(user.room_no,user.id, user.contact, user.name, user.f_name)
                }

                db.userDao().insertAll(userEntities)
                Log.d("UserRepository", "Inserted users into DB: $userEntities")

                return userEntities
            } else {
                Log.e("UserRepository", "Error fetching users: ${response.code()} - ${response.message()}")
                throw Exception("Error fetching users: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Network error: ${e.message}", e)
            throw Exception("Network error: ${e.message}")
        }
    }

    suspend fun getUsersFromDb(): List<UserEntity> {
        val usersFromDb = db.userDao().getAllUsers()
        Log.d("UserRepository", "Fetched users from DB: $usersFromDb")
        return usersFromDb
    }
}
