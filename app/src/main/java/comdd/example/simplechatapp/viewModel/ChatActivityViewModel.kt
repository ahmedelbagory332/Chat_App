package comdd.example.simplechatapp.viewModel

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import comdd.example.simplechatapp.models.*
import comdd.example.simplechatapp.netWork.ApiClient
import comdd.example.simplechatapp.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class ChatActivityViewModel(private val application: Application) : ViewModel() {



    private val _options = MutableLiveData<FirestoreRecyclerOptions<ChatModel>>()
    private val _chatId = MutableLiveData<String>()
    private val _peeredEmail = MutableLiveData<String>()
    private val _name = MutableLiveData<String>()
    private val _receiverID = MutableLiveData<String>()
    private val _lastSeen = MutableLiveData<String>()
    private val _messageText = MutableLiveData<String>()

     var mImageUri = MutableLiveData<Uri>()
     var finalImg  = MutableLiveData<ByteArray>()

    private val user = FirebaseAuth.getInstance().currentUser
    private var mStorageRef: StorageReference  = FirebaseStorage.getInstance().getReference("Images")
    private var db = FirebaseFirestore.getInstance()



    val options: LiveData<FirestoreRecyclerOptions<ChatModel>>
        get() = _options

    val chatId: LiveData<String>
        get() = _chatId

    val peeredEmail: LiveData<String>
        get() = _peeredEmail

    val name: LiveData<String>
        get() = _name

    val receiverID: LiveData<String>
        get() = _receiverID

    val lastSeen: LiveData<String>
        get() = _lastSeen

    val messageText: LiveData<String>
        get() = _messageText



    fun getMessages(){
         val query: Query = FirebaseFirestore.getInstance().collection("chat").document(chatId.value!!).collection(chatId.value!!).orderBy("timestamp", Query.Direction.ASCENDING)
         _options.value = FirestoreRecyclerOptions.Builder<ChatModel>().setQuery(query, ChatModel::class.java).build()
     }

     fun setChatId(chatId:String){
         _chatId.value = chatId
     }

    fun setPeeredEmail(peeredEmail:String){
        _peeredEmail.value = peeredEmail
    }

    fun setName(name:String){
        _name.value = name
    }

    fun setReceiverID(receiverID:String){
        _receiverID.value = receiverID
    }

    fun setMessageText(messageText:String){
        _messageText.value = messageText
    }

    fun updatePeerDevice(){
        ApiClient().getINSTANCE()?.updatePeerDevice(FirebaseAuth.getInstance().currentUser!!.email!!,peeredEmail.value!!)?.enqueue(object : Callback<UpdateModel> {
            override fun onResponse(call: Call<UpdateModel>, response: Response<UpdateModel>) {

                // if (response.body()!!.response == "0")Toast.makeText(applicationContext,"updated", Toast.LENGTH_LONG).show()

            }

            override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                Toast.makeText(application,t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })

    }


    private fun getFileExtension(uri: Uri): String? {
        val cR = application.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri))
    }


    fun uploadFile() {

        if (mImageUri.value != null) {
            val fileReference: StorageReference = mStorageRef.child(
                System.currentTimeMillis()
                    .toString() + "." + getFileExtension(mImageUri.value!!)
            )
            val uploadTask: UploadTask = fileReference.putBytes(finalImg.value!!)
            uploadTask.addOnSuccessListener {
                fileReference.downloadUrl.addOnSuccessListener {

                    val chatref = db.collection("chat").document(chatId.value!!).collection(chatId.value!!)
                    val chat = ChatModel(
                        user!!.uid,
                        receiverID.value!!,
                        user.displayName,
                        "",
                        it.toString(),
                        "image"
                    )
                    ApiClient().getINSTANCE()?.sendTextNotification(peeredEmail.value!!,user.email!!, "$name send you image","Bego Chat")?.enqueue(object : Callback<NotificationModel> {
                        override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                        }

                        override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                           // Toast.makeText(application,t.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    })

                    chatref.add(chat)
                    updateLastMessage("image")

                }
            }
                .addOnFailureListener { e ->
                    Toast.makeText(application, e.message, Toast.LENGTH_LONG).show()

                    Toast.makeText(application, "حدث خطا ما", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(application, "لم يتم اختيار صورة", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateLastMessage(messageText: String ) {
        val lastMessagesRef = db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!)
        val docRef: Query = db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!).whereEqualTo("chatId",chatId.value!!)
        docRef.get().addOnSuccessListener { documents ->
            val list: MutableList<String> = ArrayList()
            for (document in documents) {

                list.add(document.id)
            }

            if(list.size==0){
                val lastMessageModel =
                    LastMessageModel(
                        chatId.value!!,
                        user!!.uid,
                        user.displayName,
                        user.email,
                        messageText
                    )
                lastMessagesRef.add(lastMessageModel)

            }
            else{

                for (id in list) {
                    db.collection("lastMessages").document(receiverID.value!!).collection(receiverID.value!!).document(id)
                        .update("message",messageText)
                        .addOnSuccessListener {
                            Log.d("ChatActivity", " Updated!")

                        }

                }

            }
        }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }

    }

    @SuppressLint("SimpleDateFormat")
    fun getLastSeen(){
        db.collection("users").whereEqualTo("userId",receiverID.value!!).addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }

            for (document in value!!) {
                 if (document.data["lastSeen"]!=null&& document.data["type"].toString() == "Offline") {
                    val timestamp = document.data["lastSeen"] as com.google.firebase.Timestamp
                    val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                    val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                    val netDate = Date(milliseconds)
                    val date = sdf.format(netDate).toString()

                     _lastSeen.value = date

                }

            }
        }

    }

    fun sendMessage(){
        val chatRef = db.collection("chat").document(chatId.value!!).collection(chatId.value!!)
        val chat = ChatModel(user!!.uid, receiverID.value!!, user.displayName, messageText.value!!, "", "text")
        chatRef.add(chat)
        /////

        updateLastMessage(messageText.value!!)

        /////
        ApiClient().getINSTANCE()?.sendTextNotification(peeredEmail.value!!,user.email!!,user.displayName!!+" : "+messageText.value!!,"Bego Chat")?.enqueue(object : Callback<NotificationModel> {
            override fun onResponse(call: Call<NotificationModel>, response: Response<NotificationModel>) {
                //Toast.makeText(applicationContext,response.body()!!.success, Toast.LENGTH_LONG).show()

            }

            override fun onFailure(call: Call<NotificationModel>, t: Throwable) {
                // Toast.makeText(applicationContext,t.message.toString(), Toast.LENGTH_LONG).show()
            }
        })
    }

      fun updateType(offlineType:String, dateType: FieldValue?){
        val docRef: Query = db.collection("users").whereEqualTo("userId",FirebaseAuth.getInstance().currentUser!!.uid)
        docRef.get().addOnSuccessListener { documents ->
            val list: MutableList<String> = ArrayList()
            for (document in documents) {

                list.add(document.id)
            }
            if (dateType == null){
                for (id in list) {
                    db.collection("users").document(id).update("type", offlineType)
                        .addOnSuccessListener { Log.d("ChatActivity", "type Updated!") }
                }
            }
            else{
                for (id in list) {
                    db.collection("users").document(id).update("type", offlineType)
                        .addOnSuccessListener { Log.d("ChatActivity", "type Updated!") }
                    db.collection("users").document(id).update("lastSeen", dateType)
                        .addOnSuccessListener { Log.d("ChatActivity", "type Updated!") }
                }
            }

        }
            .addOnFailureListener { exception ->
                Toast.makeText(application, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }
    }

}



