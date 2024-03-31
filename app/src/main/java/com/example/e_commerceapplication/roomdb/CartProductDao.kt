package com.example.e_commerceapplication.roomdb

import android.adservices.adid.AdId
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CartProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCartProduct(products: CartProducts)

    @Update
    fun updateCartProduct(products: CartProducts)



    //check
    @Query("SELECT * FROM CartProducts")                   //it should be here if we want to see the data in app inspection.
    fun getAllCartProducts() : LiveData<List<CartProducts>>



    @Query("DELETE FROM CartProducts WHERE productId= :productId ")
    fun deleteCartProduct(productId: String)

    @Query("DELETE FROM CartProducts")
    suspend fun deleteCartProducts()
}