package com.mary.onandoff.util;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GooglepayJavaUtil {

    public interface GooglepayJavaUtilDelegate {
        void onSuccess();
    }

    public GooglepayJavaUtil(@NotNull GooglepayJavaUtil.GooglepayJavaUtilDelegate GooglepayJavaUtilDelegate) {
        this.GooglepayJavaUtilDelegate = GooglepayJavaUtilDelegate;
    }

    private static final String TAG = "GooglepayJavaUtil";

    public String[] itemIdList;

    private BillingClient billingCilent;
    private List<ProductDetails> productDetailsList;
    private ConsumeResponseListener consumeListenser;
    private GooglepayJavaUtilDelegate GooglepayJavaUtilDelegate;

    private final PurchasesUpdatedListener purchaseUpdateListener = new PurchasesUpdatedListener() {
        @Override
        public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable @org.jetbrains.annotations.Nullable List<Purchase> list) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list != null) {
                for (Purchase purchase : list) {
                    DlogUtil.INSTANCE.d(TAG, "서버 통신 완료");

                    //소비로직(정합성 확인)
                    ConsumeParams consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();

                    billingCilent.consumeAsync(consumeParams, consumeListenser);
                }
            }
        }
    };


    public void initBillingClient(Context context) {

        billingCilent = BillingClient.newBuilder(context)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build();

        billingCilent.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                DlogUtil.INSTANCE.d(TAG, "연결 실패");
                handleBillingClientDisconnection(context);
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                DlogUtil.INSTANCE.d(TAG, "연결 성공");
                getPurchaseList();
            }
        });

        consumeListenser = (billingResult, s) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                DlogUtil.INSTANCE.d(TAG, "소모 성공");
                if(GooglepayJavaUtilDelegate != null) {
                    GooglepayJavaUtilDelegate.onSuccess();
                }
            } else {
                DlogUtil.INSTANCE.d(TAG, "소모 실패");
            }
        };

    }

    private void getPurchaseList() {

        List<QueryProductDetailsParams.Product> listParam = new ArrayList<>();

        if(itemIdList.length > 0) {
            for(String itemId : itemIdList) {
                listParam.add(
                        QueryProductDetailsParams.Product.newBuilder()
                                .setProductId(itemId)
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()
                );
            }
        } else {
            DlogUtil.INSTANCE.d(TAG, "no list");
            return;
        }

        QueryProductDetailsParams itemParam = QueryProductDetailsParams.newBuilder()
                .setProductList(listParam)
                .build();

        billingCilent.queryProductDetailsAsync(itemParam, new ProductDetailsResponseListener() {
            @Override
            public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull List<ProductDetails> list) {
                DlogUtil.INSTANCE.d(TAG, list);
                productDetailsList = list;
            }
        });

    }

    public void callPay(@NotNull Activity activity, @NotNull String productId) {
        List<BillingFlowParams.ProductDetailsParams> list = new ArrayList<>();

        for(ProductDetails productDetail : productDetailsList) {
            if(productDetail.getProductId().equals(productId)) {
                BillingFlowParams.ProductDetailsParams productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetail)
                        .build();
                list.add(productDetailsParams);
            }
        }

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(list)
                .build();

        billingCilent.launchBillingFlow(activity, flowParams);
    }

    private void handleBillingClientDisconnection(Context context) {

        if (billingCilent.getConnectionState() != 2) {
            new Handler(Looper.myLooper()).postDelayed(() -> initBillingClient(context),2000);
        }

    }

    public void disconnetion() {
        billingCilent.endConnection();
    }

}
