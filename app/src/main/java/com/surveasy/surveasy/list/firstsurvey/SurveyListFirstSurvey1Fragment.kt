package com.surveasy.surveasy.list.firstsurvey


import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.surveasy.surveasy.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SurveyListFirstSurvey1Fragment() : Fragment() {

    val db = Firebase.firestore
    val firstSurveyModel by activityViewModels<FirstSurveyViewModel>()

    private lateinit var job : String
    private lateinit var major : String
    private lateinit var universityList : Array<String>
    private var university: String? = null
    private var military : String? = null
    private var engSurvey : Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_surveylistfirstsurvey1,container,false)
        val surveyListFirstSurvey1Btn : Button = view.findViewById(R.id.SurveyListFirstSurvey1_Btn)
        universityList = resources.getStringArray(R.array.university)

        // Set spinners
        setJobSpinner(view)
        setMajorSpinner(view)
        setSearchSpinner(view, universityList)

        // EngSurvey
        val engSurveyRadioGroup = view.findViewById<RadioGroup>(R.id.SurveyListFirstSurvey1_EngSurveyRadioGroup)
        engSurveyRadioGroup.setOnCheckedChangeListener { engSurveyRadioGroup, checked ->
            when(checked) {
                R.id.SurveyListFirstSurvey1_EngSurvey_O -> engSurvey = true
                R.id.SurveyListFirstSurvey1_EngSurvey_X -> engSurvey = false
            }
            Log.d(TAG, "**ENG*Eng** $engSurvey")
        }


        // military
        val militaryRadioGroup = view.findViewById<RadioGroup>(R.id.SurveyListFirstSurvey1_MilitaryRadioGroup)
        militaryRadioGroup.setOnCheckedChangeListener { militaryRadioGroup, checked ->
            when(checked) {
                R.id.SurveyListFirstSurvey1_MilitaryDone -> military = "??????"
                R.id.SurveyListFirstSurvey1_MilitaryYet -> military = "??????"
                R.id.SurveyListFirstSurvey1_MilitaryNA -> military = "????????????"
            }
        }

        // Next
        surveyListFirstSurvey1Btn.setOnClickListener {
            firstSurvey1(view)

        }



        return view
    }



    private fun firstSurvey1(view: View) {
        val etcUnivInput = view.findViewById<EditText>(R.id.SurveyListFirstSurvey1_EtcUniv)
        if((job == "?????????" && university == "??????") || (job == "?????????" && university == "")) university = etcUnivInput.text.toString()
        if((job == "????????????" && university == "??????") || (job == "????????????" && university == "")) university = etcUnivInput.text.toString()


        if(job == "????????? ??????????????????") Toast.makeText(context, "????????? ??????????????????.", Toast.LENGTH_SHORT).show()

        else if(job == "?????????" && major == "?????? ????????? ??????????????????") Toast.makeText(context, "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        else if(job == "?????????" && university == "???????????? ??????????????????") Toast.makeText(context, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        else if(job == "?????????" && university == "") Toast.makeText(context, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()

        else if(job == "????????????" && major == "?????? ????????? ??????????????????") Toast.makeText(context, "?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        else if(job == "????????????" && university == "???????????? ??????????????????") Toast.makeText(context, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        else if(job == "????????????" && university == "") Toast.makeText(context, "???????????? ??????????????????.", Toast.LENGTH_SHORT).show()

        else if(engSurvey == null) {
            Toast.makeText(context, "?????? ?????? ?????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        }

        else if(military == null) {
            Toast.makeText(context, "???????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show()
        }

        else {
            val firstSurvey1 = FirstSurvey(job, major, university, engSurvey, military,
                null, null, null, null, null, null)

            firstSurveyModel.firstSurvey = firstSurvey1
            Log.d(TAG, "FFFFFFFF first1 : ${firstSurveyModel.firstSurvey.military}")

            (activity as SurveyListFirstSurveyActivity).next()
        }

    }


    // Set spinners
    private fun setJobSpinner(view: View) {
        val jobList = resources.getStringArray(R.array.job)
        val jobAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, jobList)
        val jobSpinner : Spinner = view.findViewById(R.id.SurveyListFirstSurvey1_JobSpinner)
        val goneContainer : LinearLayout = view.findViewById(R.id.SurveyListFirstSurvey1_GoneContainer)

        jobSpinner.adapter = jobAdapter
        jobSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                job = jobList[position]
                if(position == 1 || position == 2) {
                    goneContainer.visibility = View.VISIBLE
                }
                else {
                    goneContainer.visibility = View.GONE
                    major = ""
                    university = ""
                }
                Log.d(TAG, "PPPPPPPPPPPPPPPPPPP $position" )
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setMajorSpinner(view: View) {
        val majorList = resources.getStringArray(R.array.major)
        val majorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, majorList)
        val majorSpinner : Spinner = view.findViewById(R.id.SurveyListFirstSurvey1_MajorSpinner)
        majorSpinner.adapter = majorAdapter
        majorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                major = majorList[position]
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setSearchSpinner(view: View, universityList: Array<String>) {
        val universityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, universityList)
        universityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val universitySpinner = view.findViewById<Spinner>(R.id.spinner_view)
        val etcUnivInput = view.findViewById<EditText>(R.id.SurveyListFirstSurvey1_EtcUniv)

        universitySpinner.adapter = universityAdapter

        universitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                university = universityList[position]

                if(university == "??????") {
                    etcUnivInput.visibility = View.VISIBLE
                    university = etcUnivInput.text.toString()
                }

                else etcUnivInput.visibility = View.GONE
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

    }




}