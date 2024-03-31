package com.example.e_commerceapplication.models

import com.example.e_commerceapplication.roomdb.CartProducts
import java.util.Date

data class Orders(
    val orderId: String? = null,
    val orderList: List<CartProducts>? = null,
    val userAddress: String? = null,
    val orderStatus: Int? = 0,
    val orderDate: String? = null,
    val orderingUserId: String? = null,
)
