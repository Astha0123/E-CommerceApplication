package com.example.e_commerceapplication.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.example.e_commerceapplication.CartListener
import com.example.e_commerceapplication.Constants
import com.example.e_commerceapplication.R
import com.example.e_commerceapplication.Utils
import com.example.e_commerceapplication.adapters.AdapterCartProducts
import com.example.e_commerceapplication.databinding.ActivityOrderPlaceBinding
import com.example.e_commerceapplication.databinding.AddressLayoutBinding
import com.example.e_commerceapplication.models.Orders
import com.example.e_commerceapplication.viewModels.UserViewModel
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderPlaceBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var b2BPGRequest : B2BPGRequest
    private var cartListener : CartListener?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()
        backToUserMainActivity()
        getAllCartProducts()
        initializePhonePay()
        onPlaceOrderClicked()
    }

    private fun initializePhonePay() {
        val data =  JSONObject()
        PhonePe.init(this, PhonePeEnvironment.SANDBOX, Constants.MERCHANTID, "")

        data.put("merchantId" , Constants.MERCHANTID)
        data.put("merchantTransactionId" , Constants.merchantTransactionId)
        data.put("amount" , 200)    //Long. Mandatory
        data.put("mobileNumber" , "8294693400")   //String. Optional
        data.put( "callbackUrl", "https://webhook.site/callback-ur")   //String

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")

        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS",  "ANDROID")
        data.put( "deviceContext",  deviceContext)

        val payloadBase64 = Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()),Base64.NO_WRAP
        )

        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALT_KEY)+ "###1";

        b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()
    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold(""){ str, it-> str + "%02x".format(it)}
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener{
        viewModel.getAddressStatus().observe(this){status ->
            if(status){
                //proceed to payment
                getPaymentView()

            }else{
                val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))

                val alertDialog = AlertDialog.Builder(this)
                    .setView(addressLayoutBinding.root)
                    .create()
                alertDialog.show()

                addressLayoutBinding.btnAdd.setOnClickListener{
                    saveAddress(alertDialog,addressLayoutBinding)
                }
            }

        }
        }
    }

    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK){
            checkStatus()
        }
    }

    private fun checkStatus() {
        val xVerify = sha256("/pg/v1/status/${Constants.MERCHANTID}/${Constants.merchantTransactionId}${Constants.SALT_KEY}") + "###1"
        val headers = mapOf(
            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT- ID" to Constants.MERCHANTID,
        )
        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect{status ->
                if(status){
                    Utils.showToast(this@OrderPlaceActivity, "Payment Done")

                    saveOrder()
                    deleteCartProducts()
                    viewModel.savingCartItemCount(0)
                    cartListener?.hideCartLayout()

                     //order save, Delete products,

                    Utils.hideDialog()
                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                    finish()
                }else{
                    Utils.showToast(this@OrderPlaceActivity, "Payment not Done")
                }
            }
        }

    }

    private suspend fun deleteCartProducts() {
        viewModel.deleteCartProducts()
    }

    private fun saveOrder() {
        viewModel.getAll().observe(this){cartProductsList ->
            if(cartProductsList.isNotEmpty()){
                viewModel.getUserAddress {address->
                    val order = Orders(
                        orderId = Utils.getRandomId(),
                        orderList = cartProductsList,
                        userAddress = address,
                        orderStatus = 0,
                        orderDate = Utils.getCurrentDate(),
                        orderingUserId = Utils.getCurrentUserId()
                    )

                    viewModel.saveOrderedProducts(order)

                }
                for(products in cartProductsList){
                    val count = products.productCount
                    val stock = products.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductsAfterOrder(stock, products)
                    }
                }
            }

        }
    }

    private fun getPaymentView() {
        try {
            PhonePe.getImplicitIntent(this, b2BPGRequest,"com.phonepe.simulator" )
                .let {
                    phonePayView.launch(it)
                }
        }
        catch (e : PhonePeInitException){
            Utils.showToast(this, e.message.toString())

        }

    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
       Utils.showDialog(this, "Processing...")
        val userPinCode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptionAddress.text.toString()

        val address = "$userPinCode, $userDistrict($userState), $userAddress, $userPhoneNumber"


        lifecycleScope.launch {
            viewModel.saveUserAddress(address)
            viewModel.saveAddressStatus()
        }

        Utils.showToast(this, "Saved...")
        alertDialog.dismiss()
//        Utils.hideDialog()

        getPaymentView()
    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener{
            startActivity(Intent(this, UsersMainActivity::class.java))
            finish()

        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this){ cartProductList->
            adapterCartProducts = AdapterCartProducts()
            binding.rvProductsItem.adapter = adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            var totalPrice = 0

            for(products in cartProductList){
                val price  = products.productPrice?.substring(1)?.toInt()  //₹14 then it will be 14 as 0 index will be removed
                val itemCount =products.productCount!!
                totalPrice += (price?.times(itemCount)!! )
            }

            binding.tvSubTotal.text = totalPrice.toString()

            if(totalPrice < 300){
                binding.tvDeliveryCharge.text = "₹20"
                totalPrice += 20
            }

            binding.tvGrandTotal.text = totalPrice.toString()
        }
    }

    private fun setStatusBarColor(){
        window?.apply {
            val statusBarColors= ContextCompat.getColor(this@OrderPlaceActivity, R.color.colorPrimary)
            statusBarColor= statusBarColors
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}