package com.project.umlindi.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.project.umlindi.R
import com.project.umlindi.util.Validators
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val callbackManager = CallbackManager.Factory.create()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnGoogle = view.findViewById<Button>(R.id.btnGoogle)
        val btnFacebook = view.findViewById<Button>(R.id.btnFacebook)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        LoginManager.getInstance().registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    Log.d(TAG, "Facebook login succeeded")
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    signInToFirebase(credential, getString(R.string.error_facebook_failed))
                }

                override fun onCancel() {
                    Log.d(TAG, "Facebook login cancelled by user")
                    setLoading(false)
                }

                override fun onError(error: FacebookException) {
                    Log.e(TAG, "Facebook login failed", error)
                    setLoading(false)
                    showError(getString(R.string.error_facebook_failed))
                }
            }
        )

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        btnGoogle.setOnClickListener { signInWithGoogle() }
        btnFacebook.setOnClickListener { signInWithFacebook() }

        btnLogin.setOnClickListener {
            hideError()
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()

            if (!Validators.isValidEmail(email)) {
                showError(getString(R.string.error_invalid_email))
                return@setOnClickListener
            }
            if (!Validators.isValidPassword(password)) {
                showError(getString(R.string.error_short_password))
                return@setOnClickListener
            }

            setLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Email login succeeded")
                        goHome()
                    } else {
                        Log.e(TAG, "Email login failed", task.exception)
                        setLoading(false)
                        showError(task.exception?.localizedMessage ?: getString(R.string.error_login_failed))
                    }
                }
        }
    }

    private fun signInWithFacebook() {
        hideError()
        setLoading(true)
        LoginManager.getInstance().logIn(
            requireActivity() as ActivityResultRegistryOwner,
            callbackManager,
            listOf("email", "public_profile")
        )
    }

    private fun signInWithGoogle() {
        hideError()
        setLoading(true)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val activity = requireActivity()
                val result = CredentialManager.create(activity).getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    signInToFirebase(
                        GoogleAuthProvider.getCredential(idToken, null),
                        getString(R.string.error_google_failed)
                    )
                } else {
                    Log.e(TAG, "Unexpected credential type: ${credential.type}")
                    setLoading(false)
                    showError(getString(R.string.error_google_failed))
                }
            } catch (e: GetCredentialCancellationException) {
                Log.d(TAG, "Google sign-in cancelled by user")
                setLoading(false)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google sign-in failed", e)
                setLoading(false)
                showError(getString(R.string.error_google_failed))
            }
        }
    }

    private fun signInToFirebase(credential: AuthCredential, failureMessage: String) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                val user = auth.currentUser
                if (task.isSuccessful && user != null) {
                    Log.d(TAG, "Firebase sign-in with social credential succeeded")
                    ensureProfile(user)
                } else {
                    Log.e(TAG, "Firebase sign-in with social credential failed", task.exception)
                    setLoading(false)
                    val message = if (task.exception is FirebaseAuthUserCollisionException) {
                        getString(R.string.error_account_exists)
                    } else {
                        failureMessage
                    }
                    showError(message)
                }
            }
    }

    private fun ensureProfile(user: FirebaseUser) {
        val ref = firestore.collection("users").document(user.uid)
        ref.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    goHome()
                } else {
                    val profile = hashMapOf<String, Any?>(
                        "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: ""),
                        "email" to (user.email ?: ""),
                        "homeLocation" to null,
                        "alertRadiusKm" to 5.0,
                        "preferredLanguage" to "en",
                        "trustScore" to 0,
                        "createdAt" to Timestamp.now()
                    )
                    ref.set(profile)
                        .addOnSuccessListener {
                            Log.d(TAG, "Profile created for new social user")
                            goHome()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Profile creation failed", e)
                            setLoading(false)
                            showError(getString(R.string.error_login_failed))
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Profile lookup failed", e)
                setLoading(false)
                showError(getString(R.string.error_login_failed))
            }
    }

    private fun goHome() {
        setLoading(false)
        findNavController().navigate(R.id.action_login_to_home)
    }

    private fun setLoading(loading: Boolean) {
        view?.findViewById<ProgressBar>(R.id.progressBar)?.visibility =
            if (loading) View.VISIBLE else View.GONE
        view?.findViewById<Button>(R.id.btnLogin)?.isEnabled = !loading
        view?.findViewById<Button>(R.id.btnGoogle)?.isEnabled = !loading
        view?.findViewById<Button>(R.id.btnFacebook)?.isEnabled = !loading
    }

    private fun showError(message: String) {
        view?.findViewById<TextView>(R.id.tvError)?.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun hideError() {
        view?.findViewById<TextView>(R.id.tvError)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        LoginManager.getInstance().unregisterCallback(callbackManager)
        super.onDestroyView()
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}