package com.surveasy.surveasy.home

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.amplitude.api.Amplitude
import com.surveasy.surveasy.MainActivity

import com.surveasy.surveasy.R
import com.surveasy.surveasy.list.*
import com.surveasy.surveasy.login.*
import com.surveasy.surveasy.home.Opinion.HomeOpinionDetailActivity
import com.surveasy.surveasy.home.Opinion.HomeOpinionViewModel
import com.surveasy.surveasy.home.banner.BannerViewModel
import com.surveasy.surveasy.home.banner.BannerViewPagerAdapter
import com.surveasy.surveasy.home.contribution.ContributionItemsAdapter
import com.surveasy.surveasy.home.contribution.HomeContributionViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.surveasy.surveasy.home.Opinion.HomeOpinionAnswerActivity
import com.surveasy.surveasy.my.history.MyViewHistoryActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject


class HomeFragment : Fragment() {

    val db = Firebase.firestore
    val userList = arrayListOf<UserSurveyItem>()
    private lateinit var bannerPager : ViewPager2
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val container : RecyclerView? = view.findViewById(R.id.homeList_recyclerView)
        val contributionContainer : RecyclerView = view.findViewById(R.id.HomeContribution_recyclerView)
        val userModel by activityViewModels<CurrentUserViewModel>()
        val bannerModel by activityViewModels<BannerViewModel>()
        val contributionModel by activityViewModels<HomeContributionViewModel>()
        val opinionModel by activityViewModels<HomeOpinionViewModel>()
        val model by activityViewModels<SurveyInfoViewModel>()
        val current_banner: TextView = view.findViewById(R.id.textView_current_banner)
        val total_banner: TextView = view.findViewById(R.id.textView_total_banner)
//        val springDotsIndicator: SpringDotsIndicator = view.findViewById(R.id.Home_spring_dots_indicator)
        //val register: Button = view.findViewById(R.id.HomeToRegister)
        //val login: Button = view.findViewById(R.id.HomeToLogin)
        val firstSurveyContainer : LinearLayout = view.findViewById(R.id.HomeList_ItemContainer_first)
        val firstSurveyTitle : TextView = view.findViewById(R.id.HomeListItem_Title_first)
        val homeListContainer: RecyclerView = view.findViewById(R.id.homeList_recyclerView)
        val noneText : TextView = view.findViewById(R.id.homeList_text)
        val greetingText: TextView = view.findViewById(R.id.Home_GreetingText)
        val surveyNum: TextView = view.findViewById(R.id.Home_SurveyNum)
        val totalReward: TextView = view.findViewById(R.id.Home_RewardAmount)
        val moreBtn : TextView = view.findViewById(R.id.homeList_Btn)
        val homeTopBox : LinearLayout = view.findViewById(R.id.Home_parSurvey_box)
        val opinionContainer: LinearLayout = view.findViewById(R.id.Home_Opinion_Q_Container)
        val opinionTextView : TextView = view.findViewById(R.id.Home_Opinion_TextView)
        val opinionAnswer : LinearLayout = view.findViewById(R.id.Home_Poll_answer_container)


        // Banner init
        bannerPager = view.findViewById(R.id.Home_BannerViewPager)
        val bannerDefault : ImageView = view.findViewById(R.id.Home_BannerDefault)

        Glide.with(this@HomeFragment).load(R.raw.app_loading).into(bannerDefault)
        CoroutineScope(Dispatchers.Main).launch {
            val banner = CoroutineScope(Dispatchers.IO).async {
                while (bannerModel.uriList.size == 0) {
                    //bannerDefault.visibility = View.VISIBLE
                }
                bannerDefault.visibility = View.INVISIBLE
                bannerModel.uriList

            }.await()

            total_banner.text = bannerModel.num.toString()
            bannerPager.offscreenPageLimit = bannerModel.num
            bannerPager.adapter = BannerViewPagerAdapter(mContext, bannerModel.uriList)
            bannerPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        }


