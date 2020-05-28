package com.ahm.codeprorssreader;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DeleteItemActivity extends AppCompatActivity {

    ListView feedsListView;
    ArrayList<String> urls;
    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_item_layout);
        feedsListView = findViewById(R.id.feeds_listview);
        populateFeedsListView();
    }

    public void btn_delete_clicked(View view) {
        FeedDatabaseHandler db = new FeedDatabaseHandler(getApplicationContext()
                , null, null, 1);
        ArrayList<String> list = db.loadUrls();
        String item = list.get(index);
        db.deleteItem(item);
        db.close();
        new updateActivity().execute((Void) null);
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        //editor.putInt("index", index);
        editor.putBoolean("isDeleted", true);
        editor.commit();
        editor.clear();
    }

    public void exit_btn_clicked(View view) {

        this.finish();
    }

    private class updateActivity extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    populateFeedsListView();
                }
            });

            return true;
        }
    }

    private void populateFeedsListView(){
        FeedDatabaseHandler db = new FeedDatabaseHandler(this, null, null, 1);
        urls = db.loadUrls();
        db.close();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_single_choice, urls);
        feedsListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                index = arg2;
            }
        });
        feedsListView.setAdapter(adapter);
        feedsListView.setAdapter(new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_single_choice, urls) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getApplicationContext().getResources()
                        .getColor(R.color.colorPrimaryDark));
                textView.setBackgroundColor(getApplicationContext().getResources()
                        .getColor(R.color.colorPrimary));
                textView.setTextSize(12);

                return textView;
            }
        });
    }
}
