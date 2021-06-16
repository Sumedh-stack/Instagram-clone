package com.chatt.android.photorateapp.Adapter


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.recyclerview.widget.RecyclerView
import com.chatt.android.photorateapp.CommentsActivity
import com.chatt.android.photorateapp.MainActivity
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
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private var mContext: Context, private var mPost:List<Post>): RecyclerView.Adapter<PostAdapter.ViewHolder> (){
    private var firebaseUser:FirebaseUser?=null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser=FirebaseAuth.getInstance().currentUser
        val post=mPost[position]
        Picasso.get().load(post.getPostimage()).into(holder.postImage)
        if(post.getDescription()=="")
        {
            holder.description.visibility=View.GONE
        }
        else{
            holder.description.visibility=View.VISIBLE
            holder.description.text = post.getDescription()
        }
        publisherInfo(holder.profileImage,holder.userName,holder.publisher,post.getPublisher())
        isLikes(post.getPostid(),holder.likeButton)
        numberOfLikes(holder.likes,post.getPostid())
        getTotalComments(holder.comments,post.getPostid())
        checkSavedStatus(post.getPostid(),holder.saveButton)
        holder.likeButton.setOnClickListener {
            if(holder.likeButton.tag=="Like")
            {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid()).child(firebaseUser!!.uid).setValue(true)
            }
            else{

                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostid()).child(firebaseUser!!.uid).removeValue()
                val intent= Intent(mContext, MainActivity::class.java)
                mContext.startActivity(intent)
            }

        }
        holder.commentButton.setOnClickListener {
            val intentcomment= Intent(mContext, CommentsActivity::class.java)
            intentcomment.putExtra("postId",post.getPostid())
            intentcomment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentcomment)
        }
        holder.comments.setOnClickListener {
            val intentcomment= Intent(mContext,CommentsActivity::class.java)
            intentcomment.putExtra("postId",post.getPostid())
            intentcomment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentcomment)
        }
        holder.saveButton.setOnClickListener{
            if (holder.saveButton.tag=="Save")
            {
                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).setValue(true)

            }
            else{

                FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid).child(post.getPostid()).removeValue()

            }


        }
    }

    private fun numberOfLikes(likes: TextView, postid: String) {
        val LikesRef= FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        LikesRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    likes.text=p0.childrenCount.toString() + " likes"

                }

            }


        })
    }
    private fun getTotalComments(comments: TextView, postid: String) {
        val commentsRef= FirebaseDatabase.getInstance().reference.child("Comments").child(postid)
        commentsRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    comments.text="view all " +p0.childrenCount.toString() + " comments"

                }

            }


        })
    }
    private fun isLikes(postid: String, likeButton: ImageView) {
        val firebaseUser=FirebaseAuth.getInstance().currentUser
        val LikesRef= FirebaseDatabase.getInstance().reference.child("Likes").child(postid)
        LikesRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.child(firebaseUser!!.uid).exists())
                {
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag="Liked"

                }
                else{

                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag="Like"
                }
            }


        })
    }


    inner class ViewHolder(@NonNull itemView:View):RecyclerView.ViewHolder(itemView)
    {
        var profileImage:CircleImageView
        var postImage:ImageView
        var commentButton:ImageView
        var likeButton:ImageView
        var saveButton:ImageView
        var userName:TextView
        var likes:TextView
        var publisher:TextView
        var description:TextView
        var comments:TextView

        init {
            profileImage=itemView.findViewById(R.id.user_profile_image_post)
            postImage=itemView.findViewById(R.id.post_image_home)
            commentButton=itemView.findViewById(R.id.post_image_comment_btn)
            likeButton=itemView.findViewById(R.id.post_image_like_btn)
            saveButton=itemView.findViewById(R.id.post_save_comment_btn)
            userName=itemView.findViewById(R.id.user_name_post)
            likes=itemView.findViewById(R.id.likes)
            publisher=itemView.findViewById(R.id.publisher)
            description=itemView.findViewById(R.id.description)
            comments=itemView.findViewById(R.id.comments)
        }
    }
    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {
        val userRef=FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)
        userRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists())
                {
                    val user=p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.text = user.getUsername()
                    publisher.text = user.getUsername()

                }

            }


        })
    }
    private fun checkSavedStatus(postid:String,imageView:ImageView)
    {

        val saveRef= FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)
        saveRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.child(postid).exists())
                {
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag="Saved"


                }
                else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag="Save"
                }
            }


        })
    }
}