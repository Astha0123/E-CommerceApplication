package com.example.e_commerceapplication.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.e_commerceapplication.databinding.ActivityMainBinding

class AuthMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
//        val navController= navHostFragment!!.findNavController()

//        val popupMenu= PopupMenu(this,null)
//        popupMenu.inflate(R.menu.bottom_nav)
//        binding.bottomBar.setupWithNavController(popupMenu.menu,navController)

//        navController.addOnDestinationChangedListener(object :NavController.OnDestinationChangedListener{
//            override fun onDestinationChanged(
//                controller: NavController,
//                destination: NavDestination,
//                arguments: Bundle?
//            ) {
//                title=when(destination.id){
//                    R.id.cartFragment -> "My Cart"
//                    R.id.moreFragment -> "My Dashboard"
//                    else -> "E-Commerce"
//                }
//            }
//
//        })
    }
}