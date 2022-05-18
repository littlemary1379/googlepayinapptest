package com.mary.onandoff.util

import android.app.Activity
import android.content.Context
import android.widget.TextView
import androidx.annotation.NonNull
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object GooglepayUtil : PurchasesUpdatedListener {

    val tempList = listOf("test2", "test3")

    interface GooglepayUtilDelegate {
        fun onProgress();
    }

    private const val TAG = "GooglepayUtil"

    private lateinit var billingCilent: BillingClient
    private var productDetailsList: List<ProductDetails> = mutableListOf()
    private lateinit var consumeListenser : ConsumeResponseListener


//    fun progressPay(activity: Activity) {
//        initBillingClient(activity)
//        //getPay(activity)
//    }

    /**
     * Billing Client 초기화 ->
     * BliiingClient : 결제 라이브러리 통신 인터페이스
     */
    fun initBillingClient(activity: Activity) {

        billingCilent = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingCilent.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                //연결이 종료될 시 재시도 요망
                DlogUtil.d(TAG, "연결 실패")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // 연결 성공
                    DlogUtil.d(TAG, "연결 성공")
                    DlogUtil.d(TAG, "billingCilent.connectionState : ${billingCilent.connectionState}")
                    //Suspend 함수는 반드시 코루틴 내부에서 실행
                    querySkuDetails(activity)

                }
            }

        })

        consumeListenser = ConsumeResponseListener { billingResult, purchaseToken ->
            DlogUtil.d(TAG, "billingResult.responseCode : ${billingResult.responseCode}")
            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                DlogUtil.d(TAG, "소모 성공")
            } else {
                DlogUtil.d(TAG, "소모 실패")
            }
        }

    }

    /**
     * 구매 가능 목록 호출
     * 필요하다면 API 구분 필요
     * */
    fun querySkuDetails(activity: Activity) {
        DlogUtil.d(TAG, "querySkuDetails")
//        val skuList = ArrayList<String>()
//        skuList.add("test2")
//        //skuList.add("gas")
//        val params = SkuDetailsParams.newBuilder()
//        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
//
//        //querySkuDetails 코틀린 확장 기능 활용
//        val skuDetailsResult = withContext(Dispatchers.IO) {
//            billingCilent.querySkuDetails(params.build())
//        }
//
//        if (skuDetailsResult.skuDetailsList.isNullOrEmpty()) {
//            DlogUtil.d(TAG, "skuDetailsResult list null")
//            ToastUtil.showShortToast(this, "목록 미노출")
//        } else {
//            ToastUtil.showShortToast(this, "목록 노출")
//            skuDetailsList = skuDetailsResult.skuDetailsList!!
//            for (i: Int in 0 until skuDetailsResult.skuDetailsList!!.size) {
//                DlogUtil.d(TAG, skuDetailsResult.skuDetailsList!![i].originalJson)
//            }
//        }

        var testMutableList = mutableListOf<QueryProductDetailsParams.Product>()
        for(element in tempList) {
            testMutableList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(element)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
        }

        DlogUtil.d(TAG, testMutableList.size)

        var testList = listOf(testMutableList)
        var testImmutableList = ImmutableList.of(testList)

        //5.0 마이그레이션
        val tempParam = QueryProductDetailsParams.newBuilder()
            .setProductList(
                testMutableList
            ).build()





        billingCilent.queryProductDetailsAsync(tempParam) { billingResult, mutableList ->
            DlogUtil.d(TAG, "????? ${mutableList.size}")
            productDetailsList = mutableList
            for (i: Int in 0 until mutableList!!.size) {
                DlogUtil.d(TAG, mutableList[i].name)
                //var productDetailsParams : BillingFlowParams.ProductDetailsParams =
                // tempList.add(productDetailsParams)
            }
            //getPay(activity)
        }

    }

    fun getPay(activity: Activity) {

        var list : MutableList<BillingFlowParams.ProductDetailsParams> = mutableListOf()

        for(i in 0 until productDetailsList.size) {
            if(productDetailsList[i].productId == "test2") {
                var flowProductDetailParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetailsList[i])
                    .build()

                list.add(flowProductDetailParams)
            }
        }

        var flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(list)
            .build()

        val responseCode = billingCilent.launchBillingFlow(activity, flowParams).responseCode
        DlogUtil.d(TAG, responseCode)
        DlogUtil.d(TAG, BillingClient.BillingResponseCode.OK)
    }

//    private fun setListener() {
//        textViewOneTimePayment.setOnClickListener {
//            DlogUtil.d(TAG, "click")
//
//            var flowProductDetailParams1 = BillingFlowParams.ProductDetailsParams.newBuilder()
//                .setProductDetails(productDetailsList[0])
//                .build()
//
//
//            var list : MutableList<BillingFlowParams.ProductDetailsParams> = mutableListOf()
//            list.add(flowProductDetailParams1)
//
//            var flowParams = BillingFlowParams.newBuilder()
//                .setProductDetailsParamsList(list)
//                .build()
//
//            val responseCode = billingCilent.launchBillingFlow(this, flowParams).responseCode
//            DlogUtil.d(TAG, responseCode)
//            DlogUtil.d(TAG, BillingClient.BillingResponseCode.OK)
//        }
//    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        DlogUtil.d(TAG, "???? ${billingResult.responseCode}")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                DlogUtil.d(TAG,"구매 성공")
                //ToastUtil.showShortToast(this, "구매 성공")
                // 거래 성공 코드
                // ?


//                handlePurchase(purchase)

                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingCilent.consumeAsync(consumeParams, consumeListenser)

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            //ToastUtil.showShortToast(this, "유저 취소")
            // 유저 취소 errorcode
        }
    }

//    override fun onPurchaseHistoryResponse(
//        p0: BillingResult,
//        p1: MutableList<PurchaseHistoryRecord>?
//    ) {
//        DlogUtil.d(TAG, "onPurchaseHistoryResponse ????????")
//        DlogUtil.d(TAG, p0.responseCode)
//        if(!p1.isNullOrEmpty()) run {
//            DlogUtil.d(TAG, p1[0].quantity)
//        }
//    }
}