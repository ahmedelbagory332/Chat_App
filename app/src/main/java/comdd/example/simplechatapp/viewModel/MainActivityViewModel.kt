package comdd.example.simplechatapp.viewModel

import android.app.Application
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import comdd.example.simplechatapp.activities.UsersActivity
import comdd.example.simplechatapp.models.UpdateModel
import comdd.example.simplechatapp.netWork.ApiClient
import comdd.example.simplechatapp.utiles.UserData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivityViewModel(private val application: Application) : ViewModel() {


    private val _signInSuccessful = MutableLiveData<String>()
    private var firebaseAuth: FirebaseAuth  = FirebaseAuth.getInstance()



    val signInSuccessful: LiveData<String>
        get() = _signInSuccessful


     fun signIn(email:String, password:String) {

         firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {task ->
             Log.d("TESTING", "sign In Successful:" + task.isSuccessful)
            if (!task.isSuccessful) {
                Log.w("TESTING", "signInWithEmail:failed", task.exception)
                Toast.makeText(application, task.exception!!.message, Toast.LENGTH_LONG).show()
            } else {

                    FirebaseMessaging.getInstance().token.addOnSuccessListener { instanceIdResult ->
                        if (!UserData(application).readToken().equals(instanceIdResult)) {

                            UserData(application).writeToken(instanceIdResult)
                            ApiClient().getINSTANCE()?.updateToken(
                                FirebaseAuth.getInstance().currentUser!!.email!!, instanceIdResult)?.enqueue(object : Callback<UpdateModel> {
                                override fun onResponse(
                                    call: Call<UpdateModel>,
                                    response: Response<UpdateModel>
                                ) {

                                    //   if (response.body()!!.response == "0")Toast.makeText(applicationContext,"updated", Toast.LENGTH_LONG).show()

                                }

                                override fun onFailure(call: Call<UpdateModel>, t: Throwable) {
                                    Toast.makeText(
                                        application,
                                        t.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            })
                        }
                }

                _signInSuccessful.value = "true"
                }
            }
         }




    }



