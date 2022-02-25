package com.example.surveasy.firstIntroduceScreen

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.surveasy.R
import com.example.surveasy.login.LoginActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class FirstIntroduceScreenViewPagerAdapter(context: Context, firstIntroduceScreen : FirstIntroduceScreen)
    : RecyclerView.Adapter<FirstIntroduceScreenViewPagerAdapter.PagerViewHolder>() {

    val db = Firebase.firestore
    var token = ""
    val context = context
    val imgList = firstIntroduceScreen.imgList
    val titleList = firstIntroduceScreen.titleList
    val contentList = firstIntroduceScreen.contentList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirstIntroduceScreenViewPagerAdapter.PagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.firstintroducescreen_item, parent, false)
        return PagerViewHolder(view)
    }

    override fun onBindViewHolder(holder: FirstIntroduceScreenViewPagerAdapter.PagerViewHolder, position: Int) {
        holder.img.setImageResource(imgList[position])
        holder.title.text = titleList[position]
        holder.content.text = contentList[position]
        if(position == imgList.size-1) {
            holder.startBtn.visibility = View.VISIBLE

            holder.startBtn.setOnClickListener{
                FirebaseMessaging.getInstance().token.addOnCompleteListener(
                    OnCompleteListener { task ->

                        val value = hashMapOf("fcm" to task.result)
                        db.collection("AndroidFirstScreen").document(task.result)
                            .set(value).addOnSuccessListener { Log.d(TAG,"#####save success") }

                    })

                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return imgList.size
    }

    inner class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img = itemView.findViewById<ImageView>(R.id.FirstIntroduceScreen_ImageView)
        val title = itemView.findViewById<TextView>(R.id.FirstIntroduceScreen_Title)
        val content = itemView.findViewById<TextView>(R.id.FirstIntroduceScreen_Content)
        val startBtn = itemView.findViewById<Button>(R.id.FirstIntroduceScreen_StartBtn)
    }
}