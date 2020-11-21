package comdd.example.simplechatapp.netWork

import comdd.example.simplechatapp.models.NotificationModel
import comdd.example.simplechatapp.models.RegisterDeviceModel
import comdd.example.simplechatapp.models.UpdateModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiInterface {

    @FormUrlEncoded
    @POST("RegisterDevice.php")
    fun registerDevice(@Field("email") email:String, @Field("token") token:String) : Call<RegisterDeviceModel>

    @FormUrlEncoded
    @POST("updateToken.php")
    fun updateToken(@Field("email") email:String, @Field("token") token:String) : Call<UpdateModel>

    @FormUrlEncoded
    @POST("updatePeerDevice.php")
    fun updatePeerDevice(@Field("email") email:String, @Field("isPeered") token:String) : Call<UpdateModel>

    @FormUrlEncoded
    @POST("sendSinglePush.php")
    fun sendTextNotification(@Field("email") email:String,@Field("senderEmail") senderEmail:String, @Field("message") message:String, @Field("title") title:String) : Call<NotificationModel>
}