package com.example.surveasy.my

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.surveasy.databinding.ActivityMyviewnoticelistBinding
import com.example.surveasy.login.UserItems
import com.example.surveasy.login.UserItemsAdapter
import com.google.firebase.firestore.*

class MyViewNoticeListActivity : AppCompatActivity() {

    private lateinit var noticeRecyclerView: RecyclerView
    private lateinit var noticeList : ArrayList<NoticeItems>

    private lateinit var userRecyclerView: RecyclerView
    private lateinit var userList : ArrayList<UserItems>

    private lateinit var db : FirebaseFirestore



    private lateinit var binding : ActivityMyviewnoticelistBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyviewnoticelistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.ToolbarMyViewNoticeList)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

//        binding.MyViewNoticeListItemTitle.setOnClickListener {
//            val intent = Intent(this,MyViewNoticeListDetailActivity::class.java)
//            startActivity(intent)
//        }
//        binding.MyViewNoticeListItemPinTitle.setOnClickListener {
//            val intent = Intent(this,MyViewNoticeListDetailActivity::class.java)
//            startActivity(intent)
//        }

        binding.ToolbarMyViewNoticeList.setNavigationOnClickListener {
            onBackPressed()
        }

        //fetchNoticeData()
        fetchUserData()

    }

    private fun fetchNoticeData() {
        noticeRecyclerView = binding.recyclerNoticeContainer
        noticeRecyclerView.setHasFixedSize(true)

        noticeList = arrayListOf()


        db = FirebaseFirestore.getInstance()
        db.collection("AppNotice")
            .get()
            .addOnSuccessListener { result ->
                noticeList.clear()
                for(document in result) {
                    var item : NoticeItems = NoticeItems(document["title"] as String, document["date"] as String)
                    noticeList.add(item)
                    noticeRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                    noticeRecyclerView.adapter = NoticeItemsAdapter(noticeList)
                }

            }
    }

    private fun fetchUserData() {
        userRecyclerView = binding.recyclerUserContainer
        userRecyclerView.setHasFixedSize(true)

        userList = arrayListOf()


        db = FirebaseFirestore.getInstance()
        db.collection("AndroidUser")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for(document in result) {
                    var item : UserItems = UserItems(document["uid"] as String, document["email"] as String)
                    userList.add(item)
                    userRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
                    userRecyclerView.adapter = UserItemsAdapter(userList)
                }

            }
    }



}


