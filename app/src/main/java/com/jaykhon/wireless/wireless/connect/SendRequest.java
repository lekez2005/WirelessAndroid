package com.jaykhon.wireless.wireless.connect;

import android.content.Context;
import android.util.Log;

import com.jaykhon.wireless.wireless.R;
import com.jaykhon.wireless.wireless.WirelessApp;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by lekez2005 on 3/15/15.
 */
public class SendRequest {

    public static Context mContext;
    public static final String SSL_VERSION = "SSLv3";
    public static final String HOSTNAME = "192.168.1.7";

    private static final String LOG = "SendRequest";

    public static JSONObject getJsonFromUrl(String url, Map<String, String> headers){
        HttpsURLConnection urlConnection = getConnection(url);
        addHeaders(urlConnection, headers);
        return jsonFromUrlConnection(urlConnection);
    }

    public static JSONObject postJsonToUrl(String url, JSONObject obj, Map<String, String> headers){
        HttpsURLConnection urlConnection = getConnection(url);
        addHeaders(urlConnection, headers);
        try {
            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type","application/json");

            try {
                urlConnection.connect();
                DataOutputStream out = new DataOutputStream(urlConnection.getOutputStream());
                out.write(obj.toString().getBytes());
                out.flush();
                if (urlConnection.getResponseCode() != 200){
                    Log.d(SendRequest.class.getCanonicalName(), urlConnection.getResponseMessage());
                }
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        return jsonFromUrlConnection(urlConnection);
    }

    public static HttpsURLConnection getConnection(String url) {
        try {
            URL ur = new URL(url);
            HttpsURLConnection urlConnection = (HttpsURLConnection) ur.openConnection();
            urlConnection.setSSLSocketFactory(HTTPSContext.getInstance().getSocketFactory());
            urlConnection.setHostnameVerifier(HTTPSContext.getVerifier());
            urlConnection.setConnectTimeout(5000);
            return urlConnection;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addHeaders(HttpsURLConnection urlConnection, Map<String, String> headers){
        if (headers != null){
            for (String key : headers.keySet()) {
                urlConnection.setRequestProperty(key, headers.get(key));
            }
        }
    }

    private static String getString(InputStream is) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                "UTF-8"), 8);
        StringBuilder builder = new StringBuilder();
        String line = null;

        while ((line = reader.readLine()) != null) {
            builder.append(line + "\n");
        }

        is.close();

        return builder.toString();
    }

    public static JSONObject jsonFromUrlConnection(HttpURLConnection urlConnection){
        JSONObject retVal = null;
        try {
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

            String result = getString(in);

            in.close();

            retVal = new JSONObject(result);

            String logStr = String.format("Response from %s", urlConnection.getURL());
            Log.d(logStr, retVal.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            urlConnection.disconnect();
        }
        return retVal;
    }


    public static JSONArray getJsonArray(String url, Map<String, String> headers) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (WirelessApp.networkConnected()){
            URL ur = new URL(url);
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection) ur.openConnection();
            urlConnection.setSSLSocketFactory(HTTPSContext.getInstance().getSocketFactory());
            urlConnection.setHostnameVerifier(HTTPSContext.getVerifier());

            if (headers != null){
                for (String key : headers.keySet()) {
                    urlConnection.setRequestProperty(key, headers.get(key));
                }
            }



            JSONArray retVal = null;
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                if (urlConnection.getResponseCode() != 200){
                    Log.d(LOG, urlConnection.getResponseMessage());
                }else{
                    String result = getString(in);

                    in.close();

                    retVal = new JSONArray(result);
                    WirelessApp.setLastConnectionSuccess(true);

                    Log.d(LOG, retVal.toString());
                }

            } catch (JSONException e) {
                WirelessApp.setLastConnectionSuccess(false);
                e.printStackTrace();
            } catch (ConnectException e){
                e.printStackTrace();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }
            return retVal;
        }else{
            WirelessApp.setLastConnectionSuccess(false);
            return null;
        }

    }






    public static class HTTPSContext {
        private static SSLContext context = null;
        private static HostnameVerifier verifier = null;

        protected HTTPSContext() {
        }

        public static SSLContext getInstance() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
            if (context == null) {

                // load the ssl configurations
                // Load CAs from an InputStream
                // (could be from a resource or ByteArrayInputStream or ...)
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                // From https://www.washington.edu/itconnect/security/ca/load-der.crt
                InputStream caInput = mContext.getResources().getAssets().open("server.crt");
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);
                    System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
                } finally {
                    caInput.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                context = SSLContext.getInstance(SSL_VERSION);
                context.init(null, tmf.getTrustManagers(), null);

            }

            return context;
        }

        public static HostnameVerifier getVerifier(){
            if (verifier == null){
                verifier = new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        HostnameVerifier hv =
                                HttpsURLConnection.getDefaultHostnameVerifier();
                        if (hv.verify(HOSTNAME, session)){
                            return true;
                        }else {
                            // TODO do actual hostname verification
                            return true;
                        }
                    }
                };
            }
            return verifier;
        }
    }

}

