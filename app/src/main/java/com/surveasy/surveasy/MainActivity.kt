package com.surveasy.surveasy

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.amplitude.api.Amplitude
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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.surveasy.surveasy.auth.AuthModel
import com.surveasy.surveasy.auth.loginWithKakao
import com.google.gson.internal.bind.util.ISO8601Utils
import com.kakao.sdk.common.util.Utility
import com.surveasy.surveasy.home.Opinion.AnswerItem
import com.surveasy.surveasy.home.Opinion.HomeOpinionAnswerViewModel
import com.surveasy.surveasy.list.firstsurvey.PushDialogActivity
import com.surveasy.surveasy.userRoom.MIGRATION_1_2
import com.surveasy.surveasy.userRoom.User
import com.surveasy.surveasy.userRoom.UserDatabase
import com.surveasy.surveasy.util.NavType
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val db = Firebase.firestore
    private var backKeyPressedTime : Long = 0
    val surveyList = arrayListOf<SurveyItems>()
    val answerList = arrayListOf<AnswerItem>()
    val model by viewModels<SurveyInfoViewModel>()
    val userModel by viewModels<CurrentUserViewModel>()
    val bannerModel by viewModels<BannerViewModel>()
    val contributionModel by viewModels<HomeContributionViewModel>()
    val opinionModel by viewModels<HomeOpinionViewModel>()
    val opinionAnswerModel by viewModels<HomeOpinionAnswerViewModel>()
    private lateinit var userDB : UserDatabase

    private var age : Int = 0
    private var gender : String = ""

    private val REQUEST_CODE_UPDATE = 9999
    private lateinit var appUpdateManager : AppUpdateManager

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        var keyHash = Utility.getKeyHash(this)
//        Log.d(TAG, "onCreate: $keyHash")


        // Initiate Room UserDB
        userDB = Room.databaseBuilder(
            this,
            UserDatabase::class.java, "UserDatabase"
        ).addMigrations(MIGRATION_1_2).allowMainThreadQueries().build()

        //Log.d(TAG, "onCreate: ${userDB.userDao().getAutoLogin()}")

        // 인앱 업데이트 체크
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
        }


        // Current User from LoginActivity
        val currentUser = intent.getParcelableExtra<CurrentUser>("currentUser_login")
        if(currentUser != null ) {
            userModel.currentUser = currentUser!!
        }


        // Determine Fragment of MainActivity
        val transaction = supportFragmentManager.beginTransaction()
        var defaultFrag_list = false
        var defaultFrag_list_push = false
        var defaultFrag_my = false

        defaultFrag_list = intent.getBooleanExtra("defaultFragment_list", false)
        defaultFrag_list_push = intent.getBooleanExtra("defaultFragment_list_push", false)
        defaultFrag_my = intent.getBooleanExtra("defaultFragment_my", false)


        if(defaultFrag_list) {
            navColorOn(NavType.LIST)
            navColorOff(NavType.HOME, NavType.MY)
            setContentView(binding.root)

            transaction.add(R.id.fl_main, SurveyListFragment()).commit()

            if(defaultFrag_list_push) {
                val intent = Intent(this, PushDialogActivity::class.java)
                startActivity(intent)

                defaultFrag_list_push = !defaultFrag_list_push
            }

            defaultFrag_list = !defaultFrag_list

        }else if(defaultFrag_my){
            navColorOn(NavType.MY)
            navColorOff(NavType.HOME, NavType.LIST)
            setContentView(binding.root)

            transaction.add(R.id.fl_main, MyViewFragment()).commit()
        }
        else {
            setContentView(binding.root)
            transaction.add(R.id.fl_main, HomeFragment()).commit()
        }



        // Navigation Bars
        binding.llNavHome.setOnClickListener {
            navColorOn(NavType.HOME)
            navColorOff(NavType.LIST, NavType.MY)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_main, HomeFragment())
                .commit()
        }

        binding.llNavList.setOnClickListener {
            navColorOn(NavType.LIST)
            navColorOff(NavType.HOME, NavType.MY)

            if (userModel.currentUser.didFirstSurvey == false) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_main, FirstSurveyListFragment())
                    .commit()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fl_main, SurveyListFragment())
                    .commit()
            }
        }

        binding.llNavMy.setOnClickListener {
            navColorOn(NavType.MY)
            navColorOff(NavType.HOME, NavType.LIST)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fl_main, MyViewFragment())
                .commit()
        }

    }



    fun clickList() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_main, SurveyListFragment())
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
                        Integer.parseInt(document["panelReward"].toString()) as Int?,
                        document["responseDate"] as String?,
                        document["isSent"] as Boolean?,
                        null
                    )
                    userSurveyList.add(item)

                }
            }


        docRef.get().addOnCompleteListener { snapshot ->
            if(snapshot != null) {

                // Local Room DB에 current user의 User 객체 저장하기
                val uidNum = userDB.userDao().getNumUid(snapshot.result["uid"].toString())

                // [case 1] 해당 uid 가진 튜플 없는 경우 (INSERT user info)
                if(uidNum == 0) {
                    userDB.userDao().deleteAll()
                    val user : User = User(
                        snapshot.result["uid"].toString(),
                        Integer.parseInt(snapshot.result["birthDate"].toString().substring(0, 4)),
                        snapshot.result["gender"].toString(),
                        snapshot.result["fcmToken"].toString(),
                        snapshot.result["autoLogin"] as Boolean,
                        true
                    )
                    userDB.userDao().insert(user)
                }

                // [case 2] 이미 동일한 uid의 튜플이 저장된 경우 (UPDATE fcm token)
                else if(uidNum == 1) {
                    userDB.userDao().updateFcm(snapshot.result["uid"].toString(),snapshot.result["fcmToken"].toString())
                }


                // userModel에 유저 정보 저장
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

                userModel.currentUser = currentUser


                // [Amplitude] user properties (name, reward_total)
                val client = Amplitude.getInstance()
                val userProperties = JSONObject()
                try {
                    userProperties.put("name", userModel.currentUser!!.name)
                        .put("reward_total", userModel.currentUser!!.rewardTotal)
                        .put("birthYear", userModel.currentUser!!.birthDate!!.substring(0, 4))
                        .put("gender", userModel.currentUser!!.gender)
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


    fun checkTargeting(targetingAge : Int, targetingGender : Int,
                       targetingAgeOption : Int, targetingAgeOptionList : ArrayList<String>) : Boolean {
        // 기존 타겟팅
        if(targetingAge != -1){
            // [case 1] 타겟팅 없는 설문
            if(targetingAge <= 1 && targetingGender <= 1) return true
            // [case 2] 타겟팅 있는 설문
            else  {
                when(targetingAge) {
                    2 ->  if(age < 20 || age > 29) return false
                    3 ->  if(age < 20 || age > 24) return false
                    4 ->  if(age < 25 || age > 29) return false
                    5 ->  if(age < 20 || age > 39) return false
                    6 ->  if(age < 20 || age > 49) return false
                }
                when(targetingGender) {
                    2 ->  if(gender == "여") return false
                    3 ->  if(gender == "남") return false
                }
            }
        }
        // 새로운 타겟팅설문
        else{
            // [case 1] 타겟팅 없는 설문
            if(targetingAgeOption <= 1 && targetingGender <= 1) return true

            // [case 2] 타겟팅 있는 설문
            else{
                var flag = false;
                for(value : String in targetingAgeOptionList){
                    val startAge : Int = Integer.parseInt(value.substring(0,2))
                    val endAge : Int = startAge + 4
                    if(age in startAge..endAge) {
                        flag = true;
                        break;
                    }
                }
                if(!flag) return false;
                when(targetingGender) {
                    2 ->  if(gender == "여") return false
                    3 ->  if(gender == "남") return false
                }
            }
        }

        return true
    }


    fun fetchSurvey() {

        // [Targeting] Room DB에서 User info 가져오기
        val birthYear = userDB.userDao().getBirth()
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            age = currentYear - birthYear + 1
        gender = userDB.userDao().getGender()


        // Fetch from FB
        db.collection("surveyData")
            .orderBy("lastIDChecked", Query.Direction.DESCENDING)
            .limit(18)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {

                    // [case 1] 타겟팅 추가 이후 설문
                    if(document["targetingAge"] != null && document["targetingGender"] != null) {
                        val targetingAge = Integer.parseInt(document["targetingAge"].toString()) as Int
                        val targetingGender = Integer.parseInt(document["targetingGender"].toString()) as Int
                        var targetingAgeOption = 0
                        var targetingAgeOptionList = ArrayList<String>()
                        if(document["targetingAgeOption"] != null && document["targetingAgeOptionList"] != null){
                            targetingAgeOption = Integer.parseInt(document["targetingAgeOption"].toString()) as Int
                            targetingAgeOptionList = document["targetingAgeOptionList"] as ArrayList<String>
                        }

                        if(checkTargeting(targetingAge, targetingGender, targetingAgeOption, targetingAgeOptionList)) {
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
                                    Integer.parseInt(document["progress"].toString()),
                                    Integer.parseInt(document["targetingAge"].toString()) as Int,
                                    Integer.parseInt(document["targetingGender"].toString()) as Int,
                                )
                                surveyList.add(item)
                            }
                        }


                    }

                    // [case 2] 타겟팅 추가 이전 설문
                    else {
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
                                Integer.parseInt(document["progress"].toString()),
                                1,
                                1
                            )
                            surveyList.add(item)
                        }
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
                        contributionModel.contributionList.add(contribution)
                    }
                }

            }

    }

    private fun fetchOpinion() {
        //query 문으로 수정함. 확인 후 뒤 주석처리 지우기
        db.collection("AppOpinion").whereEqualTo("isValid", true)
            .get().addOnCompleteListener{ documents ->
                val opinion = documents.result.documents[0]
                opinionModel.opinionItem = OpinionItem(
                    Integer.parseInt(opinion["id"].toString()),
                    opinion["question"].toString(),
                    opinion["content1"].toString(),
                    opinion["content2"].toString()
                )
            }

        db.collection("AppAnswer").get()
            .addOnSuccessListener { documents ->
                if(documents != null){
                    for (document in documents){
                        val answerItem : AnswerItem = AnswerItem(
                            Integer.parseInt(document["id"].toString()) as Int,
                            document["question"] as String,
                            document["content1"] as String?,
                            document["content2"] as String?,
                            document["content3"] as String?
                        )
                        answerList.add(answerItem)
                    }


                }
                opinionAnswerModel.homeAnswerList.addAll(answerList.sortedByDescending { it.id })
            }.addOnFailureListener{ exception ->
                Log.d(TAG, "fail $exception")
            }
    }

    private fun switchHomeNav(on : Boolean) {
        with(binding) {
            if(on) {
                ivNavHome.setBackgroundResource(R.drawable.nav_home_green)
                tvNavHome.setTextColor(Color.parseColor("#0aab00"))
            } else {
                ivNavHome.setBackgroundResource(R.drawable.nav_home_gray)
                tvNavHome.setTextColor(Color.parseColor("#c9c9c9"))
            }
        }
    }

    private fun switchListNav(on : Boolean) {
        with(binding) {
            if(on) {
                ivNavList.setBackgroundResource(R.drawable.nav_list_green)
                tvNavList.setTextColor(Color.parseColor("#0aab00"))
            } else {
                ivNavList.setBackgroundResource(R.drawable.nav_list_gray)
                tvNavList.setTextColor(Color.parseColor("#c9c9c9"))
            }
        }
    }

    private fun switchMyNav(on : Boolean) {
        with(binding) {
            if(on) {
                ivNavMy.setBackgroundResource(R.drawable.nav_my_green)
                tvNavMy.setTextColor(Color.parseColor("#0aab00"))
            } else {
                ivNavMy.setBackgroundResource(R.drawable.nav_my_gray)
                tvNavMy.setTextColor(Color.parseColor("#c9c9c9"))
            }
        }
    }

    private fun navColorOn(nav : NavType) {
        when(nav) {
            NavType.HOME -> switchHomeNav(true)
            NavType.LIST -> switchListNav(true)
            NavType.MY -> switchMyNav(true)
        }
    }

    private fun navColorOff(nav1 : NavType, nav2 : NavType) {
        when(nav1) {
            NavType.HOME -> switchHomeNav(false)
            NavType.LIST -> switchListNav(false)
            NavType.MY -> switchMyNav(false)
        }

        when(nav2) {
            NavType.HOME -> switchHomeNav(false)
            NavType.LIST -> switchListNav(false)
            NavType.MY -> switchMyNav(false)
        }
    }

    fun navColor_In_Home() {
        with(binding){
            ivNavHome.setColorFilter(Color.parseColor("#c9c9c9"))
            tvNavHome.setTextColor(Color.parseColor("#c9c9c9"))
            ivNavList.setColorFilter(Color.parseColor("#0aab00"))
            tvNavList.setTextColor(Color.parseColor("#0aab00"))
            ivNavMy.setColorFilter(Color.parseColor("#c9c9c9"))
            tvNavMy.setTextColor(Color.parseColor("#c9c9c9"))
        }
    }

    fun moreBtn() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fl_main, FirstSurveyListFragment())
            .commit()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() > backKeyPressedTime + 2000){
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show()
            return
        }
        if(System.currentTimeMillis() <= backKeyPressedTime + 2000){
            finish()
        }
    }



    // 인앱 업데이크 가능 여부 체크 메소드
    private fun checkUpdate() {
        var appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->

            // 업데이트 가능한 구버전 상태
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {

                // [Amplitude] In-app Update Available
                val client = Amplitude.getInstance()
                client.logEvent("In-app Update Available")


                // Request the update.
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_UPDATE
                )

            }

            else {
                // 업데이트 필요 없는 최신 상태
            }


        }
    }


    // 업데이트 취소나 실패 콜백
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                Log.e("MY_APP", "Update flow failed! Result code: $resultCode")

            }
        }
    }


    // 어플이 다시 불러와졌을 경우, 업데이트 이어서 계속 진행
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

