package comdd.example.simplechatapp.models

import androidx.annotation.Keep
import com.google.firebase.firestore.FieldValue

@Keep
class ChatModel  {

    var from: String? = null
    var to: String? = null
    var sender: String? = null
    var message: String? = null
    var image: String? = null
    var type: String? = null
    var timestamp: Any? = ""


    constructor(from: String?,to: String?, sender: String?, message: String?, image: String?, type: String?) {
        this.from = from
        this.to = to
        this.sender = sender
        this.message = message
        this.image = image
        this.type = type
        this.timestamp = FieldValue.serverTimestamp()

    }

    constructor() {}



}