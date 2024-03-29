package com.chatt.android.photorateapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_setting.*
import kotlinx.android.synthetic.main.activity_account_setting.full_name_profile_frag
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlinx.android.synthetic.main.fragment_profile.*

class AddPostActivity : AppCompatActivity() {
    private var myUrl =""
    private var imageUri: Uri?=null
    private var storagePostPicRef: StorageReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        storagePostPicRef= FirebaseStorage.getInstance().reference.child("Posts Pictures")
        save_new_post_btn.setOnClickListener{
            uploadimage()

        }
        CropImage.activity()
            .setAspectRatio(2,1)
            .start(this@AddPostActivity)
    }

    private fun uploadimage() {
when{
    imageUri==null->  Toast.makeText(applicationContext,"Please select image first", Toast.LENGTH_SHORT).show()
    description_post.text.toString()=="" -> {
        Toast.makeText(applicationContext,"Please write the fullname first",Toast.LENGTH_SHORT).show()
    }
    else->{
        val progressDialog= ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please wait ,adding your picture post ...")
        progressDialog.show()
        val fileRef=storagePostPicRef!!.child(System.currentTimeMillis().toString()+" .jpg")
        var uploadTask: StorageTask<*>
        uploadTask=fileRef.putFile(imageUri!!)
        uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task->
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
                val ref= FirebaseDatabase.getInstance().reference.child("Posts")
                val postId=ref.push().key
                val postMap=HashMap<String,Any>()
                postMap["postid"]=postId!!
                postMap["description"]=description_post.getText().toString().toLowerCase()
                postMap["publisher"]=FirebaseAuth.getInstance().currentUser!!.uid
                postMap["postimage"]=myUrl
                ref.child(postId).updateChildren(postMap)
                Toast.makeText(this,"Post Uploaded  successfully",Toast.LENGTH_LONG).show()
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
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE&&resultCode== Activity.RESULT_OK&&data!=null)
        {
            val result=CropImage.getActivityResult(data)
            imageUri=result.uri
            image_post.setImageURI(imageUri)
        }
    }
}