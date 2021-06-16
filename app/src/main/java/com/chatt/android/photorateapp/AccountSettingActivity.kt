package com.chatt.android.photorateapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.UriMatcher
import com.chatt.android.photorateapp.Model.User
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.fragment_profile.full_name_profile_frag


class AccountSettingActivity : AppCompatActivity() {
    private var checker =""
    private var myUrl =""
    private var imageUri: Uri?=null
    private var storageProfilePicRef:StorageReference?=null
    private lateinit var firebaseUser: FirebaseUser
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setting)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef=FirebaseStorage.getInstance().reference.child("Profile Pictures")
        userInfo()

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent= Intent(this,SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        change_image_text_btn.setOnClickListener {
            checker="clicked"
            CropImage.activity()
                .setAspectRatio(1,1)
                .start(this)


        }
        save_info_profile_btn.setOnClickListener {
            if(checker=="clicked")
            {
                uploadImageandUpdateInfo()
            }
            else{
                updateUserInfoOnly()
            }


        }
    }
    private fun uploadImageandUpdateInfo() {

        when {
            imageUri==null->  Toast.makeText(applicationContext,"Please select image first",Toast.LENGTH_SHORT).show()

            username_profile_frag.text.toString().toLowerCase()=="" -> {
                Toast.makeText(applicationContext,"Please write the username first",Toast.LENGTH_SHORT).show()
            }
            bio_profile_frag2.text.toString().toLowerCase()=="" -> {
                Toast.makeText(applicationContext,"Please write the bio first",Toast.LENGTH_SHORT).show()
            }
            else->{
                val progressDialog= ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait ,we are updating your profile...")
                progressDialog.show()
                val fileRef=storageProfilePicRef!!.child(firebaseUser.uid+" .jpg")
                var uploadTask:StorageTask<*>
                uploadTask=fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot,Task<Uri>> { task->
                    if(!task.isSuccessful)
                    {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl


                }).addOnCompleteListener ( OnCompleteListener<Uri>{task->
                    if(task.isSuccessful)
                    {
                        val downloadUrl=task.result
                        myUrl=downloadUrl.toString()
                        val ref=FirebaseDatabase.getInstance().reference.child("Users")
                        val userMap=HashMap<String,Any>()
                        userMap["fullname"]=full_name_profile_frag.text.toString().toLowerCase()
                        userMap["username"]=username_profile_frag.text.toString().toLowerCase()
                        userMap["bio"]=bio_profile_frag2.text.toString().toLowerCase()
                        userMap["image"]=myUrl
                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(this,"Account information has been updated successfully",Toast.LENGTH_LONG).show()
                        val intent= Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()
                    }
                    else{
                        progressDialog.dismiss()
                    }
                } )

            }

        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode==Activity.RESULT_OK&&data!=null)
        {
            val result=CropImage.getActivityResult(data)
            imageUri=result.uri
            profile_image_view_profile_frag.setImageURI(imageUri)
        }
        else{

        }
    }
    private fun updateUserInfoOnly() {
        when {
            full_name_profile_frag.getText().toString().toLowerCase()=="" -> {
                Toast.makeText(applicationContext,"Please write the fullname first",Toast.LENGTH_SHORT)
            }
            username_profile_frag.getText().toString().toLowerCase()=="" -> {
                Toast.makeText(applicationContext,"Please write the username first",Toast.LENGTH_SHORT)
            }
            bio_profile_frag2.getText().toString().toLowerCase()=="" -> {
                Toast.makeText(applicationContext,"Please write the bio first",Toast.LENGTH_SHORT)
            }
            else -> {
                val userRef= FirebaseDatabase.getInstance().reference.child("Users")
                val userMap=HashMap<String,Any>()
                userMap["fullname"]=full_name_profile_frag.getText().toString().toLowerCase()
                userMap["username"]=username_profile_frag.getText().toString().toLowerCase()
                userMap["bio"]=bio_profile_frag2.getText().toString().toLowerCase()
                userRef.child(firebaseUser.uid).updateChildren(userMap)
                Toast.makeText(this,"Account information has been updated successfully",Toast.LENGTH_LONG).show()
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

    }
    private fun userInfo() {
        val userRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if(p0.exists())
                {
                    val user=p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_profile_frag)
                    username_profile_frag.setText(user!!.getUsername())
                    full_name_profile_frag.text = user!!.getFullname()
                    bio_profile_frag2.setText(user!!.getBio())
                }
            }

        })
    }










}