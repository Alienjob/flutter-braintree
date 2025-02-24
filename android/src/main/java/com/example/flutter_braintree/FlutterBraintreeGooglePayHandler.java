package com.example.flutter_braintree;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;

import com.braintreepayments.api.googlepay.GooglePayClient;
import com.braintreepayments.api.googlepay.GooglePayLauncher;
import com.braintreepayments.api.googlepay.GooglePayRequest;
import com.braintreepayments.api.googlepay.GooglePayPaymentAuthRequest;
import com.braintreepayments.api.googlepay.GooglePayResult;
import com.braintreepayments.api.googlepay.GooglePayReadinessResult;
import com.braintreepayments.api.core.PaymentMethodNonce;
import com.braintreepayments.api.core.UserCanceledException;

import com.google.android.gms.wallet.WalletConstants;

public class FlutterBraintreeGooglePayHandler {
    private final FlutterBraintreeCustom activity;
    private final GooglePayClient googlePayClient;
    private final GooglePayLauncher googlePayLauncher;
    

    public FlutterBraintreeGooglePayHandler(FlutterBraintreeCustom activity) {
        this.activity = activity;

        String authorization = activity.getIntent().getStringExtra("authorization");

        this.googlePayClient = new GooglePayClient(activity, authorization);
        this.googlePayLauncher = new GooglePayLauncher(activity, paymentAuthResult -> {
            googlePayClient.tokenize(paymentAuthResult, result -> {
                if (result instanceof GooglePayResult.Success) {
                    GooglePayResult.Success success = (GooglePayResult.Success) result;
                    activity.onPaymentMethodNonceCreated(success.getNonce(), activity.createEmptyBillingAddress());
                } else if (result instanceof GooglePayResult.Failure) {
                    GooglePayResult.Failure failure = (GooglePayResult.Failure) result;
                    activity.onError(failure.getError());
                } else if (result instanceof GooglePayResult.Cancel) {
                    activity.onCancel();
                }
            });
        });
    }

    public void startGooglePaymentFlow(Intent intent) {
        try {
            googlePayClient.isReadyToPay(activity, readinessResult -> {
                Log.e("FlutterBraintreeGooglePayHandler", "startGooglePaymentFlow readinessResult = " + readinessResult);
                if (readinessResult instanceof GooglePayReadinessResult.ReadyToPay) {
                    GooglePayRequest request = createGooglePayRequest(intent);
                    googlePayClient.createPaymentAuthRequest(request, paymentAuthRequest -> {
                        Log.e("FlutterBraintreeGooglePayHandler", "startGooglePaymentFlow paymentAuthRequest = " + paymentAuthRequest);
                        if (paymentAuthRequest instanceof GooglePayPaymentAuthRequest.ReadyToLaunch) {
                            googlePayLauncher.launch(
                                (GooglePayPaymentAuthRequest.ReadyToLaunch) paymentAuthRequest
                            );
                        } else if (paymentAuthRequest instanceof GooglePayPaymentAuthRequest.Failure) {
                            GooglePayPaymentAuthRequest.Failure failure =
                                (GooglePayPaymentAuthRequest.Failure) paymentAuthRequest;
                            activity.onError(failure.getError());
                        } else {
                            activity.onError(new Exception("Unexpected paymentAuthRequest type"));
                        }
                    });
                } else {
                    activity.onError(new Exception("Google Pay is not ready"));
                }
            });
        } catch (Exception e) {
            Log.e("FlutterBraintreeGooglePayHandler", "startGooglePaymentFlow Error in Google Pay flow", e);
            activity.onError(e);
        }
    }

    private GooglePayRequest createGooglePayRequest(Intent intent) {
        String totalPrice = intent.getStringExtra("totalPrice");
        Log.d("FlutterBraintreeGooglePayHandler", "totalPrice = " + totalPrice);

        GooglePayRequest request = new GooglePayRequest(
            "USD", 
            totalPrice, 
            WalletConstants.TOTAL_PRICE_STATUS_FINAL
        );

        request.setBillingAddressRequired(true);
        request.setPhoneNumberRequired(true);
        request.setBillingAddressFormat(GooglePayRequest.BILLING_ADDRESS_FORMAT_FULL);

        return request;
    }

    public void checkGooglePayReady() {
        Log.d("FlutterBraintreeCustom", "checkGooglePayReady");
        googlePayClient.isReadyToPay(activity, readinessResult -> {
            Intent result = new Intent();
            if (readinessResult instanceof GooglePayReadinessResult.ReadyToPay) {
                // Success case - ready to pay
                result.putExtra("type", "isReadyToPay");
                result.putExtra("isReadyToPay", true);
                activity.setResult(FlutterBraintreeCustom.RESULT_OK, result);
            } else {
                // Not ready case
                result.putExtra("error", "Google Pay is not ready");
                activity.setResult(FlutterBraintreeCustom.RESULT_CANCELED, result);
            }
            activity.finish();
        });
    }

    
}