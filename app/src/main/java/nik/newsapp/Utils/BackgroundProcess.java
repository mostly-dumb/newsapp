package nik.newsapp.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import nik.newsapp.Adapters.RecyclerAdapter;
import nik.newsapp.AlarmReceiver;
import nik.newsapp.Views.Trending;

import static android.content.Context.ALARM_SERVICE;

public class BackgroundProcess {
    ArrayList<HashMap<String,String>> results = new ArrayList<>();
    ProgressDialog dialog;
    RecyclerView list;
    Context context;
    String url;
    int mode=0;
    HashMap<String,String> article= new HashMap<>();
    public  BackgroundProcess(String url,RecyclerView list, Context context){
        this.url=url;
        this.context=context;
        this.list=list;
        dialog=new ProgressDialog(context);
        dialog.setMessage("Fetching news for you :-)");
        dialog.setCancelable(false);

    }
    public BackgroundProcess(String url){
        this.url=url;
        mode=1;
    }
public void execute(){
if(dialog!=null)  dialog.show();
    xmlParser xmlparser = new xmlParser();
      Observable.fromCallable(() -> {
          try {
              URL feedURL=new URL(url);
              HttpURLConnection connection=(HttpURLConnection)feedURL.openConnection();
              connection.setRequestMethod("GET");
              InputStream inputStream = connection.getInputStream();
              results = xmlparser.processXML(inputStream);
              Log.d("RES", "getData: "+results.toString());
              return results;

          } catch (Exception e) {
              e.printStackTrace();
              return false;
          }
      }).subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe((result)->{
                  Log.d("check rs", "getData: "+results.toString());
                  Log.d("check r", "getData: "+result.toString());
                  if(mode==0) {
                      RecyclerAdapter recyclerAdapter = new RecyclerAdapter(context, results);
                      setTopArticle(results.get(0));
                      list.setAdapter(recyclerAdapter);

                        article=results.get(0);
                      Calendar calendar = Calendar.getInstance();
                      Intent intent1 = new Intent(context, AlarmReceiver.class);
                      if(article!=null && !article.isEmpty()) {
                          intent1.putExtra("article", article);
                      }
                      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,intent1, PendingIntent.FLAG_UPDATE_CURRENT);
                      AlarmManager am = (AlarmManager)context.getSystemService(ALARM_SERVICE);
                      if(am != null) {
                          am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 10, pendingIntent);
                      }






                      dialog.dismiss();
                  }
              });

}







    public void setTopArticle(HashMap<String,String> article){

        this.article=article;

        Log.d("setting", "setTopArticle: "+article.toString());
    }




}
