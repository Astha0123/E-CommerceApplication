package com.example.e_commerceapplication

interface CartListener {
    fun showCartLayout(itemCount : Int)

    fun savingCartItemCount(itemCount : Int)

    fun hideCartLayout()
}