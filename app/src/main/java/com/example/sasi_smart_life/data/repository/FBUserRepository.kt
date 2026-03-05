package com.example.sasi_smart_life.data.repository

import com.example.sasi_smart_life.data.models.User
import com.google.firebase.database.*

class FBUserRepository {

    private val db = FirebaseDatabase.getInstance("https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/").reference

    fun getUser(uid: String, callback: (User?) -> Unit) {
        db.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (!snapshot.exists()) {
                        callback(null)
                        return
                    }

                    val user = snapshot.getValue(User::class.java)
                    callback(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun saveUser(uid: String, user: User, callback: (Boolean) -> Unit) {
        db.child("users").child(uid)
            .setValue(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}
