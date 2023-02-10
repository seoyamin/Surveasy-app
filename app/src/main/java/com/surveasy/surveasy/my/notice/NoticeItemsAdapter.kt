package com.surveasy.surveasy.my.notice

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.surveasy.surveasy.R

class NoticeItemsAdapter(private val noticeList : ArrayList<NoticeModel>)
    : RecyclerView.Adapter<NoticeItemsAdapter.CustomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
    : CustomViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_notice,parent,false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val notice = noticeList[position]
        holder.noticeItemTitle.text = notice.title
        holder.noticeItemDate.text = notice.date
        holder.noticeItemContent.text = notice.content

        if(!noticeList[position].isOpened) holder.noticeNew.visibility = View.VISIBLE
        else holder.noticeNew.visibility = View.INVISIBLE

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView!!.context, MyViewNoticeListDetailActivity::class.java)
            intent.putExtra("title", holder.noticeItemTitle.text)
            intent.putExtra("date", holder.noticeItemDate.text)
            intent.putExtra("content", holder.noticeItemContent.text)
            intent.putExtra("fixed", false)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return noticeList.size
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noticeItemTitle : TextView = itemView.findViewById(R.id.NoticeItem_Title)
        val noticeItemDate: TextView = itemView.findViewById(R.id.NoticeItem_Date)
        val noticeItemContent : TextView = itemView.findViewById(R.id.NoticeItem_Content)
        val noticeNew : ImageView = itemView.findViewById(R.id.NoticeItem_New)

    }


}