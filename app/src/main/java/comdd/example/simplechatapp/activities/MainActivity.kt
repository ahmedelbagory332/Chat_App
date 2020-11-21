package comdd.example.simplechatapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import comdd.example.simplechatapp.*
import comdd.example.simplechatapp.viewModel.MainActivityViewModel
import comdd.example.simplechatapp.viewModel.viewModelFactory.MainActivityViewModelFactory


class MainActivity : AppCompatActivity() {

    lateinit  var username: EditText
    lateinit  var password:EditText
    private lateinit  var btLogin:Button
    private lateinit  var btSignup:Button
    lateinit  var firebaseAuth: FirebaseAuth
    lateinit var mainActivityViewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val user = FirebaseAuth.getInstance().currentUser
        if (user !=null) startActivity(Intent(this, UsersActivity::class.java))

        username = findViewById(R.id.username)
        password = findViewById(R.id.password)
        btLogin = findViewById(R.id.login)
        btSignup = findViewById(R.id.signup)
        firebaseAuth = FirebaseAuth.getInstance()

        val application = requireNotNull(this).application
        val mainActivityViewModelFactory = MainActivityViewModelFactory(application)
        mainActivityViewModel = ViewModelProvider(this,mainActivityViewModelFactory).get(MainActivityViewModel::class.java)


        btLogin.setOnClickListener{

            if (username.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Type Email", Toast.LENGTH_SHORT).show()
            }else if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString().trim()).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            }else if (password.text.toString().trim().isEmpty()) {
                Toast.makeText(this, "Type Password", Toast.LENGTH_SHORT).show()
            } else{
                mainActivityViewModel.signIn(username.text.toString().trim(),password.text.toString().trim())
            }

        }



        mainActivityViewModel.signInSuccessful.observe(this, Observer {
            if (it == "true"){

                val i = Intent(this, UsersActivity::class.java)
                startActivity(i)
                finish()
            }
        })


        btSignup.setOnClickListener {
            val i = Intent(this, SignUpActivity::class.java)
            startActivity(i)
        }
    }





}