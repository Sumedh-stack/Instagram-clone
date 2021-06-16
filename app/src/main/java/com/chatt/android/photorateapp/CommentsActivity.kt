package com.chatt.android.photorateapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatt.android.photorateapp.Adapter.CommentsAdapter
import com.chatt.android.photorateapp.Model.Comment
import com.chatt.android.photorateapp.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comments.*


class CommentsActivity : AppCompatActivity() {
    private var postId=""
    private var publisherId=""
    private  var firebaseUser: FirebaseUser?=null
    private var commentAdapter: CommentsAdapter?=null
    private var commentList:MutableList<Comment>?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        val intent=intent
        postId=intent.getStringExtra("postId")
        publisherId=intent.getStringExtra("publisherId")

        var recyclerView=findViewById<RecyclerView>(R.id.recycler_view_comments)
        val linearLayoutManager=LinearLayoutManager(this)
        linearLayoutManager.reverseLayout=true
        recyclerView.layoutManager=linearLayoutManager
        commentList=ArrayList()
        commentAdapter= CommentsAdapter(this,commentList)
        recyclerView.adapter=commentAdapter
        userInfo()
        readComments()
        getPostImage()
        post_comment.setOnClickListener(View.OnClickListener {
            if(add_comment!!.text.toString()=="")
            {
                Toast.makeText(this@CommentsActivity,"Please write comment first", Toast.LENGTH_SHORT).show()
            }
            else{
                addComment()

            }


        })





    }
    private fun readComments()
    {
        val commentsRef=FirebaseDatabase.getInstance().reference.child("Comments").child(postId)
        commentsRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    commentList!!.clear()
                    for(snapshot in p0.children)
                    {
                        val comment=snapshot.getValue(com.chatt.android.photorateapp.Model.Comment::class.java)
                        commentList!!.add(comment!!)
                    }
                    commentAdapter!!.notifyDataSetChanged()
                }
            }


        })
    }
    private fun addComment() {
        val commentsRef= FirebaseDatabase.getInstance().reference.child("Comments").child(postId!!)
        val commentsMap=HashMap<String,Any>()//any means string image int any thing
        commentsMap["comment"]=add_comment!!.text.toString()
        commentsMap["publisher"]=firebaseUser!!.uid
        commentsRef.push().setValue(commentsMap)
        add_comment!!.text.clear()
    }
    private fun getPostImage() {
        val postRef= FirebaseDatabase.getInstance().reference.child("Posts").child(postId!!).child("postimage")
        postRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if(p0.exists())
                {
                    val image=p0.value.toString()
                    Picasso.get().load(image).placeholder(R.drawable.profile).into(post_image_comment)

                }
            }

        })
    }
    private fun userInfo() {
        val userRef= FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                if(p0.exists())
                {
                    val user=p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_comment)

                }
            }

        })
    }
}