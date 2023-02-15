package com.surveasy.surveasy.my.info

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.surveasy.surveasy.R
import com.surveasy.surveasy.databinding.ActivityMyviewinfoBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.surveasy.surveasy.MainRepository
import com.surveasy.surveasy.MainViewModel
import com.surveasy.surveasy.MainViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyViewInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyviewinfoBinding
    val db = Firebase.firestore
    val infoDataModel by viewModels<InfoDataViewModel>()
    private lateinit var infoViewModel: MyInfoViewModel
    private lateinit var infoViewModelFactory : MyInfoViewModelFactory
    var fragment : Int = 1

//    override fun onStart() {
//        super.onStart()
//
//        val infoData = intent.getParcelableExtra<InfoData>("info")!!
//        infoDataModel.infoData = infoData
//        //Log.d(TAG, "### infoActivity onStart ----- ${infoDataModel.infoData.EngSurvey}")
//
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyviewinfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        infoViewModelFactory = MyInfoViewModelFactory(MyInfoRepository())
        infoViewModel = ViewModelProvider(this, infoViewModelFactory)[MyInfoViewModel::class.java]

        CoroutineScope(Dispatchers.Main).launch {
            infoViewModel.fetchUserInfo(Firebase.auth.uid.toString())
            infoViewModel.repositories1.observe(this@MyViewInfoActivity){
                binding.MyViewInfoInfoItemName.text = it.name
                binding.MyViewInfoInfoItemBirthDate.text = it.birthDate
                binding.MyViewInfoInfoItemGender.text = it.gender
                binding.MyViewInfoInfoItemEmail.text = it.email

            }
        }

        // ToolBar
        setSupportActionBar(binding.ToolbarMyViewInfo)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        binding.ToolbarMyViewInfo.setNavigationOnClickListener {
            onBackPressed()
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.MyViewInfo_View, MyViewInfo1Fragment()).commit()


        binding.MyViewInfoEditBtn.setOnClickListener{
            if(fragment == 1) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.MyViewInfo_View, MyViewInfo2Fragment()).commit()
                fragment = 2

                binding.MyViewInfoEditBtn.text = "수정완료"
                binding.MyViewInfoEditBtn.setBackgroundResource(R.drawable.register_button)
                binding.MyViewInfoEditBtn.setTextColor(Color.parseColor("#FFFFFF"))
            }
            else if(fragment == 2) {
                //updateInfo()
                //fetchInfoData()


                supportFragmentManager.beginTransaction()
                    .replace(R.id.MyViewInfo_View, MyViewInfo1Fragment()).commit()
                fragment = 1

                binding.MyViewInfoEditBtn.text = "수정하기"
                binding.MyViewInfoEditBtn.setBackgroundResource(R.drawable.white_button_myinfo)
                binding.MyViewInfoEditBtn.setTextColor(Color.parseColor("#0aab00"))
            }
        }

    }


    private fun fetchInfoData() {
        val docRef = db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
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
                infoDataModel.infoData = infoData
            }
        }

        docRef.collection("FirstSurvey").document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener { document ->
                if (document["EngSurvey"] != null) {
                    infoDataModel.infoData.EngSurvey = document["EngSurvey"]!! as Boolean
                    setVariableInfo(infoDataModel.infoData)
                }
            }

    }



    private fun setVariableInfo(infoData: InfoData) {
        val phoneNumber = findViewById<TextView>(R.id.MyViewInfo_InfoItem_PhoneNumber)
        val accountType = findViewById<TextView>(R.id.MyViewInfo_InfoItem_AccountType)
        val accountNumber = findViewById<TextView>(R.id.MyViewInfo_InfoItem_AccountNumber)
        val EngSurvey = findViewById<TextView>(R.id.MyViewInfo_InfoItem_EngSurvey)
        val EngSurveySwitch = findViewById<Switch>(R.id.MyViewInfo_InfoItem_EngSurveySwitch)

        phoneNumber.text = infoData.phoneNumber
        accountType.text = infoData.accountType
        accountNumber.text = infoData.accountNumber

        if(infoData.EngSurvey == true) {
            EngSurvey.text = "희망함"
        }
        else if(infoData.EngSurvey == false) {
            EngSurvey.text = "희망하지 않음"
        }
    }


    fun updateInfo() {
//        val docRef = db.collection("panelData").document(Firebase.auth.currentUser!!.uid)

        val phoneNumberEdit = findViewById<EditText>(R.id.MyViewInfo_InfoItem_PhoneNumberEdit)
        val accountNumberEdit = findViewById<EditText>(R.id.MyViewInfo_InfoItem_AccountNumberEdit)


        if(phoneNumberEdit.text.toString().trim().isNotEmpty()) {
            infoDataModel.infoData.phoneNumber = phoneNumberEdit.text.toString()
        }
        if(accountNumberEdit.text.toString().trim().isNotEmpty()) {
            infoDataModel.infoData.accountNumber = accountNumberEdit.text.toString()
        }

        //fetch 해서 저장하고 그 저장값 or 수정값의 플로우로 가야함.
        CoroutineScope(Dispatchers.Main).launch {
        }


//        docRef.update(
//            "phoneNumber", infoDataModel.infoData.phoneNumber,
//            "accountType", infoDataModel.infoData.accountType,
//            "accountNumber", infoDataModel.infoData.accountNumber)
//            .addOnSuccessListener {
//                Log.d(TAG, "##@@@###### info update1 SUCCESS")
//            }
//
//        docRef.collection("FirstSurvey").document(Firebase.auth.currentUser!!.uid)
//            .update("EngSurvey", infoDataModel.infoData.EngSurvey)
//            .addOnSuccessListener {
//                Log.d(TAG, "##@@@###### info update2 SUCCESS")
//            }


    }
}