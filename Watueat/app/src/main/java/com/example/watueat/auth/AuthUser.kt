package com.example.watueat.auth

import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

// Referenced: FC8 Firebase Authentication
data class User (private val nameOfUser: String?, val uid: String) { val name: String = nameOfUser ?: "User logged out" }
// Additional function so when user is invalid
const val invalidUserUid = "-1"
fun User.isInvalid(): Boolean { return uid == invalidUserUid }
val invalidUser = User(null, invalidUserUid)

class AuthUser(private val registry: ActivityResultRegistry): DefaultLifecycleObserver, FirebaseAuth.AuthStateListener {

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var liveUser = MutableLiveData<User>().apply { this.postValue(invalidUser) }
    private var loginPending = false

    init { Firebase.auth.addAuthStateListener(this) }

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = registry.register("key", owner, FirebaseAuthUIActivityResultContract()) {
            Log.d("Authenticating User: ", "result ${it.resultCode}")
            loginPending = false
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        Log.d("Authenticating User: ", "Is user null: ${p0.currentUser == null}")
        updatingUserStatus(p0.currentUser)
    }

    fun observeUser(): LiveData<User> {
        return liveUser
    }

    fun logout() {
        if (user() != null) Firebase.auth.signOut()
    }

    private fun updatingUserStatus(firebaseUser: FirebaseUser?){
        if (firebaseUser == null) {
            liveUser.postValue(invalidUser)
            login()
        } else {
            val user = User(firebaseUser.displayName, firebaseUser.uid)
            liveUser.postValue(user)
        }
    }

    private fun login() {
        if (user() == null && !loginPending) {
            Log.d("Logging In: ", "user is null")
            loginPending = true
            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    private fun user(): FirebaseUser? {
        return Firebase.auth.currentUser
    }
}
