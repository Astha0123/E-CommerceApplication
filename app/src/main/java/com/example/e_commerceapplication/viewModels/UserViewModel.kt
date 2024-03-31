package com.example.e_commerceapplication.viewModels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.view.Display.Mode
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.e_commerceapplication.Constants
import com.example.e_commerceapplication.Utils
import com.example.e_commerceapplication.api.ApiUtilities
import com.example.e_commerceapplication.models.Orders
import com.example.e_commerceapplication.models.Product
import com.example.e_commerceapplication.models.Users
import com.example.e_commerceapplication.roomdb.CartProductDao
import com.example.e_commerceapplication.roomdb.CartProducts
import com.example.e_commerceapplication.roomdb.CartProductsDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow

class UserViewModel(application: Application): AndroidViewModel(application) {

    //initialization
    val sharedPreferences : SharedPreferences = application.getSharedPreferences("My Pref", MODE_PRIVATE)
    val cartProductDao:  CartProductDao = CartProductsDatabase.getDatabaseInstance(application).cartProductDao()


    private val _paymentStatus =  MutableStateFlow<Boolean>(false)
    val paymentStatus = _paymentStatus

    //Room DB
    fun insertCartProduct(products: CartProducts){
        cartProductDao.insertCartProduct(products)
    }


    //check
    fun getAll(): LiveData<List<CartProducts>>{
        return cartProductDao.getAllCartProducts()
    }

    suspend fun deleteCartProducts(){
        cartProductDao.deleteCartProducts()
    }


    fun updateCartProduct(products: CartProducts){
        cartProductDao.updateCartProduct(products)
    }

    fun deleteCartProduct(productId: String){
        cartProductDao.deleteCartProduct(productId)
    }

    //Firebase Call
    fun  fetchAllTheProducts(): Flow<List<Product>> = callbackFlow {
        val db =  FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts")

        val eventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()

                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)

                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        db.addValueEventListener(eventListener)

        awaitClose { db.removeEventListener(eventListener) }
    }
    fun getCategotyProduct(category: String) : Flow<List<Product>> = callbackFlow{
        val db= FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${category}")

        val eventListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = ArrayList<Product>()

                for(product in snapshot.children){
                    val prod = product.getValue(Product::class.java)
                    products.add(prod!!)

                }
                trySend(products)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }

        db.addValueEventListener(eventListener)

        awaitClose { db.removeEventListener(eventListener) }


    }

    fun updateItemCount(product : Product, itemCount: Int){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productRandomId}").child("itemCount").setValue(itemCount)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productRandomId}").child("itemCount").setValue(itemCount)

    }

    fun saveProductsAfterOrder(stock :Int, product: CartProducts){
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("itemCount").setValue(0)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("itemCount").setValue(0)



        
        FirebaseDatabase.getInstance().getReference("Admins").child("AllProducts/${product.productId}").child("productStock").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductCategory/${product.productCategory}/${product.productId}").child("itemCount").setValue(stock)
        FirebaseDatabase.getInstance().getReference("Admins").child("ProductType/${product.productType}/${product.productId}").child("productStockstock").setValue(stock)



    }

    fun saveUserAddress(address : String){
        FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress").setValue(address)

    }

    fun getUserAddress(callback : (String?)->Unit){
        val db = FirebaseDatabase.getInstance().getReference("AllUsers").child("Users").child(Utils.getCurrentUserId()).child("userAddress")

        db.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()){
                   val address = snapshot.getValue(String::class.java)
                   callback(address)
               }else{
                   callback(null)
               }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }

        })
    }

    fun saveOrderedProducts(orders: Orders){
        FirebaseDatabase.getInstance().getReference("Admins").child("Orders").child(orders.orderId!!).setValue(orders)
    }


    //sharePreferences
    fun savingCartItemCount(itemCount : Int){
        sharedPreferences.edit().putInt("itemCount",itemCount).apply()
    }

    fun fetchTotalItemCount() : MutableLiveData<Int>{
        val totalItemCount = MutableLiveData<Int>()
        totalItemCount.value = sharedPreferences.getInt("itemCount", 0)
        return totalItemCount
    }

    fun saveAddressStatus(){
        sharedPreferences.edit().putBoolean("addressStatus",true).apply()

    }

    fun getAddressStatus() : MutableLiveData<Boolean>{
        val status = MutableLiveData<Boolean>()
        status.value = sharedPreferences.getBoolean("addressStatus", false)
        return status
    }

    //retrofit to fetch

    suspend fun checkPayment(headers : Map<String, String>){
        val res = ApiUtilities.statusAPI.checkStatus(headers, Constants.MERCHANTID, Constants.merchantTransactionId)
        _paymentStatus.value = res.body() != null && res.body()!!.success
    }



}