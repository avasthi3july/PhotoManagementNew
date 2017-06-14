package com.tagmypicture.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;


import com.tagmypicture.R;
import com.tagmypicture.inapppurchase.IabBroadcastReceiver;
import com.tagmypicture.inapppurchase.IabHelper;
import com.tagmypicture.inapppurchase.IabResult;
import com.tagmypicture.inapppurchase.Inventory;
import com.tagmypicture.inapppurchase.Purchase;
import com.tagmypicture.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kavasthi on 5/9/2017.
 */

public abstract class InAppPurchaseActivity
        extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener,
        DialogInterface.OnClickListener {
    static final String TAG = "TrivialDrive";
    boolean mIsPremium = false;
    boolean mSubscribedToInfiniteGas = false;
    boolean mAutoRenewEnabled = false;
    boolean mSubscribedToInfiniteGasYear = false;
    boolean mAutoRenewEnabledYear = false;
    String mInfiniteGasSku = "";
    String mFirstChoiceSku = "";
    String mSecondChoiceSku = "";
    String mSelectedSubscriptionPeriod = "";
    static final String SKU_PREMIUM = "premium";
    static final String SKU_GAS = "gas";
    static final String SKU_INFINITE_GAS_MONTHLY = "tmp_email_message";
    static final String SKU_INFINITE_GAS_YEARLY = "tmp_remove_ad";
    static final int RC_REQUEST = 10001;
    static int[] TANK_RES_IDS = {R.drawable.gas0, R.drawable.gas1, R.drawable.gas2,
            R.drawable.gas3, R.drawable.gas4};
    static final int TANK_MAX = 4;
    int mTank;
    IabHelper mHelper;
    IabBroadcastReceiver mBroadcastReceiver;
    SharedPreferences.Editor editor;

    public abstract void updateUi();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inapp);
        loadData();
        //int response = mService.consumePurchase(3, getPackageName(), token);
        // String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAox0g9Lft415txHN4dU7KcSh32UQOVA8/IJWpTNMLkw1xfPNrLHmQLak5GC2+7ZrQjFO4YwhvFqa1SWgt+9DPdPCzyoRmNANYfY/4L+IiZhGtvqFusaCJZzsU/zIoy7+yOJkO7Euxxs2n2qI7HCCKVaH2AhETbaKWTznAhlECjW+ff1sl/wI2hUP3QI1IPpoSXUGFuptYycIdOjzyKCC0qI9xPGf1mvVlff2XWzVtpr1kB47EbruizOcSGqx76sAFBhrvVmgx0jzGPiTZqTk+nKFw3KLz3k/mcCIahkY+zzGN9MOjIQV/VhAwckkHLfa1al/Y9EUG1P+ebiu1wOckvwIDAQAB";
        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArtcJjjhHBe39c16LLHFuD0X6GZtJisbLdM7hzBxW2s1KjUgNpVgEZtmaoIktt66/Zl+9KKmjWdLDGfJD5OJZrcH6mGjq5kHg4qPeUqXbgKdz6YUwft+TbAK4LRwvC0KmxloxWkKOlnM2IMeXP/YR46gawpRVOdlhcDvD0NpHR5yzenHq+19m2sswc4HRFfu/5rX0tF3R1BuGsbDPagZwON7FUjCDt6slB6MIaUV3mqMUFWU4jlYjG1CGaUgm38a7Q2ywvN5pyn4TMRe0I8edIr8TyJr+aeMdrNkXWfgQHF87N/hwsm6Zow9fhejh2p75I6cEXaFEoNMWhNU/yc0ymQIDAQAB";
        if (base64EncodedPublicKey.contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please put your app's public key in MainActivity.java. See README.");
        }
        if (getPackageName().startsWith("com.example")) {
            throw new RuntimeException("Please change the sample's package name! See README.");
        }

        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.enableDebugLogging(true);
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");
                if (!result.isSuccess()) {
                    // complain("Problem setting up in-app billing: " + result);
                    return;
                }

                if (mHelper == null) return;

                mBroadcastReceiver = new IabBroadcastReceiver(InAppPurchaseActivity.this);
                IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                registerReceiver(mBroadcastReceiver, broadcastFilter);
                Log.d(TAG, "Setup successful. Querying inventory.");
                try {
                    mHelper.queryInventoryAsync(mGotInventoryListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    //complain("Error querying inventory. Another async operation in progress.");
                }
            }
        });
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished." + result);

            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }
            Log.d(TAG, "Query inventory was successful.");
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + mIsPremium);
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
            // Util.appClass(InAppPurchaseActivity.this).setmIsPremium(mIsPremium);
            Purchase gasMonthly = inventory.getPurchase(SKU_INFINITE_GAS_MONTHLY);
            Purchase gasYearly = inventory.getPurchase(SKU_INFINITE_GAS_YEARLY);
            if (gasMonthly != null && gasMonthly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_MONTHLY;
                mAutoRenewEnabled = true;
            } else if (gasYearly != null && gasYearly.isAutoRenewing()) {
                mInfiniteGasSku = SKU_INFINITE_GAS_YEARLY;
                mAutoRenewEnabledYear = true;
            } else {
                mInfiniteGasSku = "";
                mAutoRenewEnabled = false;
            }
            mSubscribedToInfiniteGas = (gasMonthly != null && verifyDeveloperPayload(gasMonthly));
            Log.d(TAG, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            editor.putBoolean("isPremium", mSubscribedToInfiniteGas);
            editor.commit();
            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;

            mSubscribedToInfiniteGasYear = (gasYearly != null && verifyDeveloperPayload(gasYearly));
            Log.d(TAG, "User11 " + (mSubscribedToInfiniteGasYear ? "HAS" : "DOES NOT HAVE")
                    + " infinite gas subscription.");
            editor.putBoolean("isPremium1", mSubscribedToInfiniteGasYear);
            editor.commit();
            if (mSubscribedToInfiniteGasYear) mTank = TANK_MAX;
            //Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
                Log.d(TAG, "We have gas. Consuming it.");
                try {
                    mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    //complain("Error consuming gas. Another async operation in progress.");
                }
                return;
            }

            //updateUi();
            //setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    @Override
    public void receivedBroadcast() {
        Log.d(TAG, "Received broadcast notification. Querying inventory.");
        try {
            mHelper.queryInventoryAsync(mGotInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            //complain("Error querying inventory. Another async operation in progress.");
        }
    }

    public void onBuyGasButtonClicked(View arg0) {
        Log.d(TAG, "Buy gas button clicked.");

        if (mSubscribedToInfiniteGas) {
            // complain("No need! You're subscribed to infinite gas. Isn't that awesome?");
            return;
        }

        if (mTank >= TANK_MAX) {
            //complain("Your tank is full. Drive around a bit!");
            return;
        }
        //setWaitScreen(true);
        Log.d(TAG, "Launching purchase flow for gas.");
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_GAS, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            complain("Error launching purchase flow. Another async operation in progress.");
            // setWaitScreen(false);
        }
    }

    public void onUpgradeAppButtonClicked(View arg0) {
        Log.d(TAG, "Upgrade button clicked; launching purchase flow for upgrade.");
        //setWaitScreen(true);
        String payload = "";

        try {
            mHelper.launchPurchaseFlow(this, SKU_PREMIUM, RC_REQUEST,
                    mPurchaseFinishedListener, payload);
        } catch (IabHelper.IabAsyncInProgressException e) {
            //complain("Error launching purchase flow. Another async operation in progress.");
            //  setWaitScreen(false);
        }
    }

    public void onInfiniteGasButtonClicked(View arg0, String type) {
        if (!mHelper.subscriptionsSupported()) {
            complain("Subscriptions not supported on your device yet. Sorry!");
            return;
        }

        CharSequence[] options;
        if ((!mSubscribedToInfiniteGas || !mAutoRenewEnabled) && type.equalsIgnoreCase("1")) {
            options = new CharSequence[1];
            options[0] = getString(R.string.subscription_period_monthly);
            // options[1] = getString(R.string.subscription_period_yearly);
            mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            // mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;
        } else if ((!mSubscribedToInfiniteGasYear || !mAutoRenewEnabledYear) && type.equalsIgnoreCase("2")) {
            options = new CharSequence[1];
            // options[0] = getString(R.string.subscription_period_monthly);
            options[0] = getString(R.string.subscription_period_yearly);
            mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
            // mSecondChoiceSku = SKU_INFINITE_GAS_YEARLY;
        } else {
            options = new CharSequence[1];
            if (mInfiniteGasSku.equals(SKU_INFINITE_GAS_MONTHLY)) {
                options[0] = getString(R.string.subscription_period_yearly);
                mFirstChoiceSku = SKU_INFINITE_GAS_YEARLY;
            } else {
                options[0] = getString(R.string.subscription_period_monthly);
                mFirstChoiceSku = SKU_INFINITE_GAS_MONTHLY;
            }
            mSecondChoiceSku = "";
        }

        int titleResId = 0;
        if (!mSubscribedToInfiniteGas) {
            titleResId = R.string.subscription_period_prompt;
        } /*else if (!mSubscribedToInfiniteGasYear) {
            titleResId = R.string.subscription_period_prompt;
        } else if (!mAutoRenewEnabled && !mAutoRenewEnabledYear) {
            titleResId = R.string.subscription_resignup_prompt;
        } else {
            titleResId = R.string.subscription_update_prompt;
        }*/

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleResId)
                .setSingleChoiceItems(options, 0 /* checkedItem */, this)
                .setPositiveButton(R.string.subscription_prompt_continue, this)
                .setNegativeButton(R.string.subscription_prompt_cancel, this);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(DialogInterface dialog, int id) {
        if (id == 0 /* First choice item */) {
            mSelectedSubscriptionPeriod = mFirstChoiceSku;
        } else if (id == 1 /* Second choice item */) {
            mSelectedSubscriptionPeriod = mSecondChoiceSku;
        } else if (id == DialogInterface.BUTTON_POSITIVE /* continue button */) {
            String payload = "";

            if (TextUtils.isEmpty(mSelectedSubscriptionPeriod)) {
                mSelectedSubscriptionPeriod = mFirstChoiceSku;
            }

            List<String> oldSkus = null;
            if (!TextUtils.isEmpty(mInfiniteGasSku) && !mInfiniteGasSku.equals(mSelectedSubscriptionPeriod)) {
                oldSkus = new ArrayList<String>();
                //oldSkus.add(mInfiniteGasSku);
                // oldSkus.add(mSelectedSubscriptionPeriod);
            }

            //  setWaitScreen(true);
            Log.d(TAG, "Launching purchase flow for gas subscription.");
            try {
                mHelper.launchPurchaseFlow(this, mSelectedSubscriptionPeriod, IabHelper.ITEM_TYPE_SUBS,
                        oldSkus, RC_REQUEST, mPurchaseFinishedListener, payload);
            } catch (IabHelper.IabAsyncInProgressException e) {
                complain("Error launching purchase flow. Another async operation in progress.");
                /// setWaitScreen(false);
            }
            mSelectedSubscriptionPeriod = "";
            mFirstChoiceSku = "";
            mSecondChoiceSku = "";
        } else if (id != DialogInterface.BUTTON_NEGATIVE) {
            Log.e(TAG, "Unknown button clicked in subscription dialog: " + id);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }

    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();
        return true;
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            Log.d(TAG, "Purchase finished: " + result.getMessage() + ", purchase: " + result.getResponse());
            if (mHelper == null) return;

            if (result.isFailure()) {
                if (result.getResponse() == 7) {
                    editor.putBoolean("isPremium", true);
                    editor.commit();
                    updateUi();
                } else System.out.println("FAIL>>>>" + result.getResponse());
                // complain("Error purchasing: " + result);
                // setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                //complain("Error purchasing. Authenticity verification failed.");
                // setWaitScreen(false);
                System.out.println("Success1111>>>>");
                return;
            } else System.out.println("Success>>>>");

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_GAS)) {
                Log.d(TAG, "Purchase is gas. Starting gas consumption.");
                try {
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                } catch (IabHelper.IabAsyncInProgressException e) {
                    //  complain("Error consuming gas. Another async operation in progress.");
                    // setWaitScreen(false);
                    return;
                }
            } else if (purchase.getSku().equals(SKU_PREMIUM)) {
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                editor.putBoolean("isPremium", true);
                editor.commit();
                updateUi();
                //setWaitScreen(false);
            } else if (purchase.getSku().equals(SKU_INFINITE_GAS_MONTHLY)) {
                Log.d(TAG, "Infinite gas subscription purchased.");
                //alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGas = true;
                mAutoRenewEnabled = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
                editor.putBoolean("isPremium", true);
                editor.commit();
                updateUi();
                // setWaitScreen(false);
            } else if (purchase.getSku().equals(SKU_INFINITE_GAS_YEARLY)) {
                Log.d(TAG, "Infinite gas subscription purchasedYear.");
                //alert("Thank you for subscribing to infinite gas!");
                mSubscribedToInfiniteGasYear = true;
                mAutoRenewEnabledYear = purchase.isAutoRenewing();
                mInfiniteGasSku = purchase.getSku();
                mTank = TANK_MAX;
                editor.putBoolean("isPremium1", true);
                editor.commit();
                updateUi();
                // setWaitScreen(false);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            if (mHelper == null) return;
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
                saveData();
                alert("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
            } else {
                // complain("Error while consuming: " + result);
            }
            //updateUi();
            // setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };

    public void onDriveButtonClicked(View arg0) {
        Log.d(TAG, "Drive button clicked.");
        if (!mSubscribedToInfiniteGas && mTank <= 0)
            alert("Oh, no! You are out of gas! Try buying some!");
        else {
            if (!mSubscribedToInfiniteGas) --mTank;
            saveData();
            alert("Vroooom, you drove a few miles.");
            //updateUi();
            Log.d(TAG, "Vrooom. Tank is now " + mTank);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
        Log.d(TAG, "Destroying helper.");
        if (mHelper != null) {
            mHelper.disposeWhenFinished();
            mHelper = null;
        }
    }

    /*{
     *//*   ((ImageView) findViewById(R.id.free_or_premium)).setImageResource(mIsPremium ? R.drawable.premium : R.drawable.free);

        findViewById(R.id.upgrade_button).setVisibility(mIsPremium ? View.GONE : View.VISIBLE);

        ImageView infiniteGasButton = (ImageView) findViewById(R.id.infinite_gas_button);
        if (mSubscribedToInfiniteGas) {
            infiniteGasButton.setImageResource(R.drawable.manage_infinite_gas);
        } else {
            infiniteGasButton.setImageResource(R.drawable.get_infinite_gas);
        }
        if (mSubscribedToInfiniteGas) {
            ((ImageView) findViewById(R.id.gas_gauge)).setImageResource(R.drawable.gas_inf);
        } else {
            int index = mTank >= TANK_RES_IDS.length ? TANK_RES_IDS.length - 1 : mTank;
            ((ImageView) findViewById(R.id.gas_gauge)).setImageResource(TANK_RES_IDS[index]);
        }*//*
    }*/
    /*void setWaitScreen(boolean set) {
        findViewById(R.id.screen_main).setVisibility(set ? View.GONE : View.VISIBLE);
        findViewById(R.id.screen_wait).setVisibility(set ? View.VISIBLE : View.GONE);
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        alert("Error: " + message);
    }*/

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this);
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }

    void saveData() {
        SharedPreferences.Editor spe = getPreferences(MODE_PRIVATE).edit();
        spe.putInt("tank", mTank);
        spe.apply();
        Log.d(TAG, "Saved data: tank = " + String.valueOf(mTank));
    }

    void loadData() {

        SharedPreferences pref = Util.getSharedPreferences(InAppPurchaseActivity.this);
        editor = pref.edit();
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        mTank = sp.getInt("tank", 2);
        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }

    void complain(String message) {
        Log.e(TAG, "**** TrivialDrive Error: " + message);
        //alert("Error: " + message);
    }
}
