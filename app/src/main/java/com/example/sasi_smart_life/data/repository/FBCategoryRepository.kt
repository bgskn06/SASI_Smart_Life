package com.example.sasi_smart_life.data.repository

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

class FBCategoryRepository {
    private val db = FirebaseDatabase.getInstance(
        "https://sasi-smart-life-default-rtdb.asia-southeast1.firebasedatabase.app/"
    ).reference.child("deviceCategory")

    fun createCategory(
        categoryId: String,
        name: String,
        homeId: String,
        image: String = "",
        imageUrlOn: String = "",
        imageUrlOff: String = "",
        onComplete: (Boolean, String?) -> Unit
    ) {
        Log.d("CATEGORY_SASI", "Attempting to create category. Name: $name, HomeID: $homeId")
        val categoryData = mapOf(
            "name" to name,
            "homeId" to homeId,
            "image" to image,
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
        db.orderByChild("homeId").equalTo(homeId).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
//                    Log.d("CATEGORY_SASI", "No categories found for homeId: $homeId")
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                val categoryList = snapshot.children.mapNotNull { childSnapshot ->
                    val categoryData = childSnapshot.value as? MutableMap<String, Any>
                    categoryData?.set("categoryId", childSnapshot.key ?: "")
                    categoryData
                }

//                Log.d("CATEGORY_SASI", "Categories fetched: ${categoryList.size}")
                onComplete(categoryList)
            }
            .addOnFailureListener { exception ->
                Log.e("CATEGORY_SASI", "Failed to fetch categories", exception)
                onComplete(emptyList())
            }
    }
}