import com.google.gson.JsonObject
import com.indodevstudio.azka_home_iot.Model.DataModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

data class CreditRequest(val value: String, val msg: String, val added_by: String)
data class DebitRequest(val value: String, val msg: String, val added_by: String)
data class CreditResponse(val value: String)
data class DeleteAllResponse(val status: String, val msg: String)
data class HistoryResponse(
    val no: String,
    val kredit: String,
    val debit: String,
    val keterangan: String,
    val by: String
)

data class DailyGet(
    val daily: String
)

data class MonthlyGet(
    val monthly: String
)

data class UploadResponse(
    val status: String, // e.g., "success" or "error"
    val message: String, // e.g., "File uploaded successfully"
    val filePath: String? // e.g., "uploads/unique_file_name.jpg" (optional, if success)
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
//@GET
//suspend fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
//
//    @GET("generate_pdf.php/k1")
//    suspend fun generatePdf(): Response<JsonObject>
//    @GET("generate_pdf.php/k2")
//    suspend fun generatePdf2(): Response<JsonObject>
//    @GET("generate_pdf.php/k3")
//    suspend fun generatePdf3(): Response<JsonObject>
//    @GET("generate_pdf.php/k4")
//    suspend fun generatePdf4(): Response<JsonObject>
//    @GET("generate_pdf.php/k5")
//    suspend fun generatePdf5(): Response<JsonObject>
//    @GET("generate_pdf.php/k6")
//    suspend fun generatePdf6(): Response<JsonObject>

    @Multipart
    @POST("fire.php/photo")
    fun uploadPhoto(@Part photo: MultipartBody.Part): Call<Void>


    @Multipart
    @POST("fire.php/photo2")
    fun uploadPhoto2(@Part photo: MultipartBody.Part): Call<Void>
    @GET("fire.php/daily")
    fun getDaily(): Call<DailyGet>
    @GET("fire.php/monthly")
    fun getMontly(): Call<MonthlyGet>

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