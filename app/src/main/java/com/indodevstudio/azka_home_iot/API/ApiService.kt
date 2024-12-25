import com.indodevstudio.azka_home_iot.Model.DataModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class CreditRequest(val value: String, val msg: String)
data class DebitRequest(val value: String, val msg: String)
data class CreditResponse(val value: String)
data class DeleteAllResponse(val status: String, val msg: String)
data class HistoryResponse(
    val no: String,
    val kredit: String,
    val debit: String,
    val keterangan: String
)




interface ApiService {
//    @POST("fire.php/credit")
//    fun postCredit(@Body request: CreditRequest): Call<Void>
//
//    @POST("fire.php/debit")
//    fun postDebit(@Body request: DebitRequest): Call<Void>
//
//    @GET("fire.php/credit")
//    fun getCredit(): Call<List<CreditResponse>>
// GET Request: Fetch all users
//    @GET("fire.php/history") // Replace with your endpoint
//    fun getTableData(): Call<TableData>
//    fun getData(): List<DataModel?>? {
//        return data
//    }
    @GET("fire.php/history")
    fun getHistory(): Call<ArrayList<HistoryResponse>>

    @GET("fire.php/last_kredit")
    fun getCredit(): Call<CreditResponse>

    // POST Request: Create a new user
    @POST("fire.php/delete_all") // Replace with your PHP endpoint
    fun deleteAllData(@Body request: Map<String, String>): Call<DeleteAllResponse>
    @POST("fire.php/credit")
    fun postCredit(@Body request: CreditRequest): Call<Map<String, String>>
    //fun postCredit(@Body request: CreditRequest): Call<Void>

    // POST Request: Update user info
    @POST("fire.php/debit")
    fun postDebit(@Body request: DebitRequest): Call<Map<String, String>>
    //fun postDebit(@Body request: DebitRequest): Call<Void>
}