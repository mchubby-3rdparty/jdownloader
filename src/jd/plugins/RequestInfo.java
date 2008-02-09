package jd.plugins;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Diese Klasse bildet alle Informationen ab, die bei einem Request herausgefunden werden können
 *
 * @author astaldo
 */
public class RequestInfo {
    /**
     * Der Quelltext der Seite
     */
    private String htmlCode = null;
    /**
     * Die (Soll)Adresse der Seite
     */
    private String location = null;
    /**
     * Die zurückgelieferten Header
     */
    private  Map<String,List<String>> headers  = null;
    /**
     * Der zurückgelieferte Code
     */
    private int responseCode;
    private HttpURLConnection connection;
    /**
     * Cookie
     */
    private String cookie   = null;

    public RequestInfo(String htmlCode, String location, String cookie, Map<String,List<String>> headers, int responseCode){
        this.htmlCode = htmlCode;
        this.location = location;
        this.cookie   = cookie;
        this.headers  = headers;
        this.responseCode = responseCode;
        
    }
    /**
     * Gibt anhand des Rückgabecodes zurück, ob der Aufrufr erfolgreich war oder nicht.
     * HTTP Codes zwischen -2 und 499 gelten als erfolgreich
     * Negative Codes beudeuten dass der Server ( wie es z.B. machne Router HTTP Server machen) keinen responseCode zurückgegeben hat). In diesem Fall wird trotzdem true zurückgegeben
     * 
     * 
     * @return Wahr, wenn der HTTP Code zwischen -2 und 499 lag
     */
    public boolean isOK(){
        if(responseCode>-2 && responseCode<500)
            return true;
        else
            return false;
    }
    public Map<String,List<String>> getHeaders() { return headers;      }
    public String getHtmlCode()                  { return htmlCode;     }
    public String getLocation()                  { return location;     }
    public String getCookie()                    { return cookie;       }
    public int getResponseCode()                 { return responseCode; }
    /**
     * @return the connection
     */
    public HttpURLConnection getConnection() {
        return connection;
    }

    public boolean containsHTML(String pattern){
        return getHtmlCode().indexOf(pattern)>=0;
    }
    public void setConnection(HttpURLConnection connection) {
        this.connection = connection;
    }
    public String toString()	{ return getHtmlCode(); }
}
