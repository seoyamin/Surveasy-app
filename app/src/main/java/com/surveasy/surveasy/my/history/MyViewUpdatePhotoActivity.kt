package com.surveasy.surveasy.my.history

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.surveasy.surveasy.R
import com.surveasy.surveasy.databinding.ActivityHomeOpinionAnswerBinding
import com.surveasy.surveasy.databinding.ActivityMyViewUpdatePhotoBinding
import com.surveasy.surveasy.list.SurveyProofLastDialogActivity
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class MyViewUpdatePhotoActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMyViewUpdatePhotoBinding
    val db = Firebase.firestore
    val storage = Firebase.storage
    var pickImageFromAlbum = 0
    var uriPhoto: Uri? = null
    val PERMISSION_CODE = 101
    val REQUIRED_PERMISSION = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyViewUpdatePhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //activity 들어가자마자 permission 확인
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED){
            //Toast.makeText(this,"Permission Granted",Toast.LENGTH_LONG).show()
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_CODE)
        }

        //val filePath = intent.getStringExtra("filePath")
        val filePath = intent.getStringExtra("filePath").toString()
        val id = intent.getIntExtra("id",0)

        fetchLastImg(id, filePath)

        binding.MyViewHistoryUpdateSelectBtn.setOnClickListener{
            //permission 없는 상태로 upload 버튼 누르면 설정으로 이동 유도하는 창
            if(checkPermission()){

                // 앨범으로
                var photoPick = Intent(Intent.ACTION_PICK)
                photoPick.type = "image/*"
                startActivityForResult(photoPick, pickImageFromAlbum)
            }else{
                showDialogToGetPermission()
            }

        }

        binding.MyViewHistoryUpdateSentBtn.setOnClickListener {
            uploadStorage()
        }
    }

    // 기존에 첨부한 이미지 보여주기
    private fun fetchLastImg(id : Int, filePath : String) {


        val storageRef: StorageReference = storage.reference.child("historyTest")
//        val storageRef: StorageReference = storage.reference.child(id.toString())
        val file1: StorageReference = storageRef.child("surveytips2image(3).jpeg")
//        val file1: StorageReference = storageRef.child(filePath.toString())

        Glide.with(this).load(R.raw.app_loading).into(binding.MyViewHistoryLastPhoto)

        file1.downloadUrl.addOnSuccessListener { item ->
            Glide.with(this).load(item).into(binding.MyViewHistoryLastPhoto)
        }
    }

    //화면에 사진 나타내기
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.historyTextUpdate.text = "새로 첨부할 이미지"
        if(requestCode == pickImageFromAlbum){
            if(resultCode == Activity.RESULT_OK){
                uriPhoto = data?.data
                binding.MyViewHistoryLastPhoto.setImageURI(uriPhoto)

            }
        }
    }

    //firebase storage upload
    private fun uploadStorage(){
//        val file = intent.getStringExtra("filePath").toString()
        val file = "surveytips2image(2).jpeg"
        val filePath = storage.reference.child("historyTest").child(file.toString())
        val id: Int = intent.getIntExtra("id",0)!!
        val idChecked = intent.getIntExtra("idChecked",0)!!
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmm").format(Date())
        val imgName = Firebase.auth.currentUser!!.uid+"__"+timestamp

        val storageRef = storage.reference.child("historyTest").child(imgName)
        val uploadTask = storageRef.putFile(uriPhoto!!)
        updateFilePath(idChecked,imgName)
        uploadTask.addOnSuccessListener {
            deletePhoto(filePath)
        }

    }

    // 이전 사진 삭제하기
    private fun deletePhoto(storageRef : StorageReference){
        storageRef.delete().addOnSuccessListener {
            Toast.makeText(this,"업로드 완료",Toast.LENGTH_SHORT)
        }
    }

    private fun updateFilePath(id : Int, imgName : String){
        val uid = Firebase.auth.currentUser!!.uid
        val dbRef = db.collection("panelData").document(uid.toString())
            .collection("UserSurveyList").document(id.toString())
        dbRef.update(
            "filePath",
            imgName.toString()
        )
            .addOnSuccessListener { result ->
                Log.d(ContentValues.TAG, "##### filePath update 성공")
            }
    }

    //permission 동의 여부에 따라
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
                        Log.d(ContentValues.TAG,"denied")
                        showDialogToGetPermission()

                    }else{
                        Log.d(ContentValues.TAG,"no more")
                        showDialogToGetPermission()
                    }
                }
            }
        }
    }

    //한번 거부한 적 있으면 그 다음부터는 설정으로 이동하는 intent 나타내기
    private fun showDialogToGetPermission(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("권한이 필요합니다").setMessage("설문 완료 인증 캡쳐본을 전송하기 위해서 접근 권한이 필요합니다.")
        builder.setPositiveButton("설정으로 이동") { dialogInterface, i ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package",packageName,null))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        builder.setNegativeButton("나중에 하기"){ dialogInterface, i ->
            //Toast.makeText(this,"거부되었습니다",Toast.LENGTH_LONG).show()
        }
        val dialog = builder.create()
        dialog.show()
    }

    //upload 버튼 누를 때 permission 상태 확인
    private fun checkPermission() : Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }
}