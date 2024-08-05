package com.example.watueat.db

import android.util.Log
import com.example.watueat.data.Restaurant
import com.google.firebase.firestore.FirebaseFirestore

class DatabaseHelper {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rootCollection = "users"
    private val subCollection = "restaurants"

    // Referenced: https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
    fun createRestaurantMeta(restaurant: Restaurant, resultListener: (List<Restaurant>) -> Unit) {
        db.collection(rootCollection)
            .document(restaurant.ownerUid)
            .collection(subCollection)
            .add(restaurant)
            .addOnSuccessListener {
                Log.d("Creating Restaurant metadata: ", "Restaurant ${restaurant.uuid} was added successfully")
                fetchRestaurantMeta(restaurant.ownerUid) { resultListener.invoke(it) }
            }
            .addOnFailureListener {
                Log.d("Creating Restaurant metadata failed: ", "Restaurant ${restaurant.uuid} was NOT added. The following error occurred: $it")
            }
    }

    // Referenced: https://firebase.google.com/docs/firestore/manage-data/delete-data#delete_documents
    fun removeRestaurantMeta(restaurant: Restaurant, resultListener: (List<Restaurant>) -> Unit) {
        db.collection(rootCollection)
            .document(restaurant.ownerUid)
            .collection(subCollection)
            .document(restaurant.firestoreId)
            .delete()
            .addOnSuccessListener {
                Log.d("Deleting Restaurant metadata: ", "Restaurant ${restaurant.uuid} was deleted successfully")
                fetchRestaurantMeta(restaurant.ownerUid) { resultListener.invoke(it) }
            }
            .addOnFailureListener {
                Log.d("Deleting Restaurant metadata failed: ", "Restaurant ${restaurant.uuid} was NOT deleted. The following error occurred: $it")
            }
    }

    fun updateCommentsRestaurantMeta(restaurant: Restaurant, resultListener: (List<Restaurant>) -> Unit) {
        db.collection(rootCollection)
            .document(restaurant.ownerUid)
            .collection(subCollection)
            .document(restaurant.firestoreId)
            .update("comments", restaurant.comments)
            .addOnSuccessListener {
                Log.d("Updating comments for Restaurant metadata: ", "Restaurant ${restaurant.uuid} comments were updated successfully")
                fetchRestaurantMeta(restaurant.ownerUid) { resultListener.invoke(it) }
            }
            .addOnFailureListener {
                Log.d("Updating comments for Restaurant metadata failed: ", "Restaurant ${restaurant.uuid} comments were NOT updated. The following error occurred: $it")
            }
    }

    fun fetchRestaurantMeta(ownerUid: String, resultListener: (List<Restaurant>) -> Unit) {
        val query = db.collection(rootCollection).document(ownerUid).collection(subCollection)
        query.get()
            .addOnSuccessListener { result ->
                Log.d("Fetching Restaurant metadata: ", "${result!!.documents.size} favorite restaurants fetched")
                resultListener(result.documents.mapNotNull { it.toObject(Restaurant::class.java) })
            }
            .addOnFailureListener { exception ->
                Log.d("Fetching Restaurant metadata: ", "Failed to fetch favorite restaurants. The following error occurred: $exception")
            }

    }

    // Referenced: https://firebase.google.com/docs/firestore/query-data/listen
    fun listenForRestaurantMeta(ownerUid: String, resultListener: (List<Restaurant>) -> Unit) {
        val query = db.collection(rootCollection).document(ownerUid).collection(subCollection)
        query.addSnapshotListener { result, exception ->
            if (exception != null) { return@addSnapshotListener }
            resultListener(result!!.documents.mapNotNull { it.toObject(Restaurant::class.java) })
        }
    }

    fun listenForAllUsersRestaurantMeta(resultListener: (List<Restaurant>) -> Unit) {
        db.collectionGroup(subCollection)
            .addSnapshotListener { result, exception ->
                if (exception != null) {
                    return@addSnapshotListener
                }
                Log.d("Fetching All Users Restaurant metadata: ", "${result!!.documents.size} total favorite restaurants fetched from all users")
                resultListener(result!!.documents.mapNotNull { it.toObject(Restaurant::class.java) })
            }
    }
}
