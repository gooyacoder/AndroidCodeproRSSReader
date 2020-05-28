package com.ahm.codeprorssreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    String mFeedtitle;
    String mFeeddescrition;
    String mFeedlink;
    List<Feed> mFeedList = new ArrayList<>();
    String Tag = "AHM";
    WebView rssView;
    String url = "";
    int index;
    ArrayList<String> urls = new ArrayList<>();
    Button feed_btn;
    Boolean isFeedSelected = false;
    Boolean isDeleted = false;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rssView = findViewById(R.id.rssText);
        feed_btn = findViewById(R.id.btn_feed);
        webView = findViewById(R.id.rssText);
        webView.setBackgroundResource(R.drawable.tileable_icon);
        webView.setBackgroundColor(0x00000000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rss_menu, menu);
        return true;
    }

    @Override
    public void onRestart(){
        super.onRestart();
        FeedDatabaseHandler db = new FeedDatabaseHandler(this, null, null, 1);
        urls = db.loadUrls();
        db.close();
        SharedPreferences prefs =
                PreferenceManager
                        .getDefaultSharedPreferences(getApplicationContext());
        index = prefs.getInt("index", 0);
        isFeedSelected = prefs.getBoolean("isSelected", false);
        isDeleted = prefs.getBoolean("isDeleted", false);

        if(isDeleted){
            Toast.makeText(this, "Database updated!", Toast.LENGTH_SHORT).show();
            isDeleted = false;
        }else {
            if (urls.size() > 0) {
                url = urls.get(index);
            }

            if (url != "" && isFeedSelected) {
                feed_btn.setText("Load Feed");
                feed_btn.setEnabled(true);
            } else {
                feed_btn.setEnabled(false);
                feed_btn.setText("Select the Feed from the Menu.");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.add_select:
                StartAddFeedActivity();
                return true;
            case R.id.delete:
                StartDeleteFeedActivity();
                return true;
            case R.id.exit:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void StartDeleteFeedActivity() {
        Intent i = new Intent(this, DeleteItemActivity.class);
        startActivity(i);
    }

    private void StartAddFeedActivity() {
        Intent i = new Intent(this, AddFeedActivity.class);
        startActivity(i);
    }

    public List<Feed> getFeed(InputStream input) throws XmlPullParserException, IOException{
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
                        mFeedtitle = title;
                        mFeeddescrition = description;
                        mFeedlink = link;
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

    public void onFeedButtonClicked(View view) {

        if(url != ""){
            new FetchFeed().execute((Void) null);
        }
    }


    private class FetchFeed extends AsyncTask<Void, Void, Boolean>{

        //String url;

        @Override
        protected void onPreExecute(){
            mFeedtitle = null;
            mFeeddescrition = null;
            mFeedlink = null;
            //url = "https://www.sciencedaily.com/rss/all.xml";
            //url = "http://rss.sciam.com/ScientificAmerican-News?format=xml";
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
                Log.e(Tag, "Error", e);
            }catch(XmlPullParserException e){
                Log.e(Tag, "Error", e);
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success){
            if(success){
                /*String FeedText = "";
                FeedText += "Feed Title: " + mFeedtitle;
                FeedText += "\n\nFeed Description: " + mFeeddescrition;
                FeedText += "\n\nFeed Lind: " + mFeedlink;
                rssView.setText(FeedText);*/
                StringBuilder FeedText = new StringBuilder();
                for(int i = 0; i < mFeedList.size(); ++i){
                    Feed item = mFeedList.get(i);

                    FeedText.append("<br /><br />Feed Title: " + item.title);
                    FeedText.append("<br /><br />Feed Description: " + item.description);
                    FeedText.append("<br /><br />Feed Link: " + item.link);

                }
                //rssView.setText(FeedText.toString());
                rssView.getSettings().setJavaScriptEnabled(true);
                rssView.loadDataWithBaseURL("", FeedText.toString(), "text/html",
                        "UTF-8", "");

            }else{
                Toast.makeText(MainActivity.this,
                        "Enter a valid rss feed url",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

}
