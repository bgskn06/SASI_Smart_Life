package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.example.sasi_smart_life.data.models.SmartScene
import com.google.firebase.database.FirebaseDatabase

class FBSceneRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference

    val tag = "SceneRepo_SASI"

    fun addScene(scene: SmartScene, onComplete: (Boolean) -> Unit) {
        val path = "scenes/${scene.sceneId}"

        val sceneMap = mapOf(
            "sceneId" to scene.sceneId,
            "homeId" to scene.homeId,
            "name" to scene.name,
            "isActive" to scene.isActive,
            "if" to scene.ifData,
            "time" to scene.time,
            "then" to scene.thenAction
        )

        db.child(path).setValue(sceneMap)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false)
            }
    }

    fun getScenes(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        db.child("scenes").get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                onComplete(emptyList())
                return@addOnSuccessListener
            }

            val allScenes = snapshot.children.mapNotNull { childSnapshot ->
                val sceneData = childSnapshot.value as? MutableMap<String, Any>
                sceneData?.set("sceneId", childSnapshot.key ?: "")
                sceneData
            }

            val filteredList = allScenes.filter { scene ->
                val idFromData = scene["homeId"] as? String
                idFromData == homeId
            }

            onComplete(filteredList)
        }.addOnFailureListener {
            onComplete(emptyList())
        }
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
