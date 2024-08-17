package com.example.gceolmcqs.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gceolmcqs.MCQConstants
import com.example.gceolmcqs.R
//import com.example.gceolmcq.activities.OnPackageExpiredListener
import com.example.gceolmcqs.adapters.SectionNavigationRecyclerViewAdapter
//import com.example.gceolmcq.datamodels.SectionAnsweredScoreData
import com.example.gceolmcqs.viewmodels.SectionNavigationFragmentViewModel
class SectionNavigationFragment : Fragment(){
    private lateinit var onRecyclerItemClickListener: SectionNavigationRecyclerViewAdapter.OnRecyclerItemClickListener
//    private lateinit var onRestartPaperListener: OnRestartPaperListener

    private lateinit var viewModel: SectionNavigationFragmentViewModel

    private lateinit var tvSectionsAnswered: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvPaperGrade: TextView
    private lateinit var paperGradeLayout: LinearLayout
    private lateinit var rvSectionNav: RecyclerView
    private lateinit var restartPaperBtn: Button


    private lateinit var sectionNavigationRecyclerViewAdapter: SectionNavigationRecyclerViewAdapter

    private var fadeInOut: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fadeInOut = AnimationUtils.loadAnimation(requireContext(), R.anim.cross_fade)
        fadeInOut?.repeatMode = Animation.REVERSE

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_section_navigation, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        initHelperListeners(context)


    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        initViews(view)
        setupAdapters()
        setupViewObservers()
        setupViewListeners()

    }

    private fun initHelperListeners(context: Context){
        if (context is SectionNavigationRecyclerViewAdapter.OnRecyclerItemClickListener) {
            onRecyclerItemClickListener = context
        }

//        if (context is OnRestartPaperListener){
//            onRestartPaperListener = context
//        }
    }

    private fun initViews(view: View){
        tvPaperGrade = view.findViewById(R.id.tvPaperGrade)
        paperGradeLayout = view.findViewById(R.id.paperGradeLayout)
        rvSectionNav = view.findViewById(R.id.rvSectionNavigation)
        tvSectionsAnswered = view.findViewById(R.id.tvSectionsAnswered)
        tvScore = view.findViewById(R.id.tvScore)
        restartPaperBtn = view.findViewById(R.id.restartPaperBtn)
        restartPaperBtn.text = "${requireContext().resources.getString(R.string.restart)} ${requireActivity().title}"
    }

    private fun setupViewModel(){
        viewModel =
            ViewModelProvider(this)[SectionNavigationFragmentViewModel::class.java]
    }

    private fun setupAdapters(){
        val sectionNameBundleList = viewModel.getSectionNameBundleList()
        val rvLayoutMan = LinearLayoutManager(requireContext())
        rvLayoutMan.orientation = LinearLayoutManager.VERTICAL
        rvSectionNav.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        rvSectionNav.layoutManager = rvLayoutMan

        sectionNavigationRecyclerViewAdapter =
            SectionNavigationRecyclerViewAdapter(
                requireContext(),
                sectionNameBundleList!!,
                onRecyclerItemClickListener,
                viewModel.getSectionsAnswered()
            )
        rvSectionNav.adapter = sectionNavigationRecyclerViewAdapter
    }

    @SuppressLint("SetTextI18n")
    private fun setupViewObservers(){
        viewModel.getNumberOfSectionsAnswered().observe(viewLifecycleOwner) {
            tvSectionsAnswered.text =
                "$it of ${viewModel.getNumberOfSections()}"
            restartPaperBtn.isEnabled = it > 0
        }

        viewModel.getPaperScore().observe(viewLifecycleOwner){
            tvScore.text = "$it/${viewModel.getTotalNumberOfQuestions()}"

        }

        viewModel.getPaperGrade().observe(viewLifecycleOwner){
            tvPaperGrade.text = it
        }

        viewModel.getAreAllSectionsAnswered().observe(viewLifecycleOwner){
            if(it){
                paperGradeLayout.visibility = View.VISIBLE
                tvPaperGrade.startAnimation(fadeInOut)
            }else{
                paperGradeLayout.visibility = View.GONE
            }
        }

        viewModel.getPaperPercentage().observe(viewLifecycleOwner){
            if(it >= MCQConstants.MINIMUM_PASS_PERCENTAGE){
                tvPaperGrade.setTextColor(requireContext().resources.getColor(R.color.color_green))
            }else{
                tvPaperGrade.setTextColor(requireContext().resources.getColor(R.color.color_red))
            }
        }
    }

    private fun setupViewListeners(){
        restartPaperBtn.setOnClickListener {
//            onRestartPaperListener.onRestartPaper()
            displayRestartDialog()
        }
    }

    private fun resetPaperRepository(){
        viewModel.resetPaperRepo()
    }

    private fun displayRestartDialog(){
        val dialog = AlertDialog.Builder(requireContext()).apply {
            setMessage("${requireContext().resources.getString(R.string.restart)} ${requireActivity().title}?")
            setPositiveButton(requireContext().resources.getString(R.string.restart)){_, _ ->
                resetPaperRepository()
                setupAdapters()
            }
            setNegativeButton(requireContext().resources.getString(R.string.cancel)){_, _ ->}
        }.create()
        dialog.show()

    }

    override fun onResume() {
        super.onResume()
        sectionNavigationRecyclerViewAdapter.updateSectionScore(viewModel.getSectionsScores())
        sectionNavigationRecyclerViewAdapter.notifyDataSetChanged()
    }

    companion object {

        fun newInstance(): Fragment {
            return SectionNavigationFragment()
        }

    }

//    interface OnRestartPaperListener{
//        fun onRestartPaper()
//    }

}

