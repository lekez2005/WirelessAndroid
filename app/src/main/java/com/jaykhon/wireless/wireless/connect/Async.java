package com.jaykhon.wireless.wireless.connect;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

/**
 * Created by lekez2005 on 3/15/15.
 */
public class Async<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    Command<Result> command;
    ResultListener<Result> resultListener;
    Context context;

    public Async(Command<Result> command, ResultListener<Result> resultListener,
                 Context context) {
        super();
        this.command = command;
        this.resultListener = resultListener;
        this.context = context;

    }

    @Override
    protected Result doInBackground(Params... params) {
        Result result = null;
        try{
            result = command.execute();
        }catch (Exception e){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(e.getMessage())
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        if (resultListener != null) resultListener.onResultsSucceded(result);
    }

    @Override
    protected void onProgressUpdate(Progress... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Result result) {
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
}
