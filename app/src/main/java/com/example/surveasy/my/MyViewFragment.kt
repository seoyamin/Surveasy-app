package com.example.surveasy.my


import android.os.Bundle
import android.content.ContentValues.TAG
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.surveasy.R
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.surveasy.login.CurrentUserViewModel
import com.example.surveasy.my.history.MyViewHistoryActivity
import com.example.surveasy.my.info.InfoData
import com.example.surveasy.my.info.InfoDataViewModel
import com.google.firebase.auth.ktx.auth
import com.example.surveasy.my.info.MyViewInfoActivity
import com.example.surveasy.my.notice.MyViewNoticeListActivity
import com.example.surveasy.my.setting.MyViewSettingActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async


class MyViewFragment : Fragment() {
    val infoDataModel by viewModels<InfoDataViewModel>()
    val db = Firebase.firestore
    var info = InfoData(null, null, null, null, null, null, null, null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_myview, container, false)
        val userModel by activityViewModels<CurrentUserViewModel>()

        val noticeBtn = view.findViewById<ImageButton>(R.id.MyView_NoticeIcon)
        val historyIcon = view.findViewById<Button>(R.id.MyView_HistoryIcon)
        val infoIcon = view.findViewById<Button>(R.id.MyView_InfoIcon)
        val settingIcon = view.findViewById<Button>(R.id.MyView_SettingIcon)
        val contactIcon = view.findViewById<Button>(R.id.MyView_ContactIcon)

        val userName = view.findViewById<TextView>(R.id.MyView_UserName)
        val userRewardFinAmount = view.findViewById<TextView>(R.id.MyView_UserRewardFinAmount)
        val userRewardYetAmount = view.findViewById<TextView>(R.id.MyView_UserRewardYetAmount)
        val userSurveyCountAmount = view.findViewById<TextView>(R.id.MyView_UserSurveyCountAmount)


        // Set UI with userModel
        if(userModel.currentUser.uid != null) {
            userName.text = "${userModel.currentUser.name}님"
            val rewardFin = (userModel.currentUser.rewardTotal!!) - (userModel.currentUser.rewardCurrent!!)
            userRewardFinAmount.text = "${rewardFin}원"
            userRewardYetAmount.text = "${userModel.currentUser.rewardCurrent}원"
            userSurveyCountAmount.text = "${userModel.currentUser.UserSurveyList!!.size}개"
        }

        fetchInfoData()

        noticeBtn.setOnClickListener {
            val intent = Intent(context, MyViewNoticeListActivity::class.java)
            startActivity(intent)
        }
        historyIcon.setOnClickListener {
            val intent = Intent(context, MyViewHistoryActivity::class.java)
            startActivity(intent)
        }
        infoIcon.setOnClickListener {
            val intent = Intent(context, MyViewInfoActivity::class.java)
            Log.d(TAG, "____________putEtra ${info.phoneNumber}")
            intent.putExtra("info", info!!)
            startActivity(intent)
        }
        settingIcon.setOnClickListener {
            val intent = Intent(context, MyViewSettingActivity::class.java)
            startActivity(intent)
        }
        contactIcon.setOnClickListener {
            val intent = Intent(context, MyViewContactActivity::class.java)
            startActivity(intent)
        }

            return view


        }


    // Fetch info of current User for MyViewInfo
    private fun fetchInfoData() {
        val docRef = db.collection("AndroidUser").document(Firebase.auth.currentUser!!.uid)

        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val infoData: InfoData = InfoData(
                    document["name"] as String,
                    document["birthDate"] as String,
                    document["gender"] as String,
                    document["email"] as String,
                    document["phoneNumber"] as String,
                    document["accountType"] as String,
                    document["accountNumber"] as String,
                    null
                )
                info = infoData
                Log.d(TAG, "****fetch***1**** ${info.birthDate}")
            }
        }

        docRef.collection("FirstSurvey").document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener { document ->
                if (document != null) {
                    info.EngSurvey = document["EngSurvey"] as Boolean
                    Log.d(TAG, "****fetch***2**** ${info.EngSurvey}")
                }
            }

    }


}
