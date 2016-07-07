package com.krolis.tipapp.dao;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.krolis.tipapp.model.Grade;
import com.krolis.tipapp.util.NoActiveSessionOnServer;
import com.krolis.tipapp.util.NoInternetConnection;
import com.krolis.tipapp.TIPApplication;
import com.krolis.tipapp.activity.LoginActivity;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import us.codecraft.xsoup.Xsoup;

/**
 * Created by Krolis on 2016-06-09.
 * As Singleton, cookies should be multiple to all activities/services
 */
public class GradeOnlineProvider extends GradeProvider{
    private static final String URL_HOME = "https://ehms13.pk.edu.pl/";

    private static GradeOnlineProvider mInstance = null;
    private static Object sync = new Object();
    private static Map<String, String> cookies;
    private static String login, pass;

    private static  List<String> semesters;

    private GradeOnlineProvider(){
        trustEveryone();
    }

    /**
     * @return A instance of GradeOnlineProvider.
     * Using this method and private constructor provides singleton implementation.
     */
    public static GradeOnlineProvider getInstance(){
        if(mInstance == null){
            synchronized (sync){
                if(mInstance == null)
                    mInstance = new GradeOnlineProvider();
            }
        }
        return mInstance;
    }


    /**
     *
     * @param login
     * @param pass
     * @return OFFLINE - there is no internet connection
     *          LOGIN_OK - server return response code 2xx
     *          LOGIN_WRONG - incorrect login and password
     * @throws IOException - on error in Connection.execute() method
     */
    @Override
    public synchronized int login(String login, String pass) throws IOException, NoInternetConnection {
        if(!isOnline()){
            throw new NoInternetConnection();
        }

        if(login == null || pass == null){
            return LOGIN_WRONG;
        }

        Connection connection = Jsoup.connect(URL_HOME);
        Connection.Response response = connection.method(Connection.Method.GET).timeout(6000).execute();
        cookies = response.cookies();
        Document doc = connection.get();

        Element loginElement =  Xsoup.compile("//*[@id=\"web_content\"]/table/tbody/tr/td/table/tbody/tr[1]/td/table/tbody/tr[1]/td/div/table/tbody/tr[3]/td[2]/input").evaluate(doc).getElements().get(0);
        Element password =  Xsoup.compile("//*[@id=\"web_content\"]/table/tbody/tr/td/table/tbody/tr[1]/td/table/tbody/tr[1]/td/div/table/tbody/tr[4]/td[2]/input").evaluate(doc).getElements().get(0);
        Element counterElement =  Xsoup.compile("//*[@id=\"web_content\"]/table/tbody/tr/td/table/tbody/tr[1]/td/table/tbody/tr[1]/td/div/table/tbody/input").evaluate(doc).getElements().get(0);

        Connection.Response loginResponse = Jsoup.connect("https://ehms13.pk.edu.pl/")
                .data(counterElement.attr("name"), counterElement.val())
                .data("log_form", "yes")
                .data(loginElement.attr("name"), login)
                .data(password.attr("name"), pass)
                .cookies(cookies)
                .method(Connection.Method.POST)
                .execute();

        cookies.putAll(loginResponse.cookies());

        if(isActiveOnlineSession()){

            this.login = login;
            this.pass = pass;
            return LOGIN_OK;
        }else{
            return LOGIN_WRONG;
        }
    }


