package com.example.journeyjournal;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.inputmethodservice.ExtractEditText;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbb20.CountryCodePicker;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private Button registration_btn;
    private TextInputLayout first_name_inputL,last_name_inputL,phone_number_inputL,email_inputL,password_inputL,password_confirm_inputL;
    private TextInputEditText first_name_input,last_name_input,phone_number_input,email_input,password_input,password_confirm_input;
    Dialog dialog;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private FirebaseUser userID;
    private ExtractEditText MY_EDIT_TEXT ;
    private ScrollView scrollView;
    private CountryCodePicker codePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registration_btn=(Button)findViewById(R.id.registration_btn);
        first_name_input=findViewById(R.id.first_name_register);
        last_name_input=findViewById(R.id.last_name_register);
        phone_number_input=findViewById(R.id.phone_number_register);
        email_input=findViewById(R.id.email_register);
        password_input=findViewById(R.id.password_register);
        password_confirm_input=findViewById(R.id.password_confirm_register);
        first_name_inputL=findViewById(R.id.first_name_registerL);
        last_name_inputL=findViewById(R.id.last_name_registerl);
        phone_number_inputL=findViewById(R.id.phone_number_registerL);
        email_inputL=findViewById(R.id.email_registerL);
        password_inputL=findViewById(R.id.password_registerL);
        password_confirm_inputL=findViewById(R.id.password_confirm_registerL);
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        scrollView=findViewById(R.id.scrollable);
        codePicker=findViewById(R.id.ccp);

        //dialog
        registration_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });
        KeyboardVisibilityEvent.setEventListener(Register.this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {

                if (isOpen ) {

                    //scroll to last view
                    View lastChild = scrollView.getChildAt(scrollView.getChildCount() - 1);
                    int bottom = lastChild.getBottom() + scrollView.getPaddingBottom();
                    int sy = scrollView.getScrollY();
                    int sh = scrollView.getHeight();
                    int delta = bottom - (sy + sh);

                    scrollView.smoothScrollBy(0, delta);
                }
            }
        });

    }

    private void createAccount(){
        dialog= new Dialog(Register.this);
        dialog.setContentView(R.layout.custom_dialog_loading);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dialog));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        final String first_name=first_name_input.getText().toString().trim();
        final String last_name=last_name_input.getText().toString().trim();
        final String email=email_input.getText().toString().trim();
        final String phone_number=phone_number_input.getText().toString().trim();
        final String password=password_input.getText().toString().trim();
        final String password_confirm=password_confirm_input.getText().toString().trim();
        if(TextUtils.isEmpty(first_name)){
            first_name_inputL.setError("Please write your First Name......");
        }else if(TextUtils.isEmpty(last_name)){
            defaultDrawable();
            last_name_inputL.setError("Please write your Last Name......");
        }
       else if(TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            defaultDrawable();
            email_inputL.setError("Please write your Email......");

        }
        else if(TextUtils.isEmpty(phone_number)){
            defaultDrawable();
            phone_number_inputL.setError("Please write your Phone Number......");

        }else if(TextUtils.isEmpty(password) || password.length()<=5){
            defaultDrawable();
            password_inputL.setError("Please write your Password......");

        }else if(TextUtils.isEmpty(password_confirm)){
            defaultDrawable();
            password_confirm_inputL.setError("Please write your Confirm Password......");

        }else if(!(password_confirm.matches(password))){
            defaultDrawable();
            password_confirm_inputL.setError("Password and Conform Password does not match......");
        }else{
            dialog.show();;
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        FirebaseUser user=auth.getCurrentUser();
                        Toast.makeText(Register.this,"Account Created",Toast.LENGTH_SHORT).show();

                        //Store information in FireStore

                        userID=auth.getCurrentUser();
                        String email=user.getEmail();
                        String uid=userID.getUid();
                        Log.w(TAG,"test document");



                        DocumentReference documentReference=firestore.collection("Users_Information").document(uid);
                        Map<String,Object> user_info=new HashMap<>();
                        user_info.put("First Name",first_name);
                        user_info.put("Last Name",last_name);
                        user_info.put("email",email);
                        user_info.put("Phone Number",codePicker.getSelectedCountryCode()+phone_number);


                        documentReference.set(user_info).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                dialog.dismiss();
                                startActivity(new Intent(Register.this,Home.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Log.w(TAG,"Error adding document" +e);
                            }
                        });


                    }
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String error_msg= String.valueOf(e);
                            if(error_msg.contains("The email address is already in use by another account")){
                                dialog.dismiss();
                                System.out.println("Error register"+e);
                                Toast.makeText(Register.this,"Email Exists",Toast.LENGTH_SHORT).show();
                            }else {
                                dialog.dismiss();
                                System.out.println("Error register"+e);
                                Toast.makeText(Register.this,"Invalid Email",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }
            });

        }
    }

    private void defaultDrawable() {
        last_name_inputL.setError(null);
        first_name_inputL.setError(null);
        email_inputL.setError(null);
        phone_number_inputL.setError(null);
        password_inputL.setError(null);
        password_confirm_inputL.setError(null);
    }


}