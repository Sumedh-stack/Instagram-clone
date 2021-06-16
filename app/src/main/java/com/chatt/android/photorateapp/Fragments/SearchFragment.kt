package com.chatt.android.photorateapp.Fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chatt.android.photorateapp.Adapter.UserAdapter
import com.chatt.android.photorateapp.Model.User
import com.chatt.android.photorateapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.view.*
import java.util.*
import kotlin.collections.ArrayList


class SearchFragment : Fragment() {
     private var mUser:MutableList<User>?=null
    private var userAdapter:UserAdapter?=null
    private var recyclerView: RecyclerView?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view= inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView=view.findViewById(R.id.recycler_view_search)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager=LinearLayoutManager(context)
        mUser =ArrayList()
        userAdapter=context.let { UserAdapter(it!!,mUser as List<User>,true) }
        recyclerView?.adapter = userAdapter

        view.search_edit_text.addTextChangedListener(object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
            {
                if (view.search_edit_text.text.toString() == "")
                { }
                else {
                    recyclerView?.visibility = View.VISIBLE
                    retrieveUsers()
                    searchUser(s.toString().toLowerCase())
                }
            }

            override fun afterTextChanged(s: Editable?) {}


        })
        return view






    }

    private fun searchUser(input: String) {
        val query=FirebaseDatabase.getInstance().reference.child("Users").orderByChild("fullname").startAt(input).endAt(input+"\uf8ff")
        query.addValueEventListener(object:ValueEventListener
        {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot)
            {

                mUser?.clear()
                for (snapshot in dataSnapshot.children)
                {
                    val user = snapshot.getValue(User::class.java)

                    if (user != null)
                    {
                        mUser?.add(user)

                    }
                }
                userAdapter?.notifyDataSetChanged()

            }
        })
    }
    private fun retrieveUsers() {
        val userRef=FirebaseDatabase.getInstance().getReference().child("Users")
        userRef.addValueEventListener(object:ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (view?.search_edit_text?.text.toString() == "")
                {
                    mUser?.clear()
                    for(snapshot in dataSnapshot.children)
                    {
                        val user=snapshot.getValue(User::class.java)
                        if(user!=null)
                        {
                            mUser?.add(user)

                        }
                    }
                    userAdapter?.notifyDataSetChanged()

                }

            } })
    }



}