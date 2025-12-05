package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.example.sasi_smart_life.data.models.DeviceCategory
import com.google.firebase.database.FirebaseDatabase

class FBCategoryRepository {
    private val db = FirebaseDatabase.getInstance(
        "https://iot-control-aee03-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).reference.child("deviceCategory")

    fun createCategory(
        categoryId: String,
        name: String,
        homeId: String,
        imageUrlOn: String = "",
        imageUrlOff: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        Log.d("CATEGORY_SASI", "Attempting to create category. Name: $name, HomeID: $homeId")
        val categoryData = mapOf(
            "name" to name,
            "homeId" to homeId,
            "imageUrlOn" to imageUrlOn,
            "imageUrlOff" to imageUrlOff
        )

        db.child(categoryId).setValue(categoryData)
            .addOnSuccessListener {
                Log.d("CATEGORY_SASI", "Successfully created category with ID: $categoryId")
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("CATEGORY_SASI", "Failed to create category. Error: ${e.message}", e)
                onComplete(false, e.message)
            }
    }

    fun getCategories(homeId: String, onComplete: (List<Map<String, Any>>) -> Unit) {
        // Mengambil seluruh node /rooms untuk debugging
        db.get()
            .addOnSuccessListener { snapshot ->
                Log.d("CATEGORY_SASI", "Snapshot received. Exists: ${snapshot.exists()}. Children count: ${snapshot.childrenCount}")

                if (!snapshot.exists()) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                // Filter manual di sisi aplikasi
                val allCategories = snapshot.children.mapNotNull { childSnapshot ->
                    val categoryData = childSnapshot.value as? MutableMap<String, Any>
                    categoryData?.set("categoryId", childSnapshot.key ?: "")
                    categoryData
                }

                Log.d("CATEGORY_SASI", "Total categories fetched: ${allCategories.size}. Now filtering for homeId: $homeId")

                val filteredList = allCategories.filter { category ->
                    val idFromData = category["homeId"] as? String
                    // Log perbandingan untuk setiap item
                    // Log.d("CATEGORY_SASI", "Comparing db.homeId:'${idFromData}' with active.homeId:'${homeId}'")
                    idFromData == homeId
                }

                Log.d("CATEGORY_SASI", "Filtered list count: ${filteredList.size}")
                onComplete(filteredList)
            }
            .addOnFailureListener { exception ->
                Log.e("CATEGORY_SASI", "Failed to read categories node", exception)
                onComplete(emptyList())
            }
    }
}