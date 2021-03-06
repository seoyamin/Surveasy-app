package com.surveasy.surveasy.my.setting

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.surveasy.surveasy.databinding.ActivityMyviewsettingBinding
import com.surveasy.surveasy.login.LoginActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyViewSettingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyviewsettingBinding
    private lateinit var builder : AlertDialog.Builder
    var pushOn : Boolean? = null
    var marketingAgree : Boolean? = null

    override fun onStart() {
        super.onStart()
        binding = ActivityMyviewsettingBinding.inflate(layoutInflater)

        val db = Firebase.firestore
        db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener { result ->
                pushOn = result["pushOn"] as Boolean
                marketingAgree = result["marketingAgree"] as Boolean

                Log.d(TAG, "start-PPPPPPPPPPPPPP : $pushOn")
                Log.d(TAG, "start-MMMMMMMMMMMMMM : $marketingAgree")
            }

//        binding.MyViewSettingPush.setOnClickListener {
//            val intent = Intent(this, MyViewSettingPushActivity::class.java)
//            intent.putExtra("pushOn", pushOn)
//            intent.putExtra("marketingAgree", marketingAgree)
//            startActivity(intent)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyviewsettingBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val reward_current = intent.getIntExtra("reward_current", 0)

//        val db = Firebase.firestore
//        var pushOn : Boolean? = null
//        var marketingAgree : Boolean? = null
//        db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
//            .get().addOnSuccessListener { result ->
//                pushOn = result["pushOn"] as Boolean
//                marketingAgree = result["marketingAgree"] as Boolean
//                Log.d(TAG, "PPPPPPPPPPPPPP : $pushOn")
//                Log.d(TAG, "MMMMMMMMMMMMMM : $marketingAgree")
//            }
//
        binding.MyViewSettingPush.setOnClickListener {
            val intent = Intent(this, MyViewSettingPushActivity::class.java)
            intent.putExtra("pushOn", pushOn)
            intent.putExtra("marketingAgree", marketingAgree)
            startActivity(intent)
        }

        binding.MyViewSettingVersion.setOnClickListener {
            val intent = Intent(this, MyViewSettingVersionActivity::class.java)
            startActivity(intent)
        }

        binding.MyViewSettingWithdraw.setOnClickListener {
            val intent = Intent(this, MyViewSettingWithdrawActivity::class.java)
            intent.putExtra("reward_current", reward_current)
            startActivity(intent)
        }


        builder = AlertDialog.Builder(this)
        binding.MyViewSettingLogout.setOnClickListener {
            builder.setTitle("???????????? ???????????????????")
                .setCancelable(true)
                .setPositiveButton("???"){ dialogInterface, it ->
                    finishAffinity()
                    Firebase.auth.signOut()
                    Log.d(TAG, "((((((((((( logout : ${Firebase.auth.currentUser?.uid}")

                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("?????????"){ dialogInterface, it ->
                    dialogInterface.cancel()
                }
                .show()

        }

        setSupportActionBar(binding.ToolbarMyViewSetting)

        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        binding.ToolbarMyViewSetting.setNavigationOnClickListener {
            onBackPressed()
        }




    }
}