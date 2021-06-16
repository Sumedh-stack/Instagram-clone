package com.chatt.android.photorateapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : AppCompatActivity() {
    var firebaseUser:FirebaseUser?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        signup_link_btn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        firebaseUser=FirebaseAuth.getInstance().currentUser
        if (firebaseUser!=null){
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        login_btn.setOnClickListener {
            loginUser()

        }
    }

    private fun loginUser() {
        val email = email_login.text.toString()
        val password = password_login.text.toString()
        when {
            TextUtils.isEmpty(email) -> Toast.makeText(this, "email is required", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "password is required", Toast.LENGTH_LONG).show()
                else -> {
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Log In")
                    progressDialog.setMessage("Please wait ,this may take a while..")
                    progressDialog.setCanceledOnTouchOutside(false)
                    progressDialog.show()
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "You are logged in successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()

                    } else {
                        val message = task.exception.toString()
                        Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                        progressDialog.dismiss()
                        mAuth.signOut() 
                    }
                }
            }
        }
    }
}