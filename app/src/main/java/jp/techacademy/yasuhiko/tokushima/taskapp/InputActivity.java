package jp.techacademy.yasuhiko.tokushima.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class InputActivity extends AppCompatActivity {
    private int mYear, mMonth, mDay, mHour, mMinute;
    private Button mDateButton, mTimeButton;
    private EditText mTitleEdit, mContextEdit;
    private Spinner mCategorySpinner;
    private Task mTask;
    private Realm mRealm;
    private RealmResults<Category> mCategoryRealmResults;

    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            setCategorySpinner();
        }
    };

    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        mYear = year;
                        mMonth = monthOfYear;
                        mDay = dayOfMonth;
                        String dateString = mYear + "/" +
                                String.format("%02d", (mMonth + 1)) + "/" +
                                String.format("%02d", mDay);
                        mDateButton.setText(dateString);
                    }
                }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };

    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" +
                                    String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        if (addTask()) {
            finish();
        }
        }
    };

    private View.OnClickListener mOnCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // カテゴリー編集画面に遷移
            Intent intent = new Intent(InputActivity.this, CategoryListActivity.class);
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mDateButton = (Button) findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);
        mTimeButton = (Button) findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);
        findViewById(R.id.category_button).setOnClickListener(mOnCategoryClickListener);
        mTitleEdit = (EditText) findViewById(R.id.title_edit_text);
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner1);
        mContextEdit = (EditText) findViewById(R.id.content_edit_text);

        Intent intent = getIntent();
        mTask = (Task) intent.getSerializableExtra(MainActivity.EXTRA_TASK);

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mCategoryRealmResults = mRealm.where(Category.class).findAll();
        mCategoryRealmResults.sort("category", Sort.ASCENDING);
        mRealm.addChangeListener(mRealmListener);

        // Category Spinnerのリストに値をセット
        setCategorySpinner();

        if (mTask == null) {
            // 新規作成の場合
            Calendar calendar = Calendar.getInstance();
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
        } else {
            // 更新の場合
            mTitleEdit.setText(mTask.getTitle());
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) mCategorySpinner.getAdapter();
            mCategorySpinner.setSelection(adapter.getPosition(mTask.getCategory()));
            mContextEdit.setText(mTask.getContents());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mTask.getDate());
            mYear = calendar.get(Calendar.YEAR);
            mMonth = calendar.get(Calendar.MONTH);
            mDay = calendar.get(Calendar.DAY_OF_MONTH);
            mHour = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" +
                    String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    }

    private boolean addTask() {
        Realm realm = Realm.getDefaultInstance();

        // タイトルの値チェック
        if (mTitleEdit.getText().toString().trim().equals("")) {
            // ダイアログを表示する
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

            builder.setTitle("登録できない値（タイトル）");
            builder.setMessage(
                    "タイトルが空白では登録できません。"
            );
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return false;
        }

        // カテゴリーの値チェック
        if (mCategorySpinner.getSelectedItem().toString().equals("------") ||
                mCategorySpinner.getSelectedItem().toString().trim().equals("")) {
            // ダイアログを表示する
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

            builder.setTitle("登録できない値（カテゴリー）");
            builder.setMessage(
                    "カテゴリーに「" + mCategorySpinner.getSelectedItem().toString() +
                            "」は、登録できません。" + "\n" +
                            "カテゴリを登録していない場合は、カテゴリー編集ボタンから" +
                            "カテゴリー編集画面に移動して登録して下さい。"
            );
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return false;
        }

        // タイトルの値チェック
        if (mContextEdit.getText().toString().trim().equals("")) {
            // ダイアログを表示する
            AlertDialog.Builder builder = new AlertDialog.Builder(InputActivity.this);

            builder.setTitle("登録できない値（内容）");
            builder.setMessage(
                    "内容が空白では登録できません。"
            );
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            return false;
        }

        if (mTask == null) {
            // 新規作成の場合
            mTask = new Task();

            RealmResults<Task> taskRealmResults = realm.where(Task.class).findAll();

            int identifier;
            if (taskRealmResults.max("id") != null) {
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        String title = mTitleEdit.getText().toString();
        String category = mCategorySpinner.getSelectedItem().toString();
        String content = mContextEdit.getText().toString();

        mTask.setTitle(title);
        mTask.setCategory(category);
        mTask.setContents(content);
        GregorianCalendar calender = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
        Date date = calender.getTime();
        mTask.setDate(date);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mTask);
        realm.commitTransaction();

        realm.close();

        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask);
        PendingIntent resultPendingIndent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calender.getTimeInMillis(), resultPendingIndent);

        return true;
    }

    // Realmからデータを読み込んで、CategorySpinnerに値をセットする
    private void setCategorySpinner() {
        String[] Category_Arr;

        if (mCategoryRealmResults.size() == 0) {
            Category_Arr = new String[1];
            Category_Arr[0] = "------";
        } else {
            Category_Arr = new String[mCategoryRealmResults.size()];
            for (int i = 0; i < mCategoryRealmResults.size(); i++) {
                Category_Arr[i] = mCategoryRealmResults.get(i).getCategory();
            }
        }

        setSpinner(mCategorySpinner, Category_Arr);
    }

    // Spinnerに値をセットする関数
    private void setSpinner(Spinner spinner, String[] arr) {
        ArrayAdapter adapter =
                new ArrayAdapter(this, android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
