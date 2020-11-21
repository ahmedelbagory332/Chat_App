package comdd.example.simplechatapp.adapters


import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import comdd.example.simplechatapp.R
import comdd.example.simplechatapp.models.ChatModel
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SimpleDateFormat")
class ChatAdapter(val context:Context ,options: FirestoreRecyclerOptions<ChatModel>) : FirestoreRecyclerAdapter<ChatModel, ChatAdapter.ViewHolder>(options) {

//    val MSG_TYPE_LEFT = 0
//    val MSG_TYPE_RIGHT = 1

    private lateinit var user:FirebaseAuth


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view:View= LayoutInflater.from(parent.context).inflate(R.layout.message_row, parent, false)
        user = FirebaseAuth.getInstance()

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(messageViewHolder: ViewHolder, position: Int, messages: ChatModel) {

        val messageSenderId: String = user.currentUser!!.uid

        val fromUserID: String? = messages.from
        val fromMessageType: String? = messages.type

        messageViewHolder.receiverMessageText.visibility = View.GONE
        messageViewHolder.senderMessageText.visibility = View.GONE
        messageViewHolder.messageSenderPicture.visibility = View.GONE
        messageViewHolder.messageReceiverPicture.visibility = View.GONE

        if (fromMessageType.equals("text"))
        {
                    if (fromUserID.equals(messageSenderId)) {
                        messageViewHolder.senderMessageText.visibility = View.VISIBLE
                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.senderMessageText.text = messages.message + "\n \n" + date

                        }
                    }
                    else {
                         messageViewHolder.receiverMessageText.visibility = View.VISIBLE
                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.receiverMessageText.text = messages.message + "\n \n" + date

                        }
                    }
        }else{
                    if (fromUserID.equals(messageSenderId)) {
                        messageViewHolder.senderMessageText.visibility = View.VISIBLE
                        messageViewHolder.messageSenderPicture.visibility = View.VISIBLE
                        Glide.with(context).load(messages.image).diskCacheStrategy(DiskCacheStrategy.DATA).into(messageViewHolder.messageSenderPicture)

                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.senderMessageText.text =  date

                        }
                    }
                    else {
                        messageViewHolder.receiverMessageText.visibility = View.VISIBLE
                        messageViewHolder.messageReceiverPicture.visibility = View.VISIBLE
                        Glide.with(context).load(messages.image).diskCacheStrategy(DiskCacheStrategy.DATA).into(messageViewHolder.messageReceiverPicture)

                        if (messages.timestamp!=null) {
                            val timestamp = messages.timestamp as com.google.firebase.Timestamp
                            val milliseconds = timestamp.seconds * 1000 + timestamp.nanoseconds / 1000000
                            val sdf = SimpleDateFormat("dd MMM yyyy hh:mm a")
                            val netDate = Date(milliseconds)
                            val date = sdf.format(netDate).toString()
                            messageViewHolder.receiverMessageText.text =  date

                        }
                    }
        }


    }


//    override fun getItemViewType(position: Int): Int {
//        return if (getItem(position).userid.equals(user!!.uid)) MSG_TYPE_RIGHT else MSG_TYPE_LEFT
//    }

    override fun onDataChanged() {
        // do your thing
        if (itemCount == 0) Toast.makeText(context, "No Messages", Toast.LENGTH_SHORT).show()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        val senderMessageText: TextView = itemView.findViewById(R.id.sender_messsage_text)
        val receiverMessageText: TextView =  itemView.findViewById(R.id.receiver_message_text)
        val messageReceiverPicture: ImageView = itemView.findViewById(R.id.message_receiver_image_view)
        val  messageSenderPicture: ImageView = itemView.findViewById(R.id.message_sender_image_view)

    }



}