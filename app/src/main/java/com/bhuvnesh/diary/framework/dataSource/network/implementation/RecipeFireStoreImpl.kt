package com.bhuvnesh.diary.framework.dataSource.network.implementation

import com.bhuvnesh.diary.business.domain.model.Recipe
import com.bhuvnesh.diary.framework.dataSource.network.abstraction.RecipeFirestoreService
import com.bhuvnesh.diary.framework.dataSource.network.mappers.NetworkMapper
import com.bhuvnesh.diary.framework.dataSource.network.model.RecipeNetworkEntity
import com.bhuvnesh.diary.util.cLog
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Firestore doc refs:
 * 1. add:  https://firebase.google.com/docs/firestore/manage-data/add-data
 * 2. delete: https://firebase.google.com/docs/firestore/manage-data/delete-data
 * 3. update: https://firebase.google.com/docs/firestore/manage-data/add-data#update-data
 * 4. query: https://firebase.google.com/docs/firestore/query-data/queries
 */
@Singleton
class RecipeFirestoreServiceImpl
@Inject
constructor(
    private val firebaseAuth: FirebaseAuth, // WILL include auth in the future
    private val firestore: FirebaseFirestore,
    private val networkMapper: NetworkMapper
) : RecipeFirestoreService {

    override suspend fun insertOrUpdateRecipe(recipe: Recipe) {
        val entity = networkMapper.mapToEntity(recipe)
        entity.updated_at = Timestamp.now() // for updates
        firestore
            .collection(RECIPES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun deleteRecipe(primaryKey: String) {
        firestore
            .collection(RECIPES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)
            .document(primaryKey)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun insertDeletedRecipe(recipe: Recipe) {
        val entity = networkMapper.mapToEntity(recipe)
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)
            .document(entity.id)
            .set(entity)
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

//    override suspend fun insertDeletedRecipes(recipes: List<Recipe>) {
//        if(recipes.size > 500){
//            throw Exception("Cannot delete more than 500 recipes at a time in firestore.")
//        }
//
//        val collectionRef = firestore
//            .collection(DELETES_COLLECTION)
//            .document(USER_ID)
//            .collection(RECIPES_COLLECTION)
//
//        firestore.runBatch { batch ->
//            for(recipe in recipes){
//                val documentRef = collectionRef.document(recipe.id)
//                batch.set(documentRef, networkMapper.mapToEntity(recipe))
//            }
//        }.await()
//    }

    override suspend fun deleteDeletedRecipe(recipe: Recipe) {
        val entity = networkMapper.mapToEntity(recipe)
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)
            .document(entity.id)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    // used in testing
    override suspend fun deleteAllRecipes() {
        firestore
            .collection(RECIPES_COLLECTION)
            .document(USER_ID)
            .delete()
            .await()
        firestore
            .collection(DELETES_COLLECTION)
            .document(USER_ID)
            .delete()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
    }

    override suspend fun getDeletedRecipes(): List<Recipe> {
        return networkMapper.entityListToRecipeList(
            firestore
                .collection(DELETES_COLLECTION)
                .document(USER_ID)
                .collection(RECIPES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
                }
                .await().toObjects(RecipeNetworkEntity::class.java)
        )
    }

    override suspend fun searchRecipe(recipe: Recipe): Recipe? {
        return firestore
            .collection(RECIPES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)
            .document(recipe.id)
            .get()
            .addOnFailureListener {
                // send error reports to Firebase Crashlytics
                cLog(it.message)
            }
            .await()
            .toObject(RecipeNetworkEntity::class.java)?.let {
                networkMapper.mapFromEntity(it)
            }
    }

    override suspend fun getAllRecipes(): List<Recipe> {
        return networkMapper.entityListToRecipeList(
            firestore
                .collection(RECIPES_COLLECTION)
                .document(USER_ID)
                .collection(RECIPES_COLLECTION)
                .get()
                .addOnFailureListener {
                    // send error reports to Firebase Crashlytics
                    cLog(it.message)
                }
                .await()
                .toObjects(RecipeNetworkEntity::class.java)
        )
    }

    override suspend fun insertOrUpdateRecipes(recipes: List<Recipe>) {

        if (recipes.size > 500) {
            throw Exception("Cannot insert more than 500 recipes at a time into firestore.")
        }

        val collectionRef = firestore
            .collection(RECIPES_COLLECTION)
            .document(USER_ID)
            .collection(RECIPES_COLLECTION)

        firestore.runBatch { batch ->
            for (recipe in recipes) {
                val entity = networkMapper.mapToEntity(recipe)
                entity.updated_at = Timestamp.now()
                val documentRef = collectionRef.document(recipe.id)
                batch.set(documentRef, entity)
            }
        }.addOnFailureListener {
            // send error reports to Firebase Crashlytics
            cLog(it.message)
        }.await()

    }

    companion object {
        const val RECIPES_COLLECTION = "recipes"
        const val USERS_COLLECTION = "users"
        const val DELETES_COLLECTION = "deletes"
        const val USER_ID =
            "XXXXXXXXXXXXXXXXXXXXXXXXXXX" // TODO hardcoded for single user..replace with your firebase ID
    }


}

