package com.tehran.treasure;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;

import com.tehran.util.SmsSender;
import com.tehran.util.SmsSender.smsListener;
import com.tehran.util.Utils;
import com.tehran.util.Utils.FontName;

public class RegisterActivity extends Activity implements OnClickListener {
    private final String tag = this.getClass().getName();
    private final Context context = this;
    ProgressDialog communicationDialog;
    EditText etFname, etLname, etMelliCode, etBirthYear, etEmail;
    RadioButton rbMale, rbFemale;
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        initForm();
    }

    private void initForm() {
        communicationDialog = new ProgressDialog(context);
        communicationDialog.setTitle(R.string.progress_title);
        communicationDialog.setMessage(context.getResources().getString(
                R.string.progress_text));
        communicationDialog.setIcon(android.R.drawable.ic_dialog_info);
        communicationDialog.setCancelable(false);
        // communicationDialog.setContentView(R.layout.dialog);

        etFname = (EditText) findViewById(R.id.etFname);
        etLname = (EditText) findViewById(R.id.etLname);
        etMelliCode = (EditText) findViewById(R.id.etMelliCode);
        etBirthYear = (EditText) findViewById(R.id.etBirthYear);
        etEmail = (EditText) findViewById(R.id.etEmail);

        rbMale = (RadioButton) findViewById(R.id.rbMale);
        rbFemale = (RadioButton) findViewById(R.id.rbFemale);

        Utils.setupFont(context, FontName.Davat, R.id.tvTitle);
        Utils.setupFont(context, FontName.Davat, R.id.tvRegisterDetails);
        Utils.setupFont(context, FontName.Titr, R.id.tvFname);
        Utils.setupFont(context, FontName.Titr, R.id.tvLname);
        Utils.setupFont(context, FontName.Titr, R.id.tvMelliCode);
        Utils.setupFont(context, FontName.Titr, R.id.tvSex);
        Utils.setupFont(context, FontName.Titr, R.id.tvBirthYear);
        Utils.setupFont(context, FontName.Titr, R.id.tvEmail);

        Utils.setupFont(context, FontName.Titr, R.id.btSend);

        Utils.setupFont(context, FontName.Zar, R.id.etFname);
        Utils.setupFont(context, FontName.Zar, R.id.etLname);
        Utils.setupFont(context, FontName.Zar, R.id.etMelliCode);
        Utils.setupFont(context, FontName.Zar, R.id.rbMale);
        Utils.setupFont(context, FontName.Zar, R.id.rbFemale);
        Utils.setupFont(context, FontName.Zar, R.id.etBirthYear);
        // Utils.setupFont(context, FontName.Zar, R.id.etEmail);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btSend:
                if (checkFormData())
                    confirmSendSMS();
                break;

            default:
                break;
        }
    }

    private void confirmSendSMS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setMessage(R.string.sms_confirm);
        builder.setTitle(R.string.sms_confirm_title);
        builder.setPositiveButton(R.string.send,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS();
                    }
                });
        builder.setNegativeButton(R.string.back, null);

        builder.show();

    }

    private Boolean checkFormData() {
        Boolean result = true;

        etFname.setError(null);
        etLname.setError(null);
        etMelliCode.setError(null);
        etEmail.setError(null);
        etBirthYear.setError(null);

        View focusView = null;

        if (!TextUtils.isEmpty(etEmail.getText().toString().trim())) {
            if (!etEmail.getText().toString().trim().contains("@")
                    || !(etEmail.getText().toString().trim().split("@").length == 2)
                    || !etEmail.getText().toString().trim().split("@")[1]
                    .contains(".")) {
                etEmail.setError(getString(R.string.strRegFormEmailError));
                focusView = etEmail;
                result = false;
            }
        }

        if (etBirthYear.getText().toString().trim().length() < 4) {
            etBirthYear.setError(getString(R.string.strRegFormBirthYearError));
            focusView = etBirthYear;
            result = false;
        } else if (Integer.valueOf(etBirthYear.getText().toString().trim()) < 1290
                || Integer.valueOf(etBirthYear.getText().toString().trim()) > 1389) {
            etBirthYear.setError(getString(R.string.strRegFormBirthYearError2));
            focusView = etBirthYear;
            result = false;
        }

        if (etMelliCode.getText().toString().trim().length() == 0) {
            etMelliCode.setError(getString(R.string.strRegFormMelliCodeError));
            focusView = etMelliCode;
            result = false;
        } else if (!Utils.checkMelliCode(etMelliCode.getText().toString()
                .trim())) {
            etMelliCode.setError(getString(R.string.strRegFormMelliCodeError2));
            focusView = etMelliCode;
            result = false;
        }

        if (etLname.getText().toString().trim().length() < 2) {
            etLname.setError(getString(R.string.strRegFormLnameError));
            focusView = etLname;
            result = false;
        }

        if (etFname.getText().toString().trim().length() < 2) {
            etFname.setError(getString(R.string.strRegFormFnameError));
            focusView = etFname;
            result = false;
        }

        if (result == false) {
            focusView.requestFocus();
        }

        return result;
    }

    private void sendSMS() {
        String message = createSmsMessage();

        final SmsSender sms = new SmsSender(context, context.getResources()
                .getString(R.string.callCenter));

        try {
            sms.sendSMS(message, new smsListener() {

                @Override
                public void OnSent() {
                    if (communicationDialog != null)
                        communicationDialog.dismiss();

                    savePlayerData();

                    showRegistryResult();
                }

                @Override
                public void OnSending() {
                    if (communicationDialog != null)
                        communicationDialog.dismiss();

                    communicationDialog.show();
                }

                @Override
                public void OnNotSent() {
                    if (communicationDialog != null)
                        communicationDialog.dismiss();

                    showRegisterErrorDialog();
                }

                @Override
                public void OnNotDelivered() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void OnDelivered() {
                    // TODO Auto-generated method stub

                }
            });

        } catch (Exception e) {
            Log.e(tag, e.getMessage());

            if (communicationDialog != null)
                communicationDialog.dismiss();

            showRegisterErrorDialog();
        }
    }

    private String createSmsMessage() {
        String result = "A.\n";
        try {
            result += String.format("0.\t%s-%s\n", context.getResources()
                    .getString(R.string.app_name), Utils
                    .getApplicationVersionName(context));

            result += String.format("1.\t%s\n", etFname.getText().toString()
                    .trim());
            result += String.format("2.\t%s\n", etLname.getText().toString()
                    .trim());
            result += String.format("3.\t%s\n", etMelliCode.getText()
                    .toString().trim());
            result += String.format("4.\t%s\n", rbMale.isChecked() ? "M" : "F");
            result += String.format("5.\t%s\n", etBirthYear.getText()
                    .toString().trim());
            result += String.format("6.\t%s\n", etEmail.getText().toString()
                    .trim());

        } catch (Exception e) {
            Log.e(tag, e.getMessage());
        }

        return result;
    }

    private void savePlayerData() {
        Editor editor = prefs.edit();
        editor.putString(Utils.P_FNAME, etFname.getText().toString().trim());
        editor.putString(Utils.P_LNAME, etLname.getText().toString().trim());
        editor.putString(Utils.P_MELLICODE, etMelliCode.getText().toString()
                .trim());
        editor.putString(Utils.P_SEX, rbMale.isChecked() ? "M" : "F");
        editor.putString(Utils.P_BIRTH_YEAR, etBirthYear.getText().toString()
                .trim());
        editor.putString(Utils.P_EMAIL, etEmail.getText().toString().trim());

        editor.putBoolean(Utils.P_REGISTERED, true);
        editor.commit();
    }

    private void showRegistryResult() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setMessage(context.getResources().getString(
                R.string.registration_sent));
        builder.setTitle(R.string.registration_sent_title);
        builder.setPositiveButton(R.string.btStart,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.exit(context);
                    }
                });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Utils.exit(context);
            }
        });

        builder.show();
    }

    private void showRegisterErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setMessage(R.string.sms_error);
        builder.setTitle(R.string.sms_error_title);
        builder.setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        sendSMS();
                    }
                });
        builder.setNegativeButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.exit(context);
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Utils.exit(context);
            }
        });

        builder.show();
    }
}
