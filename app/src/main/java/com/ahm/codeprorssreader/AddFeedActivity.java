package com.ahm.codeprorssreader;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddFeedActivity extends AppCompatActivity {

    List<Feed> mFeedList = new ArrayList<>();
    EditText urlText;
    String url = null;
    ListView feedsListView;
    ArrayList<String> urls;
    int index;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_feed_layout);
        urlText = findViewById(R.id.url_text);
        //urlText.clearFocus();
        feedsListView = findViewById(R.id.feeds_listview);
        populateFeedsListView();
    }

    public void btn_add_cliced(View view) {
        new FetchFeed().execute((Void) null);
        new updateActivity().execute((Void) null);
    }

    private class updateActivity extends AsyncTask<Void, Void, Boolean>{

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

    public List<Feed> getFeed(InputStream input) throws XmlPullParserException, IOException {
        String title = null;
        String description = null;
        String link = null;
        boolean isItem = false;
        List<Feed> items = new ArrayList<>();
        try{
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input, null);
            parser.nextTag();
            while(parser.next() != XmlPullParser.END_DOCUMENT){
                int eventtype = parser.getEventType();
                String name = parser.getName();
                if(name == null)
                    continue;
                if(eventtype == XmlPullParser.END_TAG){
                    if(name.equalsIgnoreCase("item")){
                        isItem = false;
                    }
                    continue;
                }
                if(eventtype == XmlPullParser.START_TAG){
                    if(name.equalsIgnoreCase("item")){
                        isItem = true;
                        continue;
                    }
                }
                String result = "";
                if(parser.next() == XmlPullParser.TEXT){
                    result = parser.getText();
                    parser.nextTag();
                }
                if(name.equalsIgnoreCase("title")){
                    title = result;
                }
                else if(name.equalsIgnoreCase("description")){
                    description = result;
                }
                else if(name.equalsIgnoreCase("link")){
                    link = result;
                }
                if(title != null && description != null && link != null){
                    if(isItem){
                        Feed item = new Feed(title, description, link);
                        items.add(item);
                    }
                    else{

                    }
                    title = null;
                    description = null;
                    link = null;
                    isItem = false;
                }
            }
            return items;
        }finally{
            input.close();
        }
    }

    public void btn_exit_clicked(View view) {
        this.finish();
    }

    public void btn_select_clicked(View view) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("index", index);
        editor.putBoolean("isSelected", true);
        editor.putBoolean("isDeleted", false);
        editor.commit();
        this.finish();
    }


    private class FetchFeed extends AsyncTask<Void, Void, Boolean> {



        @Override
        protected void onPreExecute(){
            url = urlText.getText().toString();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if(TextUtils.isEmpty(url))
                return false;
            try{
                if(!url.startsWith("http://") && !url.startsWith("https://")){
                    url = "http://" + url;
                }
                URL feed_url = new URL(url);
                InputStream input = feed_url.openConnection().getInputStream();
                mFeedList = getFeed(input);
                return true;
            }catch(IOException e){
                //Log.e(Tag, "Error", e);
            }catch(XmlPullParserException e){
                //Log.e(Tag, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success){
            if(success){

                if (mFeedList.size() > 0){
                    //add the url to database
                    FeedDatabaseHandler db = new FeedDatabaseHandler(AddFeedActivity.this,
                            null, null, 1);
                    if(url != null){
                        db.addItem(url);
                        db.close();
                        Toast.makeText(AddFeedActivity.this, "Success",
                                Toast.LENGTH_SHORT).show();
                    }
                    mFeedList.clear();
                    url = null;
                    urlText.setText("");

                }

            }else{
                Toast.makeText(AddFeedActivity.this,
                        "The url failed.",
                        Toast.LENGTH_SHORT).show();
                url = null;
                urlText.setText("");
            }
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
