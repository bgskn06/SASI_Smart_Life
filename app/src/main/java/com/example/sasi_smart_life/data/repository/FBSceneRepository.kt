package com.example.sasi_smart_life.data.repository

import com.google.firebase.database.FirebaseDatabase

class FBSceneRepository {

    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference


    // ------------------------------------------------------------------
    // SAVE SCENE
    // ------------------------------------------------------------------
    fun saveScene(
        sceneId: String,
        homeId: String,
        name: String,
        ifData: Map<String, Any>,
        thenActions: List<Map<String, Any>>,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val data = mapOf(
            "homeId" to homeId,
            "name" to name,
            "if" to ifData,
            "then" to thenActions
        )

        db.child("scenes").child(sceneId)
            .setValue(data)
            .addOnSuccessListener { onComplete(true, null) }
            .addOnFailureListener { e -> onComplete(false, e.message) }
    }


    // ------------------------------------------------------------------
    // GET SCENES BY HOME ID
    // ------------------------------------------------------------------
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

}
