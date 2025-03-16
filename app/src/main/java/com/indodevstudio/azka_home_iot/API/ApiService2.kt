import com.google.gson.JsonObject
import com.indodevstudio.azka_home_iot.Model.DataModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

data class Event(
    val id: Int,
    val name: String,
    val date: String,
    val color: Int // Warna event dalam format integer
)

data class Event2(
    val id: Int,
    val name: String,
    val date: String
)

data class Event3(
    val id: Int,
    val name: String,
    val date: String,
    val email: String
)

interface ApiService2 {

    @GET("get_events_by_date.php")
    fun getEventsByDate(
        @Query("date") date: String,
        @Query("email") email: String
    ): Call<List<Event>>
    @POST("addevent.php")
    fun addEvent(@Body event: Event3): Call<ResponseBody>

    @GET("getevents.php")
    fun getAllEvents(@Query("email") email: String): Call<ArrayList<Event2>>


    @POST("deleteevent.php")
    fun deleteEvent(@Query("id") id: Int): Call<ResponseBody>

}