        // Banner ????????? [?????? ?????????/?????? ?????????] ??????
        bannerPager.apply {
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    current_banner.text = "${position+1}"
                }
            })
        }

        homeTopBox.setOnClickListener {
            val intent = Intent(context, MyViewHistoryActivity::class.java)
            startActivity(intent)
        }



        moreBtn.setOnClickListener {
            if (userModel.currentUser.didFirstSurvey == false) {
                (activity as MainActivity).navColor_in_Home()
                (activity as MainActivity).moreBtn()

//                val intent_surveylistfirstsurvey: Intent =
//                    Intent(context, FirstSurveyListActivity::class.java)
//                intent_surveylistfirstsurvey.putExtra("currentUser_main", userModel.currentUser)
//                startActivity(intent_surveylistfirstsurvey)

            } else {
                (activity as MainActivity).clickList()
                (activity as MainActivity).navColor_in_Home()
            }
        }




        //user name, reward ????????????
        if (userModel.currentUser.uid != null) {
            greetingText.text = "???????????????, ${userModel.currentUser.name}???!"
            if(userModel.currentUser.UserSurveyList == null){
                surveyNum.text = "0???"
            }else{
                surveyNum.text = "${userModel.currentUser.UserSurveyList!!.size}???"
            }

            totalReward.text = "${userModel.currentUser.rewardTotal}???"
        } else {
            if (Firebase.auth.currentUser?.uid != null) {
                db.collection("panelData")
                    .document(Firebase.auth.currentUser!!.uid)
                    .get().addOnSuccessListener { document ->
                        greetingText.text = "???????????????, ${document["name"].toString()}???"
                        totalReward.text =
                            "${(Integer.parseInt(document["reward_total"].toString()))}???"
                    }

                db.collection("panelData").document(Firebase.auth.currentUser!!.uid)
                    .collection("UserSurveyList").get()
                    .addOnSuccessListener { document ->
                        var num = 0
                        for(item in document) {
                            num++
                        }
                        surveyNum.text = num.toString() + "???"
                    }

            } else {
                greetingText.text = "??????"
                totalReward.text = "$-----"
            }
        }

        //list ????????????
        CoroutineScope(Dispatchers.Main).launch {
            val list = CoroutineScope(Dispatchers.IO).async {
                val model by activityViewModels<SurveyInfoViewModel>()
                while (model.surveyInfo.size == 0) {
                    //Log.d(TAG, "########loading")
                }
                model.surveyInfo.get(0).id
            }.await()


            if (userModel.currentUser.didFirstSurvey == false) {
                firstSurveyContainer.visibility= View.VISIBLE
                noneText.visibility = View.GONE
                homeListContainer.visibility = View.GONE
                if (userModel.currentUser.uid != null) {
                    firstSurveyTitle.text = "${userModel.currentUser.name}?????? ?????? ???????????????!"
                }
                else {
                    if (Firebase.auth.currentUser?.uid != null) {
                        db.collection("panelData")
                            .document(Firebase.auth.currentUser!!.uid)
                            .get().addOnSuccessListener { document ->
                                firstSurveyTitle.text = "${document["name"].toString()}?????? ?????? ???????????????!"
                            }
                    }
                }

                firstSurveyContainer.setOnClickListener{
                    (activity as MainActivity).navColor_in_Home()
                    (activity as MainActivity).moreBtn()
                }

            }

            else if(userModel.currentUser.didFirstSurvey == true) {
                if (setHomeList(chooseHomeList()).size == 0) {
                    firstSurveyContainer.visibility= View.GONE
                    homeListContainer.visibility = View.GONE

                    noneText.text = "?????? ??????????????? ????????? ????????????"
                    noneText.visibility = View.VISIBLE
                }

                else {
                    firstSurveyContainer.visibility= View.GONE
                    noneText.visibility = View.GONE
                    homeListContainer.visibility = View.VISIBLE
                    val adapter = HomeListItemsAdapter(setHomeList(chooseHomeList()))
                    container?.layoutManager = LinearLayoutManager(
                        context,
                        LinearLayoutManager.VERTICAL, false
                    )
                    container?.adapter = HomeListItemsAdapter(setHomeList(chooseHomeList()))
                }

            }

        }


        // Contribution
        CoroutineScope(Dispatchers.Main).launch {
            val contributionList = CoroutineScope(Dispatchers.IO).async {
                while(contributionModel.contributionList.size == 0) { }
                contributionModel.contributionList
            }.await()

            Log.d(TAG, "+++++++++++ ${contributionList}")
            contributionContainer.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            contributionContainer.adapter = ContributionItemsAdapter(contributionList)

        }


        // Opinion
        CoroutineScope(Dispatchers.Main).launch {
            val opinionItem = CoroutineScope(Dispatchers.IO).async {
                while(opinionModel.opinionItem.question == null) { }
                opinionModel.opinionItem
            }.await()

            Log.d(TAG, "+++++++++++ ${opinionModel.opinionItem.question}")
            opinionTextView.text = opinionModel.opinionItem.question

        }

        opinionContainer.setOnClickListener {
            val intent = Intent(context, HomeOpinionDetailActivity::class.java)
            intent.putExtra("id", opinionModel.opinionItem.id)
            intent.putExtra("question", opinionModel.opinionItem.question)
            intent.putExtra("content1", opinionModel.opinionItem.content1)
            intent.putExtra("content2", opinionModel.opinionItem.content2)

            startActivity(intent)


            // [Amplitude] Poll View Showed
            val client = Amplitude.getInstance()
            client.logEvent("Poll View Showed")
        }

        opinionAnswer.setOnClickListener {
            val intent = Intent(context, HomeOpinionAnswerActivity::class.java)

            startActivity(intent)

            // [Amplitude] Poll_Answer View Showed
            val client = Amplitude.getInstance()
            client.logEvent("Poll_Answer View Showed")
        }





            return view
    }




    //?????? ??????, ?????? ?????? boolean list
    private fun chooseHomeList() : ArrayList<Boolean>{
        val userModel by activityViewModels<CurrentUserViewModel>()
        val model by activityViewModels<SurveyInfoViewModel>()
        val doneSurvey = userModel.currentUser.UserSurveyList
        var boolList = ArrayList<Boolean>(model.sortSurvey().size)
        var num: Int = 0

        //survey list item ????????? ?????? boolean type list ?????????. ?????? false ???
        while (num < model.surveyInfo.size) {
            boolList.add(false)
            num++
        }

        var index: Int = -1

        // userSurveyList ??? ????????? ????????? ????????? boolean ????????? ?????? ????????? ?????? true??? ??????
        if (doneSurvey?.size != 0) {
            if (doneSurvey != null) {
                for (done in doneSurvey) {
                    index = -1
                    for (survey in model.surveyInfo) {
                        index++
                        if (survey.id.equals(done.id)) {
                            boolList[index] = true
                        }else if(survey.progress >=3){
                            boolList[index] = true
                        }
                    }
                }
            }
        }else{
            index = -1
            for(survey in model.surveyInfo){
                index++
                boolList[index] = survey.progress>=3
            }
        }
        return boolList
    }

    //home list??? ????????? list return ??????
    private fun setHomeList(boolList : ArrayList<Boolean>) : ArrayList<SurveyItems>{
        val finList = arrayListOf<SurveyItems>()
        val model by activityViewModels<SurveyInfoViewModel>()
        var index = 0
        while(index < model.surveyInfo.size){
            if(!boolList[index]){
                finList.add(model.sortSurvey().get(index))
                index+=1
            }else{
                index+=1
            }

        }
        return finList
    }




}