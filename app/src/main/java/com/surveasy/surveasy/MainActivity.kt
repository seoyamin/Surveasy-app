package com.surveasy.surveasy

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import com.amplitude.api.Amplitude
import com.amplitude.api.Identify
import com.surveasy.surveasy.databinding.ActivityMainBinding
import com.surveasy.surveasy.home.banner.BannerViewModel
import com.surveasy.surveasy.home.HomeFragment
import com.surveasy.surveasy.home.Opinion.HomeOpinionViewModel
import com.surveasy.surveasy.home.Opinion.OpinionItem
import com.surveasy.surveasy.home.contribution.ContributionItems
import com.surveasy.surveasy.home.contribution.HomeContributionViewModel
import com.surveasy.surveasy.list.*
import com.surveasy.surveasy.list.firstsurvey.FirstSurveyListFragment
import com.surveasy.surveasy.list.firstsurvey.SurveyListFirstSurveyActivity
import com.surveasy.surveasy.login.CurrentUser
import com.surveasy.surveasy.login.CurrentUserViewModel
import com.surveasy.surveasy.my.MyViewFragment
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.surveasy.surveasy.list.firstsurvey.PushDialogActivity
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    val db = Firebase.firestore
    private var backKeyPressedTime : Long = 0
    val surveyList = arrayListOf<SurveyItems>()
    val model by viewModels<SurveyInfoViewModel>()
    val userModel by viewModels<CurrentUserViewModel>()
    val bannerModel by viewModels<BannerViewModel>()
    val contributionModel by viewModels<HomeContributionViewModel>()
    val opinionModel by viewModels<HomeOpinionViewModel>()

    private val REQUEST_CODE_UPDATE = 9999
    private lateinit var appUpdateManager : AppUpdateManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ?????? ???????????? ??????
        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkUpdate()

        fetchBanner()
        fetchCurrentUser(Firebase.auth.currentUser!!.uid)
        fetchSurvey()
        fetchContribution()
        fetchOpinion()



        // Current User
        val user = Firebase.auth.currentUser
        user?.let {
            val uid = user.uid
            val email = user.email
            Log.d(TAG, "@@@@@ Firebase auth email: ${user.email}")
        }


        // Current User from LoginActivity
        val currentUser = intent.getParcelableExtra<CurrentUser>("currentUser_login")
        if(currentUser != null ) {
            userModel.currentUser = currentUser!!
        }
        Log.d(TAG, "###### from Login model: ${userModel.currentUser.email}")


        // Determine Fragment of MainActivity
        val transaction = supportFragmentManager.beginTransaction()
        var defaultFrag_list = false
        var defaultFrag_list_push = false

        defaultFrag_list = intent.getBooleanExtra("defaultFragment_list", false)
        defaultFrag_list_push = intent.getBooleanExtra("defaultFragment_list_push", false)

        if(defaultFrag_list) {
            navColor_On(binding.NavListImg, binding.NavListText)
            navColor_Off(binding.NavHomeImg, binding.NavHomeText, binding.NavMyImg, binding.NavMyText)
            setContentView(binding.root)

            transaction.add(R.id.MainView, SurveyListFragment()).commit()

            if(defaultFrag_list_push) {

                val intent = Intent(this, PushDialogActivity::class.java)
                startActivity(intent)

                defaultFrag_list_push = !defaultFrag_list_push
            }

            defaultFrag_list = !defaultFrag_list

        }
        else {
            setContentView(binding.root)
            transaction.add(R.id.MainView, HomeFragment()).commit()
        }



        // Navigation Bars
        binding.NavHome.setOnClickListener {
            navColor_On(binding.NavHomeImg, binding.NavHomeText)
            navColor_Off(binding.NavListImg, binding.NavListText, binding.NavMyImg, binding.NavMyText)

            supportFragmentManager.beginTransaction()
                .replace(R.id.MainView, HomeFragment())
                .commit()
        }

        binding.NavList.setOnClickListener {
            navColor_On(binding.NavListImg, binding.NavListText)
            navColor_Off(binding.NavHomeImg, binding.NavHomeText, binding.NavMyImg, binding.NavMyText)

            if (userModel.currentUser.didFirstSurvey == false) {
                // Send Current User to Activities
//                val intent_surveylistfirstsurvey: Intent = Intent(this, FirstSurveyListActivity::class.java)
//                intent_surveylistfirstsurvey.putExtra("currentUser_main", userModel.currentUser)
//                startActivity(intent_surveylistfirstsurvey)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.MainView, FirstSurveyListFragment())
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.MainView, SurveyListFragment())
                    .commit()
            }
        }

        binding.NavMy.setOnClickListener {
            navColor_On(binding.NavMyImg, binding.NavMyText)
            navColor_Off(binding.NavHomeImg, binding.NavHomeText, binding.NavListImg, binding.NavListText)

            supportFragmentManager.beginTransaction()
                .replace(R.id.MainView, MyViewFragment())
                .commit()
        }


        //        val keyHash = Utility.getKeyHash(this)
        //        Log.d("Hash",keyHash)




        fun clickList() {
            supportFragmentManager.beginTransaction()
                .replace(R.id.MainView, SurveyListFragment())
                .commit()
        }


    }

    fun firstSurvey(){
        binding.NavList.setOnClickListener {

        }
    }


    fun clickList() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.MainView, SurveyListFragment())
            .commit()
    }

    //when click first Survey
    fun clickItem(){
        val intent_surveylistfirstsurvey: Intent = Intent(this, SurveyListFirstSurveyActivity::class.java)
        intent_surveylistfirstsurvey.putExtra("currentUser_main", userModel.currentUser)
        startActivity(intent_surveylistfirstsurvey)
    }


    private fun fetchCurrentUser(uid: String) :CurrentUser {

        val docRef = db.collection("panelData").document(uid.toString())

        val userSurveyList = ArrayList<UserSurveyItem>()

        docRef.collection("UserSurveyList").get()
            .addOnSuccessListener { documents ->
                for(document in documents){
                    var item : UserSurveyItem = UserSurveyItem(
                        Integer.parseInt(document["id"].toString()),
                        Integer.parseInt(document["lastIDChecked"].toString()),
                        document["title"] as String?,
                        Integer.parseInt(document["panelReward"]?.toString()),
                        document["responseDate"] as String?,
                        document["isSent"] as Boolean?
                    )
                    userSurveyList.add(item)

                }
            }


        docRef.get().addOnCompleteListener { snapshot ->
            if(snapshot != null) {
                val currentUser : CurrentUser = CurrentUser(
                    snapshot.result["uid"].toString(),
                    snapshot.result["fcmToken"].toString(),
                    snapshot.result["name"].toString(),
                    snapshot.result["email"].toString(),
                    snapshot.result["phoneNumber"].toString(),
                    snapshot.result["gender"].toString(),
                    snapshot.result["birthDate"].toString(),
                    snapshot.result["accountType"].toString(),
                    snapshot.result["accountNumber"].toString(),
                    snapshot.result["accountOwner"].toString(),
                    snapshot.result["inflowPath"].toString(),
                    snapshot.result["didFirstSurvey"] as Boolean?,
                    snapshot.result["autoLogin"] as Boolean?,
                    Integer.parseInt(snapshot.result["reward_current"].toString()),
                    Integer.parseInt(snapshot.result["reward_total"].toString()),
                    snapshot.result["marketingAgree"] as Boolean?,
                    userSurveyList
                )
//                if(snapshot.result["autoLogin"] == false) {
//                    intent = Intent(this, LoginActivity::class.java)
//                    startActivity(intent)
//                }
                userModel.currentUser = currentUser
                //Log.d(TAG, "^^^^####$$$$%%%%%%%@@@@@ fetch fun ?????? userModel: ${userModel.currentUser.didFirstSurvey}")
                //Log.d(TAG, "@@@@@ fetch fun ?????? userModel: ${userModel.currentUser.UserSurveyList.toString()}")



                // [Amplitude] user properties (name, reward_total)
                val client = Amplitude.getInstance()
                val userProperties = JSONObject()
                try {
                    userProperties.put("name", userModel.currentUser!!.name).put("reward_total", userModel.currentUser!!.rewardTotal)
                } catch (e: JSONException) {
                    e.printStackTrace()
                    System.err.println("Invalid JSON")
                }
                client.setUserProperties(userProperties)



            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "fail $exception")
        }
        return userModel.currentUser
    }


    fun fetchSurvey() {

        db.collection("surveyData")
            // id??? ???????????? ????????? ????????? ???????????? ???????????? progress ?????? ?????????.
            .orderBy("lastIDChecked", Query.Direction.DESCENDING)
            .limit(18).get()
            .addOnSuccessListener { result ->

                for (document in result) {
                    if(document["panelReward"] != null) {
                        val item: SurveyItems = SurveyItems(
                            Integer.parseInt(document["id"].toString()) as Int,
                            Integer.parseInt(document["lastIDChecked"].toString()) as Int,
                            document["title"] as String,
                            document["target"] as String,
                            document["uploadDate"] as String?,
                            document["link"] as String?,
                            document["spendTime"] as String?,
                            document["dueDate"] as String?,
                            document["dueTimeTime"] as String?,
                            Integer.parseInt(document["panelReward"].toString()),
                            document["noticeToPanel"] as String?,
                            Integer.parseInt(document["progress"].toString())
                        )
                        surveyList.add(item)
                        //Log.d(TAG,"################${document["title"]} and ${document["uploadDate"]}")
                    }

                }

                model.surveyInfo.addAll(surveyList)
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "fail $exception")
            }
    }


    // Get banner img uri from Firebase Storage
    private fun fetchBanner() {
        val storage : FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef : StorageReference = storage.reference.child("banner")
        val listAllTask: Task<ListResult> = storageRef.listAll()


        listAllTask.addOnSuccessListener { result ->
            val items : List<StorageReference> = result.items
            val itemNum : Int = result.items.size
            bannerModel.num = itemNum
            items.forEachIndexed { index, item ->
                item.downloadUrl.addOnSuccessListener {
                    bannerModel.uriList.add(it.toString())
                    //Log.d(TAG, "UUUUUUUU--${index}---$itemNum---${bannerModel.num}--${bannerModel.uriList}+++")
                }

            }
        }
    }

    private fun fetchContribution() {
        db.collection("AppContribution").get()
            .addOnSuccessListener { documents ->
                if(documents != null) {
                    for (document in documents) {
                        val contribution = ContributionItems(
                            document["date"].toString(),
                            document["title"].toString(),
                            document["journal"].toString(),
                            document["source"].toString(),
                            document["institute"].toString(),
                            document["img"].toString(),
                            document["content"].toString()

                        )
                        //Log.d(TAG, ">>>>>>>>>>>>>>>>>>>>>${contribution.date}")
                        contributionModel.contributionList.add(contribution)
                    }
                }

            }

    }

    private fun fetchOpinion() {
        db.collection("AppOpinion").get()
            .addOnSuccessListener { documents ->
                if(documents != null) {
                    for (document in documents) {
                        if(document["isValid"] as Boolean == true) {
                            opinionModel.opinionItem = OpinionItem(
                                Integer.parseInt(document["id"].toString()),
                                document["question"].toString(),
                                document["content1"].toString(),
                                document["content2"].toString()
                            )
                        }
                    }
                }
            }
    }

    private fun navColor_On(img: ImageView, text: TextView) {
        img.setColorFilter(Color.parseColor("#0aab00"))
        text.setTextColor(Color.parseColor("#0aab00"))
    }

    private fun navColor_Off(img1: ImageView, text1: TextView, img2: ImageView, text2: TextView) {
        img1.setColorFilter(Color.parseColor("#c9c9c9"))
        text1.setTextColor(Color.parseColor("#c9c9c9"))

        img2.setColorFilter(Color.parseColor("#c9c9c9"))
        text2.setTextColor(Color.parseColor("#c9c9c9"))
    }

    fun navColor_in_Home() {
        binding.NavHomeImg.setColorFilter(Color.parseColor("#c9c9c9"))
        binding.NavHomeText.setTextColor(Color.parseColor("#c9c9c9"))
        binding.NavListImg.setColorFilter(Color.parseColor("#0aab00"))
        binding.NavListText.setTextColor(Color.parseColor("#0aab00"))
        binding.NavMyImg.setColorFilter(Color.parseColor("#c9c9c9"))
        binding.NavMyText.setTextColor(Color.parseColor("#c9c9c9"))
    }

    fun moreBtn() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.MainView, FirstSurveyListFragment())
            .commit()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this,"???????????? ????????? ?????? ??? ????????? ???????????????.",Toast.LENGTH_SHORT).show()
            return
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish()
        }
    }



    // ?????? ???????????? ?????? ?????? ?????? ?????????
    private fun checkUpdate() {
        var appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            // ???????????? ????????? ????????? ??????
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE
                )

            }

            else {
                // ???????????? ?????? ?????? ?????? ??????
            }


        }
    }


    // ???????????? ????????? ?????? ??????
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")

            }
        }
    }


    // ????????? ?????? ??????????????? ??????, ???????????? ????????? ?????? ??????
    override fun onResume() {
        super.onResume()

        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // If an in-app update is already running, resume the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE)

            }
        }

    }


}

