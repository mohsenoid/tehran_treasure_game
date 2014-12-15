package com.tehran.treasure;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.tehran.util.Utils;
import com.tehran.util.Utils.FontName;

import java.util.Timer;
import java.util.TimerTask;

//import com.tehran.util.service.NotificationService;

public class MainActivity extends Activity implements OnClickListener {
    private static boolean isRegistered;
    // final private String tag = this.getClass().getName();
    final private Context context = this;
    SharedPreferences prefs;
    Dialog dialogHelp, dialogTreasure;
    private boolean doubleBackToExitPressedOnce;
    private Button btStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        isRegistered = prefs.getBoolean(Utils.P_REGISTERED, false);

//		startNotificationService();

        initForm();

        if (Utils.isFirstRun(context)) {
            showHelpDialog();
            Utils.setFirstRun(context, false);
        }
    }

//	private void startNotificationService() {
//		// if (!NotificationService.runFlag)
//		if (!Utils.isServiceRunning(context, NotificationService.class)) {
//			Intent iNotificationService = new Intent(this,
//					NotificationService.class);
//
//			startService(iNotificationService);
//		}
//	}

    private void initForm() {
        Utils.setupFont(context, FontName.Titr, R.id.btStart);
        Utils.setupFont(context, FontName.Titr, R.id.btHelp);
        Utils.setupFont(context, FontName.Titr, R.id.btExit);

        btStart = (Button) findViewById(R.id.btStart);
    }

    @Override
    public void onClick(View view) {
        Utils.playSound(context, R.raw.usekey);

        switch (view.getId()) {
            case R.id.btStart:
                if (isRegistered)
                    startBlocksActivity();
                else
                    startRegisterActivity();
                break;
            case R.id.btHelp:
                showHelpDialog();
                break;
            case R.id.btCloseHelp:
                if (dialogHelp != null)
                    dialogHelp.dismiss();
                break;
            case R.id.imgHeaderMain:
                showTreasureDialog();
                break;
            case R.id.btCloseTreasure:
                if (dialogTreasure != null)
                    dialogTreasure.dismiss();
                break;
            case R.id.btExit:
                Utils.exit(context);
                break;

            default:
                break;
        }
    }

    private void startRegisterActivity() {
        Intent iRegister = new Intent(context, RegisterActivity.class);
        startActivity(iRegister);
    }

    private void startBlocksActivity() {
        Intent iBlocks = new Intent(context, BlocksActivity.class);
        startActivity(iBlocks);
    }

    private void showHelpDialog() {
        dialogHelp = new Dialog(context);
        dialogHelp.requestWindowFeature(Window.FEATURE_LEFT_ICON);// .FEATURE_LEFT_ICON);
        dialogHelp.setContentView(R.layout.dialog_help);
        dialogHelp.setCancelable(true);
        dialogHelp.setTitle(R.string.help_title);

        Button btBack = (Button) dialogHelp.findViewById(R.id.btCloseHelp);
        btBack.setOnClickListener(this);

        dialogHelp.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialogHelp.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                R.drawable.ic_help);

        TextView tvHelp = (TextView) dialogHelp.findViewById(R.id.tvHelp);

        Utils.setupFont(context, FontName.Zar,
                (TextView) dialogHelp.findViewById(R.id.tvHelp));

        Utils.setupFont(context, FontName.Titr, (TextView) btBack);

        dialogHelp.show();
    }

    private void showTreasureDialog() {
        dialogTreasure = new Dialog(context);
        // dialogTreasure.requestWindowFeature(Window.FEATURE_LEFT_ICON);//
        // .FEATURE_LEFT_ICON);
        dialogTreasure.setContentView(R.layout.dialog_treasure);
        dialogTreasure.setCancelable(true);
        dialogTreasure.setTitle(R.string.treasure_title);

        Button btBack = (Button) dialogTreasure
                .findViewById(R.id.btCloseTreasure);
        btBack.setOnClickListener(this);

        dialogTreasure.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // dialogTreasure.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
        // R.drawable.ic_help);

        Utils.setupFont(context, FontName.Zar,
                (TextView) dialogTreasure.findViewById(R.id.tvTreasure));

        Utils.setupFont(context, FontName.Titr, (TextView) btBack);

        dialogTreasure.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this); // Google Analytic
    }

    @Override
    protected void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this); // Google Analytic
    }

    @Override
    protected void onResume() {
        doubleBackToExitPressedOnce = false;

        isRegistered = prefs.getBoolean(Utils.P_REGISTERED, false);
        if (isRegistered)
            btStart.setText(R.string.btStart);
        else
            btStart.setText(R.string.btRegister);

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.msg_exit, Toast.LENGTH_SHORT).show();

        Timer t = new Timer();
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2500);

    }

}
