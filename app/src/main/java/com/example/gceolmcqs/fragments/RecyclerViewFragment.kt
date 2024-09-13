package com.example.gceolmcqs.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.R
import com.example.gceolmcqs.adapters.SectionRecyclerAdapter
import com.example.gceolmcqs.datamodels.UserMarkedAnswersSheetData

private const val QUESTION_USER_ANSWER = "Question user answer"
private const val TITLE = "title"

class RecyclerViewFragment : Fragment() {
    private  var container: ViewGroup? = null
    private lateinit var layoutSvResult: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        this.container = container
        return inflater.inflate(R.layout.fragment_recycler_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv: RecyclerView = view.findViewById(R.id.rv)
        val layoutMan = LinearLayoutManager(requireContext())
        layoutMan.orientation = LinearLayoutManager.VERTICAL
        rv.layoutManager = layoutMan

        rv.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        val userMarkedAnswersSheet =
            requireArguments().getSerializable(QUESTION_USER_ANSWER) as UserMarkedAnswersSheetData
        val adapter = SectionRecyclerAdapter(
            requireContext(),
            requireArguments().getString(TITLE)!!,
            userMarkedAnswersSheet.questionsWithUserAnswerMarkedData
        )
        rv.adapter = adapter



//        layoutSvResult = view.findViewById(R.id.layoutSVResult)
//        for(index in 0..userMarkedAnswersSheet.questionsWithUserAnswerMarkedData.size){
//            val itemView = getQuestionItemView()
//        }

    }



//    private fun getQuestionItemView(): View{
//        return LayoutInflater.from(requireContext()).inflate(R.layout.question_card_item, this.container, false)
//
//    }
//
//    private fun setUpScrollView(){
//
//    }

    companion object {

        fun newInstance(
            title: String,
            userMarkedAnswersSheet: UserMarkedAnswersSheetData
        ): Fragment {
            val bundle = Bundle()
            bundle.putSerializable(QUESTION_USER_ANSWER, userMarkedAnswersSheet)
            bundle.putString(TITLE, title)
            val recyclerViewFragment = RecyclerViewFragment()
            recyclerViewFragment.arguments = bundle
            return recyclerViewFragment
        }

    }
}