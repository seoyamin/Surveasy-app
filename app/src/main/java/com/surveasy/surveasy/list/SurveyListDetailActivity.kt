package com.surveasy.surveasy.list

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.Window
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import com.amplitude.api.Amplitude
import com.surveasy.surveasy.databinding.ActivitySurveylistdetailBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.lang.RuntimeException


class SurveyListDetailActivity : AppCompatActivity() {

    val PERMISSION_CODE = 101
    val REQUIRED_PERMISSION = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE)

    private lateinit var binding: ActivitySurveylistdetailBinding
    val db = Firebase.firestore
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySurveylistdetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


        var webView : WebView = binding.surveyWebView
        val url : String = intent.getStringExtra("link")!!
        val id : Int = intent.getIntExtra("id",0)!!
        val idChecked : Int = intent.getIntExtra("idChecked",0)!!
        val index : Int = intent.getIntExtra("index",0)!!
        val reward : Int = intent.getIntExtra("reward",0)
        val title : String = intent.getStringExtra("title")!!
        val title_amplitude = title
        val idChecked_amplitude = idChecked


        // [Amplitude] List Detail Showed
        val client = Amplitude.getInstance()
        val eventProperties = JSONObject()
        try {
            eventProperties.put("id", idChecked).put("title", title)
        } catch (e: JSONException) {
            System.err.println("Invalid JSON")
            e.printStackTrace()
        }
        client.logEvent("List Detail Showed", eventProperties)




        val spannableString = SpannableString(reward.toString()+"??? ????????? ??????")
        spannableString.setSpan(UnderlineSpan(),0,spannableString.length,0)
        binding.toolbarUpload.text = spannableString

        if(title.length>15){
            binding.toolbarText.text = title.substring(0,14)+".."
        }else{
            binding.toolbarText.text = title
        }


       //activity ?????????????????? permission ??????
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show()
        }else{
            requestPermissions(this,REQUIRED_PERMISSION,PERMISSION_CODE)
        }


        //progress 3??? ???????????? alert ????????? ?????????
        db.collection("surveyData").document(id.toString()).get()
            .addOnSuccessListener { document ->
                if(Integer.parseInt(document["progress"].toString())>2){
                    val intent = Intent(this,SurveyDoneAlertDialogActivity::class.java)
                    startActivity(intent)
                }
            }

        val apply = webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }
        webView.loadUrl(url)
        webView.pageUp(true)
        val timestamp_start = System.currentTimeMillis() / 1000

        //Toast.makeText(this,"###${id}",Toast.LENGTH_LONG).show()



        binding.toolbarUpload.setOnClickListener {
            val intent = Intent(this, SurveyProofDialogActivity::class.java)
            val title = intent.putExtra("title",title)
            val index = intent.putExtra("index",index)
            val id = intent.putExtra("id",id)
            val idChecked = intent.putExtra("idChecked",idChecked)
            val reward = intent.putExtra("reward", reward)


            val timestamp_end = System.currentTimeMillis() / 1000
            val spentTimeInSurvey = (timestamp_end - timestamp_start).toInt()


            // [Amplitude] Survey Participated
            val client = Amplitude.getInstance()
            val eventProperties = JSONObject()
            try {
                eventProperties.put("title", title_amplitude).put("id", idChecked_amplitude)
                    .put("spentTimeInSurvey", spentTimeInSurvey)
            } catch (e: JSONException) {
                System.err.println("Invalid JSON")
                e.printStackTrace()
            }
            client.logEvent("Survey Participated", eventProperties)




            //permission ?????? ????????? upload ?????? ????????? ???????????? ?????? ???????????? ???
            if(checkPermission()){
                startActivityForResult(intent,101)
            }else{
                showDialogToGetPermission()
            }


       }

        //Toolbar
        setSupportActionBar(binding.ToolbarProof)
        if(supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        binding.ToolbarProof.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    //???????????? ????????? webView ????????? ????????????
    override fun onBackPressed() {
        var webView : WebView = binding.surveyWebView
        if(webView.canGoBack()){
            webView.goBack()
        }else{
            finish()
        }
    }

    //permission ?????? ????????? ??????
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            PERMISSION_CODE -> {
                if(grantResults.isEmpty()){
                    throw RuntimeException("Empty permission result")
                }
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show()
                } else {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(
                            this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                        Log.d(TAG,"denied")
                        showDialogToGetPermission()

                    }else{
                        Log.d(TAG,"no more")
                        showDialogToGetPermission()
                    }
                }
            }
        }
    }

    //?????? ????????? ??? ????????? ??? ??????????????? ???????????? ???????????? intent ????????????
    private fun showDialogToGetPermission(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("????????? ???????????????").setMessage("?????? ?????? ?????? ???????????? ???????????? ????????? ?????? ????????? ???????????????.")
        builder.setPositiveButton("???????????? ??????") { dialogInterface, i ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package",packageName,null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNegativeButton("????????? ??????"){ dialogInterface, i ->
            //Toast.makeText(this,"?????????????????????",Toast.LENGTH_LONG).show()
        }
        val dialog = builder.create()
        dialog.show()
    }

    //upload ?????? ?????? ??? permission ?????? ??????
    private fun checkPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }


//    private fun takeScreenshot(){
//        try{
//            Log.d(TAG,"//////ss")
//            val mPath = Environment.getExternalStorageDirectory().toString()+".jpg"
//            val v1 : View = getWindow().decorView.rootView
//            v1.isDrawingCacheEnabled()
//            val bitmap :Bitmap = Bitmap.createBitmap(v1.getDrawingCache())
//            val imgFile : File = File(mPath)
//            val outputStream: FileOutputStream = FileOutputStream(imgFile)
//            val quality = 100
//            bitmap.compress(Bitmap.CompressFormat.JPEG , quality,outputStream)
//            outputStream.flush()
//            outputStream.close()
//
//        }catch (e:Throwable){
//            Log.d(TAG,"//////fail")
//            e.printStackTrace()
//        }
//    }







    }

