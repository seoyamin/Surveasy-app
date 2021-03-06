package com.surveasy.surveasy.list

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amplitude.api.Amplitude
import com.surveasy.surveasy.MainActivity
import com.surveasy.surveasy.databinding.ActivitySurveyprooflastdialogBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject


class SurveyProofLastDialogActivity : AppCompatActivity() {
    val db = Firebase.firestore


    private lateinit var binding: ActivitySurveyprooflastdialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySurveyprooflastdialogBinding.inflate(layoutInflater)

        setContentView(binding.root)




        binding.SurveyListDetailResponseProofBtn.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("defaultFragment_list",true)
            startActivity(intent)
            finishAffinity()

        }


        val reward = intent.getIntExtra("reward",0)

        // panelData-reward_current/reward_total 업데이트
        var reward_current = 0
        var reward_total = 0
        db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
            .get().addOnSuccessListener { snapShot ->
                reward_current = Integer.parseInt(snapShot["reward_current"].toString())
                reward_total = Integer.parseInt(snapShot["reward_total"].toString())
                Log.d(ContentValues.TAG, "@@@@@@@@@@@@@@@@@@@@ reward_current fetch: $reward_current")
                reward_current += reward
                reward_total += reward

                db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
                    .update("reward_current", reward_current, "reward_total", reward_total)


            }



        // surveyData-respondedPanel에 currentUser uid 추가
        val id = intent.getIntExtra("id",0)
        val dbRef = db.collection("surveyData").document(id.toString())
        dbRef.get().addOnSuccessListener { document ->
            val respondList = document["respondedPanel"] as ArrayList<*>
            val text = document["requiredHeadCount"] as String
            val headCount = Integer.parseInt(text.substring(0,text.length-1))

            //마지막 headcount 면 progress 3으로 업데이트
            if(respondList.size>=headCount-1) {
                dbRef.update(
                    "respondedPanel",
                    FieldValue.arrayUnion(Firebase.auth.currentUser!!.uid)
                )
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "##### surveyData - respondedPanel 성공")
                    }
                dbRef.update("progress", 3)
                    .addOnSuccessListener { Log.d(TAG, "$$$ progress update 성공") }
            }else{
                dbRef.update(
                    "respondedPanel",
                    FieldValue.arrayUnion(Firebase.auth.currentUser!!.uid)
                )
                    .addOnSuccessListener { result ->
                        Log.d(TAG, "##### surveyData - respondedPanel 성공, progress 그대로")
                    }
            }
        }


        // [Amplitude] Survey Submission Fin
        val idChecked = intent.getIntExtra("idChecked", 0)
        val title = intent.getStringExtra("title")
        val client = Amplitude.getInstance()
        val eventProperties = JSONObject()
        try {
            eventProperties.put("id", idChecked).put("title", title).put("reward", reward)
        } catch (e: JSONException) {
            System.err.println("Invalid JSON")
            e.printStackTrace()
        }
        client.logEvent("Survey Submission Fin", eventProperties)


    }


}




