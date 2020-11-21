package comdd.example.simplechatapp.models

import com.google.firebase.firestore.FieldValue


class UsersModel {
    var name: String? = null
    var email: String? = null
    var userId: String? = null
    var type: String? = null
    var lastSeen: Any? = ""

    constructor(name: String?, email: String?, userId: String?, type: String?) {
        this.name = name
        this.email = email
        this.userId = userId
        this.type = type
        this.lastSeen = FieldValue.serverTimestamp()
    }

    constructor() {}

}