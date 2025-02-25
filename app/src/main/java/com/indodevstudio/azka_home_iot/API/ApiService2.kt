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
interface ApiService2 {

    @GET("get_events_by_date.php") // Sesuaikan dengan API-mu
    fun getEventsByDate(date: String): ArrayList<Event> // Harus List, bukan LiveData atau nullable
    @POST("addevent.php")
    fun addEvent(@Body event: Event): Call<ResponseBody>

    @GET("getevents.php")
    fun getEvents(): Call<ArrayList<Event>>

    @POST("deleteevent.php")
    fun deleteEvent(@Query("id") id: Int): Call<ResponseBody>

}