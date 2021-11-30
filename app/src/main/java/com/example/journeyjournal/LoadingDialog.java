package com.example.journeyjournal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.view.LayoutInflater;

class LoadingDialog {
    private Activity activity;
    private AlertDialog dialog2;
    Dialog dialog;

    LoadingDialog(Activity myactivity){
        activity=myactivity;
    }

    void startLoadDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        LayoutInflater inflater=activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog_loading,null));
        builder.setCancelable(true);

        dialog2=builder.create();
        dialog2.show();
    }

    void dismissDialog(){
        dialog2.dismiss();
    }


}
