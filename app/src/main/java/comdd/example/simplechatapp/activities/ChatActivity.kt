package comdd.example.simplechatapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import comdd.example.simplechatapp.*
import comdd.example.simplechatapp.adapters.ChatAdapter
import comdd.example.simplechatapp.models.ChatModel
import comdd.example.simplechatapp.models.LastMessageModel
import comdd.example.simplechatapp.models.NotificationModel
import comdd.example.simplechatapp.models.UpdateModel
import comdd.example.simplechatapp.netWork.ApiClient
import comdd.example.simplechatapp.utiles.FilePath
import comdd.example.simplechatapp.viewModel.ChatActivityViewModel
import comdd.example.simplechatapp.viewModel.LastMessageActivityViewModel
import comdd.example.simplechatapp.viewModel.viewModelFactory.ChatActivityViewModelFactory
import comdd.example.simplechatapp.viewModel.viewModelFactory.LastMessageActivityViewModelFactory
import id.zelory.compressor.Compressor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.Observer
class ChatActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var textsend: EditText
    private lateinit var btSend: ImageButton
    private lateinit var btSendImg: ImageButton
    lateinit var chatadapter: ChatAdapter
    lateinit var toolbar:Toolbar
    lateinit var chatActivityViewModel: ChatActivityViewModel

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        toolbar = findViewById(R.id.toolbar)
        textsend = findViewById(R.id.textsend)
        btSend = findViewById(R.id.send)
        btSendImg = findViewById(R.id.sendImg)
        recyclerView = findViewById(R.id.chatrecyclerview)
        linearLayoutManager =  LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true


        val application = requireNotNull(this).application
        val chatActivityViewModelFactory = ChatActivityViewModelFactory(application)
        chatActivityViewModel = ViewModelProvider(this,chatActivityViewModelFactory).get(ChatActivityViewModel::class.java)

        chatActivityViewModel.setReceiverID(intent.getStringExtra("receiverID").toString())
        chatActivityViewModel.setChatId(intent.getStringExtra("CHAT_ID").toString())
        chatActivityViewModel.setPeeredEmail(intent.getStringExtra("Email").toString())
        chatActivityViewModel.setName(intent.getStringExtra("Name").toString())
        chatActivityViewModel.updatePeerDevice()
        chatActivityViewModel.getMessages()
        chatActivityViewModel.getLastSeen()

        chatActivityViewModel.options.observe(this, Observer {
            chatadapter = ChatAdapter(this, it)
            chatadapter.registerAdapterDataObserver(object : AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    recyclerView.scrollToPosition(chatadapter.itemCount - 1)
                }
            })
            recyclerView.adapter = chatadapter
            recyclerView.layoutManager = linearLayoutManager
            chatadapter.startListening()
        })
        chatActivityViewModel.name.observe(this, Observer {
            toolbar.title = it

        })
        chatActivityViewModel.lastSeen.observe(this, Observer {
            toolbar.subtitle = it

        })
        chatActivityViewModel.updateType("Online",null)


        btSend.setOnClickListener{
         if (textsend.text.toString().isNotEmpty()){
             val  messageText =  textsend.text.toString()
             chatActivityViewModel.setMessageText(messageText)
             chatActivityViewModel.sendMessage()
         }
            textsend.setText("")

        }
        btSendImg.setOnClickListener{
            openFileChooser()
        }
        textsend.addTextChangedListener(object : TextWatcher {
                     override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        if (!TextUtils.isEmpty(s)) {
                            //Set the value of typing field to true.
                            chatActivityViewModel.updateType("Typing...",null)
                        } else {
                            // Set to false
                            chatActivityViewModel.updateType("Online",null)
                        }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })



    }




    private fun openFileChooser() {
        CropImage.startPickImageActivity(this)
    }

    private fun cropRequest(imageUri: Uri) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //RESULT FROM SELECTED IMAGE
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri = CropImage.getPickImageResultUri(this, data)
            cropRequest(imageUri)
        }
        //RESULT FROM CROPING ACTIVITY
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {

                chatActivityViewModel.mImageUri.value = result.uri
                //Glide.with(this).load(mImageUri).into(ImageView_profile)
                val realPath: String =
                    FilePath.getPath(this, chatActivityViewModel.mImageUri.value)
                val actualImage = File(realPath)
                try {
                    val compressedImage: Bitmap = Compressor(this).compressToBitmap(actualImage)
                    val baos = ByteArrayOutputStream()
                    compressedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                    chatActivityViewModel.finalImg.value = baos.toByteArray()
                    chatActivityViewModel.uploadFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_items, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.title.toString()) {
            "logout" -> {
                FirebaseAuth.getInstance().signOut()
                val i = Intent(this@ChatActivity, MainActivity::class.java)
                startActivity(i)
                finish()
            }
        }
        return true
    }



    override fun onStop() {
        super.onStop()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.updatePeerDevice()

    }

    override fun onDestroy() {
        super.onDestroy()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.updatePeerDevice()
    }


    override fun onPause() {
        super.onPause()
        chatActivityViewModel.updateType("Offline", FieldValue.serverTimestamp())
        chatActivityViewModel.updatePeerDevice()
    }

    override fun onResume() {
        super.onResume()
        chatActivityViewModel.updateType("Online",null)
        chatActivityViewModel.updatePeerDevice()

    }

}