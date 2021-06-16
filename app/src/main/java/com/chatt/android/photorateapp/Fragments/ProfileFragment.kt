package com.chatt.android.photorateapp.Fragments
    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import androidx.fragment.app.Fragment
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageButton
    import androidx.recyclerview.widget.GridLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.chatt.android.photorateapp.AccountSettingActivity
    import com.chatt.android.photorateapp.Adapter.MyImagesAdapter
    import com.chatt.android.photorateapp.Model.Post
    import com.chatt.android.photorateapp.Model.User
    import com.chatt.android.photorateapp.R
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.auth.FirebaseUser
    import com.google.firebase.database.DataSnapshot
    import com.google.firebase.database.DatabaseError
    import com.google.firebase.database.FirebaseDatabase
    import com.google.firebase.database.ValueEventListener
    import com.squareup.picasso.Picasso
    import kotlinx.android.synthetic.main.fragment_profile.*
    import kotlinx.android.synthetic.main.fragment_profile.view.*
    import java.util.*
    import kotlin.collections.ArrayList


    class ProfileFragment : Fragment() {
        private lateinit var profileId: String
        private lateinit var firebaseUser: FirebaseUser
        var myImagesAdapter: MyImagesAdapter?=null
        var myImagesAdapterSavedImage:MyImagesAdapter?=null
        var postList:List<Post>?=null
        var postListSaved:List<Post>?=null
        var mySavesImg:List<String>?=null
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            // Inflate the layout for this fragment
            val view = inflater.inflate(R.layout.fragment_profile, container, false)

            firebaseUser = FirebaseAuth.getInstance().currentUser!!


            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            if (pref != null)
            {
                this.profileId = pref.getString("profileId", "none").toString()
            }


            if (profileId == firebaseUser.uid)
            {
                view.edit_account_settings_btn.text = "Edit Profile"
            }
            else if (profileId != firebaseUser.uid)
            {
                checkFollowAndFollowingButtonStatus()
            }
            var recyclerViewUploadedImages:RecyclerView
            recyclerViewUploadedImages  =view.findViewById(R.id.recycler_view_upload_pic)
            recyclerViewUploadedImages.setHasFixedSize(true)
            val linearLayoutManager=GridLayoutManager(context,3)
            recyclerViewUploadedImages.layoutManager=linearLayoutManager
            postList=ArrayList()

            myImagesAdapter=context?.let { MyImagesAdapter( it,postList as ArrayList ) }
            recyclerViewUploadedImages.adapter=myImagesAdapter













            var recyclerViewSavedImages:RecyclerView
            recyclerViewSavedImages  =view.findViewById(R.id.recycler_view_saved_pic)
            recyclerViewSavedImages.setHasFixedSize(true)
            val linearLayoutManager2=GridLayoutManager(context,3)
            recyclerViewSavedImages.layoutManager=linearLayoutManager2
            postListSaved=ArrayList()

            myImagesAdapterSavedImage=context?.let { MyImagesAdapter( it,postListSaved as ArrayList ) }
            recyclerViewSavedImages.adapter=myImagesAdapterSavedImage

            recyclerViewSavedImages.visibility=View.GONE
            recyclerViewUploadedImages.visibility=View.VISIBLE
            val uploadImagesbtn:ImageButton
            uploadImagesbtn=view.findViewById(R.id.images_grid_view_btn)
            uploadImagesbtn.setOnClickListener{
                recyclerViewSavedImages.visibility=View.GONE
                recyclerViewUploadedImages.visibility=View.VISIBLE
            }
            val saveImagesbtn:ImageButton
            saveImagesbtn=view.findViewById(R.id.images_save_btn)
            saveImagesbtn.setOnClickListener{
                recyclerViewSavedImages.visibility=View.VISIBLE
                recyclerViewUploadedImages.visibility=View.GONE
            }

            view.edit_account_settings_btn.setOnClickListener {
                val getButtonText = view.edit_account_settings_btn.text.toString()

                when
                {
                    getButtonText == "Edit Profile" -> startActivity(Intent(context, AccountSettingActivity::class.java))

                    getButtonText == "Follow" -> {

                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .setValue(true)
                        }

                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .setValue(true)
                        }

                    }

                    getButtonText == "Following" -> {

                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(it1.toString())
                                .child("Following").child(profileId)
                                .removeValue()
                        }

                        firebaseUser?.uid.let { it1 ->
                            FirebaseDatabase.getInstance().reference
                                .child("Follow").child(profileId)
                                .child("Followers").child(it1.toString())
                                .removeValue()
                        }

                    }
                }

            }

            getFollowers()
            getFollowings()
            userInfo()
            myPhotos()
            getTotalNumberOfPosts()
            mySaves()
            return view
        }
        private fun mySaves() {
            mySavesImg=ArrayList()
            val saveRef=FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser.uid)
            saveRef.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot){
                    if(dataSnapshot.exists())
                    {
                        for (snapshot in dataSnapshot.children)
                        {
                            ( mySavesImg as ArrayList<String>).add(snapshot.key!!)
                        }
                        readSavedImagesData()
                    }
                }


            })
        }
        private fun readSavedImagesData() {
            val postref = FirebaseDatabase.getInstance().reference.child("Posts")

            postref.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.exists()) {
                        (postListSaved as ArrayList<Post>).clear()
                        for (snapshot in p0.children){
                            val post=snapshot.getValue(Post::class.java)
                            for (key in mySavesImg!!)
                            {
                                if (post!!.getPostid()==key)
                                {
                                    (postListSaved as ArrayList<Post>).add(post)

                                }
                            }
                        }
                        myImagesAdapterSavedImage!!.notifyDataSetChanged()
                    }
                }
            })
        }
        private fun myPhotos() {
            val postsRef=FirebaseDatabase.getInstance().reference.child("Posts")
            postsRef.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        (postList as ArrayList<Post>).clear()
                        for (snapshot in p0.children)
                        {
                            val post=snapshot.getValue(Post::class.java)
                            if(post?.getPublisher().equals(profileId))
                            {
                                (postList as ArrayList).add(post!!)
                            }
                            Collections.reverse(postList)
                            myImagesAdapter!!.notifyDataSetChanged()
                        }
                    }
                }


            })

        }

        private fun checkFollowAndFollowingButtonStatus() {
            val followingRef = firebaseUser?.uid.let { it1 ->
                FirebaseDatabase.getInstance().reference
                    .child("Follow").child(it1.toString())
                    .child("Following")
            }

            followingRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (p0.child(profileId).exists())
                    {
                        view?.edit_account_settings_btn?.text = "Following"
                    }
                    else
                    {
                        view?.edit_account_settings_btn?.text = "Follow"
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        private fun getFollowers() {
            val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Followers")

            followersRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (p0.exists())
                    {
                        view?.total_followers?.text = p0.childrenCount.toString()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        private fun getFollowings() {
            val followersRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(profileId)
                .child("Following")


            followersRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (p0.exists())
                    {
                        view?.total_following?.text = p0.childrenCount.toString()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        private fun userInfo() {
            val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(profileId)

            usersRef.addValueEventListener(object : ValueEventListener
            {
                override fun onDataChange(p0: DataSnapshot)
                {
                    if (p0.exists())
                    {
                        val user = p0.getValue<User>(User::class.java)

                        Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(view?.pro_image_profile_frag)
                        view?.profile_fragment_username?.text = user!!.getUsername()
                        view?.full_name_profile_frag?.text = user!!.getFullname()
                        view?.bio_profile_frag?.text = user!!.getBio()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
        private fun getTotalNumberOfPosts() {
            val postref=FirebaseDatabase.getInstance().reference.child("Posts")
            postref.addValueEventListener(object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    if (p0.exists())
                    {
                        var postCounter=0
                        for(snapshot in p0.children)
                        {
                            val post=snapshot.getValue(Post::class.java)
                            if(post?.getPublisher()==profileId)
                            {
                                postCounter++
                            }
                        }
                        total_posts.text=" "+postCounter
                    }
                }
            })

        }
        override fun onStop() {
            super.onStop()

            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref?.putString("profileId", firebaseUser.uid)
            pref?.apply()
        }
        override fun onPause() {
            super.onPause()

            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref?.putString("profileId", firebaseUser.uid)
            pref?.apply()
        }
        override fun onDestroy() {
            super.onDestroy()

            val pref = context?.getSharedPreferences("PREFS", Context.MODE_PRIVATE)?.edit()
            pref?.putString("profileId", firebaseUser.uid)
            pref?.apply()
        }
    }
