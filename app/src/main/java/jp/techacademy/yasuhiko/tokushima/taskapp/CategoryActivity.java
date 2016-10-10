package jp.techacademy.yasuhiko.tokushima.taskapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;

public class CategoryActivity extends AppCompatActivity {
    private EditText mCategoryEdit;
    private Category mCategory;
    private Task mTask;
    private  String mOldCategory = "";

    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (addCategory()) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // UI部品の設定
        mCategoryEdit = (EditText) findViewById(R.id.category_edit_text);
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);

        Intent intent = getIntent();
        mCategory = (Category) intent.getSerializableExtra(CategoryListActivity.EXTRA_CATEGORY);

        if (mCategory == null) {
            // 新規作成の場合

        } else {
            // 更新の場合
            mCategoryEdit.setText(mCategory.getCategory());
            mOldCategory = mCategory.getCategory();
        }
    }

    private boolean addCategory() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> categoryRealmResults;

        // カテゴリーが------やスペースだったら、メッセージを出してキャンセル
        if (mCategoryEdit.getText().toString().equals("------") ||
                mCategoryEdit.getText().toString().trim().equals("")) {
            // ダイアログを表示する
            AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);

            builder.setTitle("登録できない値");
            builder.setMessage("「" + mCategoryEdit.getText().toString() + "」は、登録できません。");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // メッセージ表示を行って、何もせずに抜ける
            return false;
        }

        // 重複するcategoryがあるかをチェック
        categoryRealmResults =
                realm.where(Category.class).equalTo(
                        "category",
                        mCategoryEdit.getText().toString()
                ).findAll();
        if (categoryRealmResults.size() > 0) {
            // ダイアログを表示する
            AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);

            builder.setTitle("値の重複");
            builder.setMessage("同じカテゴリーが既に存在しています。");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

            // メッセージ表示を行って、何もせずに抜ける
            return false;
        }

        // エラーや重複がなければ、この先に進んでデータを登録する
        // カテゴリーの追加・更新
        if (mCategory == null) {
            // 新規作成の場合
            mCategory = new Category();

            // category_idの最大値を求める
            categoryRealmResults = realm.where(Category.class).findAll();

            int identifier;
            if (categoryRealmResults.max("category_id") != null) {
                identifier = categoryRealmResults.max("category_id").intValue() + 1;
            } else {
                identifier = 0;
            }

            mCategory.setCategory_id(identifier);
        }

        String category = mCategoryEdit.getText().toString();
        mCategory.setCategory(category);

        realm.beginTransaction();
        realm.copyToRealmOrUpdate(mCategory);
        realm.commitTransaction();

        // 変更したカテゴリーと同じカテゴリーが設定されているタスクデータを更新する
        if (!mOldCategory.equals("")) {
            RealmResults<Task> taskRealmResults =
                    realm.where(Task.class).equalTo("category", mOldCategory).findAll();

            if (taskRealmResults.size() > 0) {
                realm.beginTransaction();
                while (taskRealmResults.size() > 0) {
                    mTask = taskRealmResults.get(0);
                    mTask.setCategory(category);
                }
                realm.commitTransaction();
            }
        }

        realm.close();

        return true;
    }
}
