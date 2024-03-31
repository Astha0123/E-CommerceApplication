package com.example.e_commerceapplication.fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.e_commerceapplication.Constants
import com.example.e_commerceapplication.R
import com.example.e_commerceapplication.adapters.AdapterCategory
import com.example.e_commerceapplication.databinding.FragmentHomeBinding
import com.example.e_commerceapplication.models.Category
import com.example.e_commerceapplication.viewModels.UserViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel : UserViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  FragmentHomeBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAllCategories()
        navigatingToSearchFragment()
        get()
        return binding.root
    }


private fun get() {
    viewModel.getAll().observe(viewLifecycleOwner){
        for(i in it){
            Log.d("vvv", i.productTitle.toString())
            Log.d("vvv", i.productCount.toString())

        }
    }
}

    private fun navigatingToSearchFragment() {
        binding.searchCv.setOnClickListener{
            findNavController().navigate(R.id.action_home_Fragment_to_searchFragment2)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()

        for(i in 0 until Constants.allProductsCategoryIcon.size){
            categoryList.add(Category(Constants.allProductsCategory[i], Constants.allProductsCategoryIcon[i]))
        }

        binding.rvCategories.adapter= AdapterCategory(categoryList, ::onCategoryIconClicked)

    }

    fun onCategoryIconClicked(category: Category){
        val bundle = Bundle()
        bundle.putString("category", category.title)
        findNavController().navigate(R.id.action_home_Fragment_to_categoryFragment, bundle)
    }




    private fun setStatusBarColor(){
        activity?.window?.apply {
            val statusBarColors= ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            statusBarColor= statusBarColors
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


}