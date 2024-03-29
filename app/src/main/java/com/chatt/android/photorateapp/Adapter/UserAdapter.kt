package com.chatt.android.photorateapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.chatt.android.photorateapp.Fragments.ProfileFragment
import com.chatt.android.photorateapp.Model.User
import com.chatt.android.photorateapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class UserAdapter (private var mContext:Context,private var mUser:List<User>,private var isFragment: Boolean=false): RecyclerView.Adapter<UserAdapter.ViewHolder> (){
    private var firebaseuser=FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):ViewHolder {
        val view =LayoutInflater.from(mContext).inflate(R.layout.user_item_layout,parent,false)
        return ViewHolder(view)
    }
    override fun getItemCount(): Int {
        return mUser.size
    }
    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user=mUser[position]
        holder.usernameTextView.text=user.getUsername()
        holder.userFullnameTextView.text=user.getFullname()
        Picasso.get().load(user.getImage()).placeholder(R.drawable.profile).into(holder.userProfileImage)
        checkFollowingStatus(user.getUID(),holder.followbutton)
        holder.itemView.setOnClickListener {
            val pref=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            pref.putString("profileId",user.getUID())
            pref.apply()
            (mContext as FragmentActivity).supportFragmentManager.beginTransaction().replace(R.id.fragment_container,
                ProfileFragment()
            ).commit()

        }
        holder.followbutton.setOnClickListener {
            if(holder.followbutton.text=="Follow") {
                holder.followbutton.text=="Following"
                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseuser!!.uid)
                        .child("Following")
                        .child(user.getUID()).setValue(true)
                        .addOnCompleteListener { task->
                        if(task.isSuccessful)
                        {

                                FirebaseDatabase.getInstance().reference.child("Follow")
                                    .child(user.getUID()).child("Followers").child(firebaseuser!!.uid)
                                    .setValue(true). addOnCompleteListener { task ->
                                        if (task.isSuccessful) {

                                        }

                            }
                        }

                }
            }
            else{
                firebaseuser?.uid.let { it1 ->
                    FirebaseDatabase.getInstance().reference
                        .child("Follow")
                        .child(it1.toString())
                        .child("Following")
                        .child(user.getUID())
                        .removeValue()
                        .addOnCompleteListener { task->
                        if(task.isSuccessful)
                        {
                            firebaseuser?.uid.let { it1 ->
                                FirebaseDatabase.getInstance().reference.child("Follow")
                                    .child(user.getUID()).child("Followers").child(it1.toString())
                                    .removeValue(). addOnCompleteListener { task ->
                                        if (task.isSuccessful) {

                                        }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }
    private fun checkFollowingStatus(uids: String, followbutton: Button) {
        val followingRef= FirebaseDatabase.getInstance().reference
            .child("Follow").child(firebaseuser!!.uid)
                .child("Following")

        followingRef.addValueEventListener ( object : ValueEventListener {


            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.child(uids).exists())
                {
                    followbutton.text="Following"
                }
                else{
                    followbutton.text="Follow"
                }
            }


        }
        )




    }
    class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView) {
        var usernameTextView:TextView=itemView.findViewById(R.id.user_name_search)
        var userFullnameTextView:TextView=itemView.findViewById(R.id.user_full_name_search)
        var userProfileImage:CircleImageView=itemView.findViewById(R.id.user_profile_image_search)
        var followbutton: Button =itemView.findViewById(R.id.follow_btn_search)
    }
}