    /**
     *
     * @param semester
     * @return List of grades in semester represented by param[0]
     *          null - there is no internet connection.
     *
     * @throws IOException
     */
    @Override
    public synchronized List<Grade> getGrades(String semester) throws IOException, NoInternetConnection, NoActiveSessionOnServer {
        if(!isOnline()){
            throw new NoInternetConnection();
        }
        try{
            if(!isActiveOnlineSession()){
                login(login,pass);
                if(!isActiveOnlineSession())
                    return null;
            }

            if(!semester.matches("..../..."))
                return null;

            String temp[] = semester.split("/");

            Document doc = Jsoup.connect("https://ehms13.pk.edu.pl/?tab=5&sub=8&RokAk=" + temp[0] + "%2F" + temp[1])
                    .cookies(cookies).get();

            Elements gradeElements = Xsoup.compile("//*[@id=\"content\"]/div/table/tbody/tr").evaluate(doc).getElements();
            gradeElements.remove(0);//remove headers

            List<Grade> grades = new LinkedList<Grade>();
            for(Element grade : gradeElements){
                Elements row = grade.select("td");
                if(!row.get(11).text().equals("nie wymaga oceny")){
                    HashMap<String, String> notes =  new HashMap<String, String>();
                    for(int i = 13; i<row.size(); i++){
                        if(!row.get(i).text().trim().isEmpty()){
                            String noteAndDate = row.get(i).text().trim();
                            String temp2[] = noteAndDate.split(" ");
                            if(temp2.length>=2)
                                notes.put(temp2[0], temp2[1]);
                            else{
                                /**
                                 * If split doesnt work.
                                 * I had case when i got only date, without note, probably something went wrong on server side.
                                 * It can be only note or only date, note has only 3 characters, date is longer.
                                 */
                                if(noteAndDate.length()>3)
                                    notes.put("",noteAndDate);
                                else
                                    notes.put(noteAndDate,"");
                            }
                        }
                    }
                    grades.add(new Grade(row.get(4).text(),notes));
                }
            }
            return grades;
        }catch (SSLHandshakeException e){
            e.printStackTrace();
            return null;
        }catch (IndexOutOfBoundsException e){
            throw new NoActiveSessionOnServer();
        }
    }

    @Override
    public synchronized List<String> getSemesters() throws IOException, NoInternetConnection, NoActiveSessionOnServer {
        if(!isOnline()){
            throw new NoInternetConnection();
        }

        if(semesters==null){
            semesters = new LinkedList<String>();

            if(!isActiveOnlineSession()){
                login(login,pass);
                if(!isActiveOnlineSession())
                    return null;
            }

            Document doc = Jsoup.connect("https://ehms13.pk.edu.pl/?tab=5&sub=8")
                    .cookies(cookies).get();
            Elements selectsOptions =  Xsoup.compile("//*[@id=\"myform\"]/select/option").evaluate(doc).getElements();
            if(selectsOptions==null)
                throw new NoActiveSessionOnServer();
            for (Element option :selectsOptions){
                semesters.add(option.val());
            }
        }
        return  semesters;
    }

    public boolean isActiveOnlineSession() {
        if(cookies!= null)
            return cookies.containsKey("eHMSdsys_tokid");
        else
            return false;
    }

    @Override
    public synchronized int logout(){
        try{
            if(!isOnline()){
                if(cookies!=null)
                    cookies.clear();
                return OFFLINE;
            }
            if(cookies!= null){
                Connection.Response loginResponse = Jsoup.connect("https://ehms13.pk.edu.pl/?action=logout")
                        .cookies(cookies)
                        .method(Connection.Method.GET)
                        .execute();
                cookies.clear();

                if(loginResponse.statusCode()<200 || loginResponse.statusCode() >=300){
                    return LOGOUT_WRONG;
                }
            }
            return LOGOUT_OK;
        }catch (IOException e){
            e.printStackTrace();
            return LOGOUT_WRONG;
        }
    }

    /**
     * To avoid SSL errors
     */
    private static void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }

    public static Thread getLogoutThread(final Context context){
        return new Thread(new Runnable(){
            @Override
            public void run() {
                int response = getInstance().logout();
               if(response==LOGOUT_OK){
                   new Handler(Looper.getMainLooper()).post(new Runnable() {
                       public void run() {
                           Toast.makeText(context,"Wylogowano", Toast.LENGTH_SHORT).show();
                            if(TIPApplication.isVisible()){
                                Intent intent = new Intent (context, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(intent);
                            }
                       }
                   });
               }else if(response==OFFLINE){
                   new Handler(Looper.getMainLooper()).post(new Runnable() {
                       public void run() {
                           Toast.makeText(context,"Brak dostępu do internetu sesja na serwerze nie została zakonczona", Toast.LENGTH_SHORT).show();

                       }
                   });
               }else{
                   new Handler(Looper.getMainLooper()).post(new Runnable() {
                       public void run() {
                           Toast.makeText(context,"Problem z wylogowaniem się", Toast.LENGTH_SHORT).show();
                       }
                   });
               }

            }
        }, "LogoutThread");
    }



}
