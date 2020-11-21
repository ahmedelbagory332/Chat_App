package comdd.example.simplechatapp.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import comdd.example.simplechatapp.models.RegisterDeviceModel
import comdd.example.simplechatapp.models.UsersModel
import comdd.example.simplechatapp.netWork.ApiClient
import comdd.example.simplechatapp.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivityViewModel(private val application: Application) : ViewModel() {



    private val _signUpSuccessful = MutableLiveData<String>()
    private var firebaseAuth: FirebaseAuth  = FirebaseAuth.getInstance()
    private var db = FirebaseFirestore.getInstance()
    private var userref = db.collection("users")



    val signUpSuccessful: LiveData<String>
        get() = _signUpSuccessful


     fun signUp(firstName:String,email:String, password:String) {

         firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {task ->
                     if (!task.isSuccessful) {
                         Toast.makeText(application, "Signed up Failed", Toast.LENGTH_SHORT).show()
                     } else {
                         Toast.makeText(application, "Signed up Success, Please login", Toast.LENGTH_SHORT).show()
                         userProfile(firstName,email)
                     }

            }
         }


    private fun userProfile(firstName: String, email: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(firstName)
                .build()
            user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("TESTING", "User profile updated.")
                    FirebaseMessaging.getInstance().token.addOnSuccessListener{ instanceIdResult ->

                        ApiClient().getINSTANCE()?.registerDevice(email, instanceIdResult)?.enqueue(object : Callback<RegisterDeviceModel> {
                            override fun onResponse(call: Call<RegisterDeviceModel>, response: Response<RegisterDeviceModel>) {

                                Toast.makeText(application,response.body()!!.message,Toast.LENGTH_LONG).show()
                                _signUpSuccessful.value = "true"
                                //  finish()

                            }

                            override fun onFailure(call: Call<RegisterDeviceModel>, t: Throwable) {
                                Toast.makeText(application,t.message.toString(),Toast.LENGTH_LONG).show()
                            }
                        })
                    }
                }
            }
            val person = UsersModel(firstName, email, user.uid, "offline")
            userref.add(person)
        }
    }


    }



