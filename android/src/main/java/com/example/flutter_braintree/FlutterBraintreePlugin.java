package com.example.flutter_braintree;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.Map;
import java.util.HashMap;
import android.util.Log;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;

public class FlutterBraintreePlugin implements FlutterPlugin, ActivityAware, MethodCallHandler, ActivityResultListener {
    private static final int CUSTOM_ACTIVITY_REQUEST_CODE = 0x420;

    private Activity activity;
    private Result activeResult;

    private FlutterBraintreeDropIn dropIn;

    public static void registerWith(Registrar registrar) {
        Log.d("FlutterBraintreePlugin", "registerWith called");
        FlutterBraintreeDropIn.registerWith(registrar);
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_braintree.custom");
        FlutterBraintreePlugin plugin = new FlutterBraintreePlugin();
        plugin.activity = registrar.activity();
        registrar.addActivityResultListener(plugin);
        channel.setMethodCallHandler(plugin);
    }

    @Override
    public void onAttachedToEngine(FlutterPluginBinding binding) {
        Log.d("FlutterBraintreePlugin", "onAttachedToEngine called");
        final MethodChannel channel = new MethodChannel(binding.getBinaryMessenger(), "flutter_braintree.custom");
        channel.setMethodCallHandler(this);

        dropIn = new FlutterBraintreeDropIn();
        dropIn.onAttachedToEngine(binding);
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {
        Log.d("FlutterBraintreePlugin", "onDetachedFromEngine called");
        dropIn.onDetachedFromEngine(binding);
        dropIn = null;
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        Log.d("FlutterBraintreePlugin", "onAttachedToActivity called");
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        dropIn.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        Log.d("FlutterBraintreePlugin", "onDetachedFromActivityForConfigChanges called");
        activity = null;
        dropIn.onDetachedFromActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        Log.d("FlutterBraintreePlugin", "onReattachedToActivityForConfigChanges called");
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
        dropIn.onReattachedToActivityForConfigChanges(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        Log.d("FlutterBraintreePlugin", "onDetachedFromActivity called");
        activity = null;
        dropIn.onDetachedFromActivity();
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        Log.d("FlutterBraintreePlugin", "onMethodCall called with method: " + call.method);
        if (activeResult != null) {
            result.error("already_running", "Cannot launch another custom activity while one is already running.", null);
            return;
        }
        activeResult = result;

        if (call.method.equals("tokenizeCreditCard")) {
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "tokenizeCreditCard");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("cardNumber", (String) request.get("cardNumber"));
            intent.putExtra("expirationMonth", (String) request.get("expirationMonth"));
            intent.putExtra("expirationYear", (String) request.get("expirationYear"));
            intent.putExtra("cvv", (String) request.get("cvv"));
            intent.putExtra("cardholderName", (String) request.get("cardholderName"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("requestPaypalNonce")) {
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "requestPaypalNonce");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("amount", (String) request.get("amount"));
            intent.putExtra("currencyCode", (String) request.get("currencyCode"));
            intent.putExtra("displayName", (String) request.get("displayName"));
            intent.putExtra("payPalPaymentIntent", (String) request.get("payPalPaymentIntent"));
            intent.putExtra("payPalPaymentUserAction", (String) request.get("payPalPaymentUserAction"));
            intent.putExtra("billingAgreementDescription", (String) request.get("billingAgreementDescription"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("startThreeDSecureFlow")) {
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "startThreeDSecureFlow");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("nonce", (String) request.get("nonce"));
            intent.putExtra("amount", (String) request.get("amount"));
            intent.putExtra("email", (String) request.get("email"));
            intent.putExtra("surname", (String) request.get("surname"));
            intent.putExtra("givenName", (String) request.get("givenName"));
            Map billingAddress = (Map) request.get("billingAddress");
            intent.putExtra("billingAddress", new HashMap<>(billingAddress));

            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("startGooglePaymentFlow")) {
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "startGooglePaymentFlow");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            assert (call.argument("request") instanceof Map);
            Map request = (Map) call.argument("request");
            intent.putExtra("totalPrice", (String) request.get("totalPrice"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else if (call.method.equals("checkGooglePayReady")) {
            Intent intent = new Intent(activity, FlutterBraintreeCustom.class);
            intent.putExtra("type", "checkGooglePayReady");
            intent.putExtra("authorization", (String) call.argument("authorization"));
            activity.startActivityForResult(intent, CUSTOM_ACTIVITY_REQUEST_CODE);
        } else {
            result.notImplemented();
            activeResult = null;
        }
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("FlutterBraintreePlugin", "onActivityResult called with requestCode: " + requestCode + ", resultCode: " + resultCode);
        if (activeResult == null)
            return false;

        switch (requestCode) {
            case CUSTOM_ACTIVITY_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    String type = data.getStringExtra("type");
                    if (type.equals("paymentMethodNonce")) {
                        activeResult.success(data.getSerializableExtra("paymentMethodNonce"));
                    } else if (type.equals("isReadyToPay")) {
                        activeResult.success(data.getBooleanExtra("isReadyToPay", false));
                    } else {
                        Exception error = new Exception("Invalid activity result type.");
                        activeResult.error("error", error.getMessage(), null);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    String error = data.getStringExtra("error");
                    if (error != null) {
                        activeResult.error("error", error, null);
                    } else {
                        activeResult.success(null);
                    }
                } else {
                    Exception error = (Exception) data.getSerializableExtra("error");
                    activeResult.error("error", error.getMessage(), null);
                }
                activeResult = null;
                return true;
            default:
                return false;
        }
    }
}
