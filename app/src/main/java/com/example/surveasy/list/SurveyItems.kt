package com.example.surveasy.list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

//기본 Survey list
@Parcelize
class SurveyItems(
    val id : String,
    val title : String,
    val target : String,
    val uploadDate : String,
    val link : String,
    val spendTime : String,
    val dueDate : String,
    val dueTimeTime : String,
    val reward : Int,
    val noticeToPanel : String?,

    ):Parcelable {
}