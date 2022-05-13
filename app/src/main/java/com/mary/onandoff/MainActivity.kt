package com.mary.onandoff

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.android.billingclient.api.*
import com.mary.onandoff.util.DlogUtil
import com.mary.onandoff.util.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), PurchasesUpdatedListener {

    private val TAG = "MainActivity"

    private lateinit var textViewOneTimePayment: TextView
    private lateinit var billingCilent: BillingClient
    private var skuDetailsList: List<SkuDetails> = mutableListOf()
    private lateinit var consumeListenser : ConsumeResponseListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initBillingClient()

        setListener()
    }

    private fun initView() {
        textViewOneTimePayment = findViewById(R.id.textViewOneTimePayment)
    }

    /**
     * Billing Client 초기화 ->
     * BliiingClient : 결제 라이브러리 통신 인터페이스
     */
    private fun initBillingClient() {
        billingCilent = BillingClient.newBuilder(this)
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
                    //Suspend 함수는 반드시 코루틴 내부에서 실행
                    CoroutineScope(Dispatchers.Main).launch {
                        querySkuDetails()
                    }

                }
            }

        })

        consumeListenser = ConsumeResponseListener { billingResult, purchaseToken ->
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
    suspend fun querySkuDetails() {
        DlogUtil.d(TAG, "querySkuDetails")
        val skuList = ArrayList<String>()
        skuList.add("test2")
        //skuList.add("gas")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        //querySkuDetails 코틀린 확장 기능 활용
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingCilent.querySkuDetails(params.build())
        }

        if (skuDetailsResult.skuDetailsList.isNullOrEmpty()) {
            DlogUtil.d(TAG, "skuDetailsResult list null")
            ToastUtil.showShortToast(this, "목록 미노출")
        } else {
            ToastUtil.showShortToast(this, "목록 노출")
            skuDetailsList = skuDetailsResult.skuDetailsList!!
            for (i: Int in 0 until skuDetailsResult.skuDetailsList!!.size) {
                DlogUtil.d(TAG, skuDetailsResult.skuDetailsList!![i].originalJson)
            }
        }
    }

    private fun setListener() {
        textViewOneTimePayment.setOnClickListener {
            DlogUtil.d(TAG, "click")

            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList[0])
                .build()

            val responseCode = billingCilent.launchBillingFlow(this, flowParams).responseCode
            DlogUtil.d(TAG, responseCode)
            DlogUtil.d(TAG, BillingClient.BillingResponseCode.OK)
        }
    }


    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        DlogUtil.d(TAG, "????")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                ToastUtil.showShortToast(this, "구매 성공")
                // 거래 성공 코드
                // ?
                //handlePurchase(purchase)

                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingCilent.consumeAsync(consumeParams, consumeListenser)

            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            ToastUtil.showShortToast(this, "유저 취소")
            // 유저 취소 errorcode
        }
    }

//    fun handlePurchase(purchase: Purchase) {
//        ToastUtil.showShortToast(this, "구매 핸들링")
//        //BillingClient#queryPurchasesAsync 혹은 onPurchasesUpdated에서 검색된 구매 목록
//
//        //제품에 대한 확인
//        //구매토큰의 권한 확인 필요
//        //유저에게 권한 부여
//        val consumeParams = ConsumeParams.newBuilder()
//            .setPurchaseToken(purchase.purchaseToken)
//            .build()
//        val consumeResult = withContext(Dispatchers.IO) {
//            billingCilent.consumePurchase(consumeParams)
//        }
//    }

}