package jp.techacademy.yasuhiko.tokushima.taskapp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import java.io.Serializable;

/**
 * Created by tokushima on 2016/10/08.
 */

public class Category extends RealmObject implements Serializable {
    private String category;    // カテゴリー名

    // category_id をプライマリーキーとして設定
    @PrimaryKey
    private int category_id;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategory_id() {
        return category_id;
    }

    public void setCategory_id(int category_id) {
        this.category_id = category_id;
    }
}
