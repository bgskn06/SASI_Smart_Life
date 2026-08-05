package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.example.sasi_smart_life.data.models.SmartScene
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FBSceneRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).reference

    val tag = "SceneRepo_SASI"

    fun addScene(scene: SmartScene, onComplete: (Boolean) -> Unit) {
        val path = "scenes/${scene.sceneId}"

        db.child(path).setValue(scene)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false)
            }
    }

    fun updateScene(sceneId: String, updatedScene: SmartScene, onComplete: (Boolean) -> Unit) {
        val updates = mapOf(
            "name" to updatedScene.name,
            "category" to updatedScene.category,
            "ifData" to updatedScene.ifData,
            "logic" to updatedScene.logic,
            "schedule" to updatedScene.schedule,
            "thenAction" to updatedScene.thenAction
        )

        db.child("scenes")
            .child(sceneId)
            .updateChildren(updates)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getScenes(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        db.child("scenes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onComplete(emptyList())
                    return
                }
                val allScenes = snapshot.children.mapNotNull { childSnapshot ->
                    val sceneData = childSnapshot.value as? MutableMap<String, Any>
                    sceneData?.set("sceneId", childSnapshot.key ?: "")
                    sceneData
                }

                // Filter scene berdasarkan homeId
                val filteredList = allScenes.filter { scene ->
                    val idFromData = scene["homeId"] as? String
                    idFromData == homeId
                }

                Log.d(tag, "Real-time Scenes updated: ${filteredList.size} items found for home: $homeId")
                onComplete(filteredList)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e(tag, "Error database: ${error.message}")
                onComplete(emptyList())
            }
        })
    }

    fun updateSceneStatus(sceneId: String, newStatus: Boolean, onComplete: (Boolean) -> Unit){
        val path = "scenes/$sceneId/isActive"

        db.child(path).setValue(newStatus)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false)
            }
    }

    fun deleteScene(sceneId: String, onComplete: (Boolean, String?) -> Unit) {
        val path = "scenes/$sceneId"

        db.child(path).removeValue()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false, e.message)
            }
    }
}
