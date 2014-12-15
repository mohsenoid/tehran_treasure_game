package com.tehran.treasure;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.tehran.util.SmsSender;
import com.tehran.util.Utils;

import java.io.IOException;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("HandlerLeak")
public class BlocksActivity extends Activity implements OnClickListener {
    public final static String P_DEAL_POINT = "dealPoint";
    final static int blocksCount = 25;
    final static int blocksCellCount = 5;
    static int blocksRowCount;
    static int placeID, questionID, answersCount;
    static String placeName;
    static String placeDetails;
    static String placeAddress;
    static double placeLat;
    static double placeLon;
    final int animOffset = 50;
    final private Context context = this;
    final private String tag = this.getClass().getName();
    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            showPlaceDialog();
        }
    };
    ProgressDialog communicationDialog;
    DBhelper dHelper;
    int[] dealPoints;
    Button btDeal;
    TextView tvAnswersCount;
    Dialog dialogTreasure;
    boolean[] enabled;
    Dialog dialogPlace, dialogQuestion;
    int placeKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocks);

        dHelper = new DBhelper(context);

        dealPoints = context.getResources().getIntArray(R.array.dealPoints);

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

        btDeal = (Button) findViewById(R.id.btDeal);
        btDeal.setOnClickListener(this);

        tvAnswersCount = (TextView) findViewById(R.id.tvAnswersCount);

        Utils.setupFont(context, Utils.FontName.Davat, R.id.tvAnswersCountLable);
        Utils.setupFont(context, Utils.FontName.Titr, R.id.tvAnswersCount);
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("InlinedApi")
    private void initBlocks() {
        enabled = dHelper.getAnsweredBlocks();

        TableLayout tlBlocks = (TableLayout) findViewById(R.id.tlBlocks);
        tlBlocks.removeAllViews();

        int displayWidth = Utils.getDisplayWidth(context);
        int buttonWidth = displayWidth / 5;
        blocksRowCount = blocksCount / blocksCellCount;

        long startOffset = 0;
        // anim.setStartOffset(startOffset);

        // boolean[] answered = new boolean[blocksCount];

        TableLayout.LayoutParams rParams = new TableLayout.LayoutParams();
        if (Utils.getAndroidVersionInt() > 7)
            rParams.width = LayoutParams.MATCH_PARENT;
        else
            rParams.width = LayoutParams.FILL_PARENT;
        rParams.height = LayoutParams.WRAP_CONTENT;

        TableRow.LayoutParams bParams = new TableRow.LayoutParams();
        bParams.width = buttonWidth;
        bParams.height = buttonWidth;

        for (int i = 0; i < blocksRowCount; i++) {

            TableRow trBlocks = new TableRow(context);

            for (int j = 0; j < blocksCellCount; j++) {
                Animation anim = null;
                try {
                    anim = AnimationUtils.loadAnimation(context,
                            R.anim.block_appear);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                anim.setStartOffset(startOffset);

                Button btBlock = new Button(context);
                btBlock.setId(i * blocksCellCount + j);
                btBlock.setText(i * blocksCellCount + j + 1 + "");
                btBlock.setOnClickListener(this);
                btBlock.setLayoutParams(bParams);
                btBlock.setTag(i * blocksCellCount + j);

                btBlock.setAnimation(anim);
                Utils.setupFont(context, Utils.FontName.Titr, (TextView) btBlock);

                // btBlock.setEnabled(enabled[i * blocksCellCount + j]);
                if (enabled[i * blocksCellCount + j])
                    btBlock.setBackgroundResource(R.drawable.selector_action_block);
                else
                    btBlock.setBackgroundResource(R.drawable.selector_action_block_disabled);

                trBlocks.addView(btBlock);

                anim.start();
                startOffset += animOffset;
            }

            trBlocks.setGravity(Gravity.CENTER_HORIZONTAL);
            trBlocks.setLayoutParams(rParams);

            tlBlocks.addView(trBlocks);
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() >= 0 && view.getId() < blocksCount) {
            placeID = Integer.valueOf(view.getTag().toString());

            if (Utils.getAndroidVersionInt() >= 11)
                startBlockAnimation();
            else
                showPlaceDialog();

        } else {
            switch (view.getId()) {
                case R.id.btMap:
                    showPlaceOnMap();
                    break;
                case R.id.btScan:
                    startScan();
                    break;
                case R.id.btAnswer1:
                case R.id.btAnswer2:
                case R.id.btAnswer3:
                case R.id.btAnswer4:
                    showAnswerConfirm(Integer.valueOf(view.getTag().toString()));
                    break;
                case R.id.btDeal:
                    showDealMessage();
                    break;
                case R.id.btCloseTreasure:
                    if (dialogTreasure != null)
                        dialogTreasure.dismiss();
                    break;
                case R.id.imgHeader:
                    showTreasureDialog();
                    break;
                default:
                    break;
            }
        }
    }

    private void showAnswerConfirm(final int tag) {
        final Dialog answerDialog = new Dialog(context);
        answerDialog.setCancelable(true);

        answerDialog.setTitle(R.string.dialog_answer_title);

        answerDialog.setContentView(R.layout.dialog_answer);

        TextView tvAnswer = (TextView) answerDialog.findViewById(R.id.tvAnswer);
        tvAnswer.setText(context.getResources().getString(
                R.string.dialog_answer_text1)
                + " "
                + tag
                + " "
                + context.getResources()
                .getString(R.string.dialog_answer_text2));

        Button btAnswerConfirm = (Button) answerDialog
                .findViewById(R.id.btAnswerConfirm);
        btAnswerConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                answerDialog.dismiss();
                finalAnswer(tag);
            }
        });

        Button btAnswerCancel = (Button) answerDialog
                .findViewById(R.id.btAnswerCancel);
        btAnswerCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                answerDialog.dismiss();
            }
        });

        Utils.setupFont(context, Utils.FontName.Zar, tvAnswer);
        Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswerConfirm);
        Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswerCancel);

        answerDialog.show();
    }

    private void finalAnswer(int tag) {
        dHelper.insertPlaceAnswer(placeID, tag);
        dialogQuestion.dismiss();
        updateAnswersCounter();
    }

    private void showSendDeal(int i) {
        final Dialog dealDialog = new Dialog(context);
        dealDialog.setCancelable(true);

        dealDialog.setTitle(R.string.dialog_deal_title);

        dealDialog.setContentView(R.layout.dialog_deal);

        TextView tvDeal = (TextView) dealDialog.findViewById(R.id.tvDeal);
        tvDeal.setText(context.getResources().getStringArray(
                R.array.fix_deal_messages)[i]);

        Button btDealConfirm = (Button) dealDialog
                .findViewById(R.id.btDealConfirm);
        btDealConfirm.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                dealDialog.dismiss();
                sendDeal();
            }

            private void sendDeal() {
                String message = dHelper.getBlocksDealInfo();

                final SmsSender sms = new SmsSender(context, context
                        .getResources().getString(R.string.callCenter));

                try {
                    sms.sendSMS(message, new SmsSender.smsListener() {

                        @Override
                        public void OnSent() {
                            if (communicationDialog != null)
                                communicationDialog.dismiss();

                            Toast.makeText(context, R.string.deal_sent,
                                    Toast.LENGTH_LONG).show();

                            saveDealPoint();
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

                            showSendDealErrorDialog();
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

                    showSendDealErrorDialog();
                }
            }

            private void showSendDealErrorDialog() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setMessage(R.string.sms_error);
                builder.setTitle(R.string.sms_error_title);
                builder.setPositiveButton(R.string.retry,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                sendDeal();
                            }
                        });
                builder.setNegativeButton(R.string.exit,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
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

        });

        Button btDealCancel = (Button) dealDialog
                .findViewById(R.id.btDealCancel);
        btDealCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                dealDialog.dismiss();
            }
        });

        Utils.setupFont(context, Utils.FontName.Zar, tvDeal);
        Utils.setupFont(context, Utils.FontName.Titr, (TextView) btDealConfirm);
        Utils.setupFont(context, Utils.FontName.Titr, (TextView) btDealCancel);

        dealDialog.show();

        Utils.playSound(context, R.raw.tada);
    }

    private void showPlaceOnMap() {
        // String placeName = cur.getString(cur
        // .getColumnIndex(DBhelper.C_PLACE_NAME));

        // int zoom = 7;
        String uri = String.format(Locale.ENGLISH, "geo:%f,%f"// ?z=%d&q=%f,%f
                // (%s)" +
                , placeLat, placeLon// , zoom , lat, lon, placeName
        );
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);

    }

    private void startBlockAnimation() {
        Animation animation = AnimationUtils.loadAnimation(context,
                R.anim.block_open);

        Button block = (Button) findViewById(placeID);
        block.bringToFront();
        block.setAnimation(animation);

        animation.start();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.sendEmptyMessage(0);
            }

        }, 1000);

    }

    private void showPlaceDialog() {
        Cursor cur = dHelper.selectPlace(placeID);
        if (cur.moveToNext()) {
            placeName = cur
                    .getString(cur.getColumnIndex(DBhelper.C_PLACE_NAME));
            placeDetails = cur.getString(cur
                    .getColumnIndex(DBhelper.C_PLACE_DETAILS));
            placeAddress = cur.getString(cur
                    .getColumnIndex(DBhelper.C_PLACE_ADDRESS));
            placeLat = Double.valueOf(cur.getDouble(cur
                    .getColumnIndex(DBhelper.C_PLACE_LAT)));
            placeLon = Double.valueOf(cur.getDouble(cur
                    .getColumnIndex(DBhelper.C_PLACE_LON)));

            dialogPlace = new Dialog(context);
            dialogPlace.setContentView(R.layout.dialog_place);
            // dialogPlace.setTitle(R.string.dialog_place_title);
            dialogPlace.setCancelable(true);

            dialogPlace.setTitle(placeName);

            // TextView tvTitle = (TextView)
            // dialogPlace.findViewById(R.id.tvTitle);
            // tvTitle.setText(context.getResources().getStringArray(
            // R.array.placeNames)[placeID]);

            TextView tvDetails = (TextView) dialogPlace
                    .findViewById(R.id.tvDetails);
            tvDetails.setText(placeDetails);

            TextView tvAddress = (TextView) dialogPlace
                    .findViewById(R.id.tvAddress);
            tvAddress.setText(placeAddress);

            TextView tvAddressLabel = (TextView) dialogPlace
                    .findViewById(R.id.tvAddressLabel);

            ImageView imgPlace = (ImageView) dialogPlace
                    .findViewById(R.id.imgPlace);
            Bitmap bmp;
            try {
                bmp = BitmapFactory.decodeStream(context.getAssets().open(
                        "images/" + placeID + ".jpg"));
                imgPlace.setImageBitmap(bmp);
            } catch (IOException e) {
                // TODO Asset photo not found
                imgPlace.setVisibility(View.GONE);
                Log.d(tag, e.getMessage());
            }

            placeKey = context.getResources().getIntArray(R.array.placeKeys)[placeID];

            Button btScan = (Button) dialogPlace.findViewById(R.id.btScan);
            Button btMap = (Button) dialogPlace.findViewById(R.id.btMap);
            btScan.setOnClickListener(BlocksActivity.this);
            btMap.setOnClickListener(BlocksActivity.this);

            btScan.setEnabled(enabled[placeID]);

            Utils.setupFont(context, Utils.FontName.Zar, tvDetails);
            Utils.setupFont(context, Utils.FontName.Zar, tvAddress);
            Utils.setupFont(context, Utils.FontName.Zar, tvAddressLabel);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btScan);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btMap);

            dialogPlace.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            dialogPlace.show();
        }

    }

    private void startScan() {
        // Intent intent = new
        // Intent("com.google.zxing.client.android.SCAN");
        // intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
        // startActivityForResult(intent, READ_CODE);

        IntentIntegrator integrator = new IntentIntegrator(this, context
                .getResources().getString(R.string.qrcode_title), context
                .getResources().getString(R.string.qrcode_message), context
                .getResources().getString(R.string.qrcode_yes), context
                .getResources().getString(R.string.qrcode_no));
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // super.onActivityResult(requestCode, resultCode, data);
        // if (requestCode == READ_CODE)
        // if (resultCode == Activity.RESULT_OK) {
        // String contents = intent.getStringExtra("SCAN_RESULT");
        // String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
        //
        // Toast.makeText(context, format + ": " + contents,
        // Toast.LENGTH_LONG).show();
        // // t1.setText(contents);
        // // t2.setText(format);
        // // savePreferences();
        // // TODO: Do something here with it
        // }// if result_ok
        // Boolean result = false;

        try {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, intent);
            if (scanResult.getFormatName() != null) {
                if (scanResult.getFormatName().equals("QR_CODE")) {
                    if (scanResult.getContents() != null) {
                        String content = scanResult.getContents();
                        content = content.split(context.getResources()
                                .getString(R.string.qrism_code))[1].trim();
                        int blockKey = Integer.valueOf(content);

                        // Toast.makeText(context, "CODE=" + blockKey,
                        // Toast.LENGTH_LONG).show();

                        checkBlockKey(blockKey);

                    } else {
                        Toast.makeText(
                                context,
                                getResources().getString(
                                        R.string.invalid_qrcode),
                                Toast.LENGTH_SHORT).show();
                        throw new Exception("Invalid QR-Code scaned!");
                    }
                } else {
                    Toast.makeText(context,
                            getResources().getString(R.string.invalid_barcode),
                            Toast.LENGTH_SHORT).show();
                    throw new Exception("Not a QR-Code!");
                }
            } else {
                Toast.makeText(context,
                        getResources().getString(R.string.invalid_barcode),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(tag, e.getMessage());
        }
    }

    private void checkBlockKey(int blockKey) {
        if (blockKey == context.getResources().getIntArray(R.array.placeKeys)[placeID]) {
            // Toast.makeText(context, "Right Key! Show Question...",
            // Toast.LENGTH_LONG).show();
            showQuestionDialog();
        } else {
            Toast.makeText(context,
                    getResources().getString(R.string.wrong_qrcode_key),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void showQuestionDialog() {
        if (dialogPlace != null)
            dialogPlace.dismiss();

        Cursor cur1 = dHelper.selectPlace(placeID);
        if (cur1.moveToNext()) {
            if (cur1.getInt(cur1.getColumnIndex(DBhelper.C_PLACE_QUESTION_ID)) != 0) {
                questionID = cur1.getInt(cur1
                        .getColumnIndex(DBhelper.C_PLACE_QUESTION_ID));
            } else {
                int questionsCount = dHelper.getPlaceQuestionCount(placeID);
                if (questionsCount > 0) {
                    Random random = new Random();
                    int randomQuestion = random.nextInt(questionsCount) + 1;

                    Cursor cur2 = dHelper.selectAllPlaceQuestions(placeID);

                    for (int i = 0; i < randomQuestion; i++)
                        cur2.moveToNext();

                    questionID = cur2.getInt(cur2
                            .getColumnIndex(DBhelper.C_QUESTION_ID));

                    dHelper.insertPlaceQuestion(placeID, questionID);
                }
            }
        }

        Cursor cur3 = dHelper.selectQuestion(questionID);

        if (cur3.moveToNext()) {
            dialogQuestion = new Dialog(context);
            dialogQuestion.setContentView(R.layout.dialog_question);
            dialogQuestion.setCancelable(true);

            dialogQuestion.setTitle(placeName);

            TextView tvQuestion = (TextView) dialogQuestion
                    .findViewById(R.id.tvQuestion);
            tvQuestion.setText(cur3.getString(cur3
                    .getColumnIndex(DBhelper.C_QUESTION_TEXT)));

            Button btAnswer1 = (Button) dialogQuestion
                    .findViewById(R.id.btAnswer1);
            btAnswer1.setText(cur3.getString(cur3
                    .getColumnIndex(DBhelper.C_QUESTION_ANS1)));
            btAnswer1.setOnClickListener(this);

            Button btAnswer2 = (Button) dialogQuestion
                    .findViewById(R.id.btAnswer2);
            btAnswer2.setText(cur3.getString(cur3
                    .getColumnIndex(DBhelper.C_QUESTION_ANS2)));
            btAnswer2.setOnClickListener(this);

            Button btAnswer3 = (Button) dialogQuestion
                    .findViewById(R.id.btAnswer3);
            btAnswer3.setText(cur3.getString(cur3
                    .getColumnIndex(DBhelper.C_QUESTION_ANS3)));
            btAnswer3.setOnClickListener(this);

            Button btAnswer4 = (Button) dialogQuestion
                    .findViewById(R.id.btAnswer4);
            btAnswer4.setText(cur3.getString(cur3
                    .getColumnIndex(DBhelper.C_QUESTION_ANS4)));
            btAnswer4.setOnClickListener(this);

            Utils.setupFont(context, Utils.FontName.Zar, tvQuestion);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswer1);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswer2);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswer3);
            Utils.setupFont(context, Utils.FontName.Titr, (TextView) btAnswer4);

            dialogQuestion.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            dialogQuestion.show();
        }

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
        updateAnswersCounter();

        super.onResume();
    }

    private void saveDealPoint() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        Editor e = prefs.edit();
        e.putInt(P_DEAL_POINT, answersCount);
        e.commit();

        updateAnswersCounter();
    }

    private int loadDealPoint() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);

        return prefs.getInt(P_DEAL_POINT, 0);
    }

    private boolean checkDeal() {
        for (int dealPoint : dealPoints) {
            if (answersCount == dealPoint && dealPoint != loadDealPoint())
                return true;
        }
        return false;
    }

    private void showDealMessage() {
        for (int i = 0; i < dealPoints.length; i++)
            if (answersCount < dealPoints[i]) {
                Toast.makeText(
                        context,
                        context.getResources().getStringArray(
                                R.array.middle_deal_messages)[i],
                        Toast.LENGTH_LONG).show();
                break;
            } else if (answersCount == dealPoints[i]
                    && answersCount != loadDealPoint()) {
                showSendDeal(i);
                break;
            }
    }

    private void updateAnswersCounter() {
        answersCount = dHelper.getAnswersCount();

        tvAnswersCount.setText(answersCount + "");

        if (checkDeal()) {
            btDeal.setBackgroundResource(R.drawable.ic_action_start_gold);
            showDealMessage();
        } else
            btDeal.setBackgroundResource(R.drawable.ic_action_start);

        initBlocks();

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

        Utils.setupFont(context, Utils.FontName.Zar,
                (TextView) dialogTreasure.findViewById(R.id.tvTreasure));

        Utils.setupFont(context, Utils.FontName.Titr, (TextView) btBack);

        dialogTreasure.show();
    }

}
