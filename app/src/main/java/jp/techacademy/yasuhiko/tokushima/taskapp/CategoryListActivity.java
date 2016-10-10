package jp.techacademy.yasuhiko.tokushima.taskapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CategoryListActivity extends AppCompatActivity {
    public final static String EXTRA_CATEGORY = "jp.techacademy.yasuhiko.tokushima.taskapp.CATEGORY";

    private Realm mRealm;
    private RealmResults<Category> mCategoryRealmResults;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange() {
            reloadListView();
        }
    };
    private ListView mListView;
    private CategoryAdapter mCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        // ActionBarを設定する
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =
                        new Intent(CategoryListActivity.this, CategoryActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mCategoryRealmResults = mRealm.where(Category.class).findAll();
        mCategoryRealmResults.sort("category", Sort.ASCENDING);
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mCategoryAdapter = new CategoryAdapter(CategoryListActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップした時の処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 入力・編集画面に遷移
                Category category = (Category) adapterView.getAdapter().getItem(i);

                Intent intent = new Intent(CategoryListActivity.this, CategoryActivity.class);
                intent.putExtra(EXTRA_CATEGORY, category);

                startActivity(intent);
            }
        });

        // ListViewを長押しした時の処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                // タスクを削除する
                final Category category = (Category) adapterView.getAdapter().getItem(i);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(CategoryListActivity.this);

                builder.setTitle("削除");
                builder.setMessage("「" + category.getCategory() + "」を削除しますか？\n" +
                        "「" + category.getCategory() + "」が設定されたタスクは残りますので、" +
                        "手動で更新／削除して下さい。"
                );
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        RealmResults<Category> results =
                                mRealm.where(Category.class).equalTo(
                                        "category_id",
                                        category.getCategory_id()
                                ).findAll();

                        mRealm.beginTransaction();
                        results.clear();
                        mRealm.commitTransaction();

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
    }

    private void reloadListView() {
        ArrayList<Category> categoryArrayList = new ArrayList<>();

        for (int i = 0; i < mCategoryRealmResults.size(); i++) {
            Category category = new Category();

            category.setCategory_id(mCategoryRealmResults.get(i).getCategory_id());
            category.setCategory(mCategoryRealmResults.get(i).getCategory());

            categoryArrayList.add(category);
        }

        mCategoryAdapter.setCategoryArrayList(categoryArrayList);
        mListView.setAdapter(mCategoryAdapter);
        mCategoryAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
