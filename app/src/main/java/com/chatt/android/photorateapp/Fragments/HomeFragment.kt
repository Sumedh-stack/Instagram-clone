package com.chatt.android.photorateapp.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatt.android.photorateapp.Adapter.PostAdapter
import com.chatt.android.photorateapp.Model.Post
import com.chatt.android.photorateapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var postId:String=""
    private var followinglist: MutableList<Post>? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_home, container, false)
        var recyclerView: RecyclerView? = null
        recyclerView = view?.findViewById(R.id.recycle_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManager

        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView?.adapter = postAdapter
        checkFollowings()
        return view


    }

    private fun checkFollowings() {
        followinglist = ArrayList()
        val followingRef = FirebaseDatabase.getInstance().reference.child("Follow")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Following")
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    (followinglist as ArrayList<String>).clear()
                    for (snapshot in p0.children) {
                        snapshot.key?.let { (followinglist as ArrayList<String>).add(it) }
                    }
                    retrievePosts()
                }
            }


        })
    }
    private fun retrievePosts() {
        val postRef = FirebaseDatabase.getInstance().reference.child("Posts")
        postRef.addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                postList?.clear()
                for (snapshot in p0.children)
                {
                    val post=snapshot.getValue(Post::class.java)
                    for(id in (followinglist as ArrayList<String>))
                    {
                        if(post!!.getPublisher()==id.toString())
                        {
                            postList!!.add(post)
                        }
                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }


        })
    }

}