package com.example.journeyjournal;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {
    private static final int RC_SIGN_IN =100;
    private TextView create_account,forgot_password;
    private Button login_btn;
    private TextInputEditText email_input,password_input;
    private TextInputLayout email_inputL,password_inputL;
    private FirebaseAuth auth;
    private ImageButton google_login,face_book_login,twitter_login;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseFirestore firestore;
    CallbackManager mCallbackManager;
    PackageInfo info;
    private OAuthProvider.Builder provider;



    Dialog dialog;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.NoActionBarAndStatusBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        create_account=findViewById(R.id.Create_Account);
        forgot_password=findViewById(R.id.Forgot_Password);
        login_btn=findViewById(R.id.login_btn);
        email_input=findViewById(R.id.email_login);
        password_input=findViewById(R.id.password_login);
        email_inputL=findViewById(R.id.email_loginL);
        password_inputL=findViewById(R.id.password_loginL);
        google_login=findViewById(R.id.google_login);
        auth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        face_book_login=findViewById(R.id.face_book_login);
        twitter_login=findViewById(R.id.twitter_login);

        provider = OAuthProvider.newBuilder("twitter.com");
        provider.addCustomParameter("lang", "Eng");

        FacebookSdk.sdkInitialize(Login.this);
        mCallbackManager = CallbackManager.Factory.create();


        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();

        twitter_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                twitter_login();
            }
        });

        if(auth.getCurrentUser() !=null){
            startActivity(new Intent(getApplicationContext(), Home.class));
            finish();
        }

        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setForgot_password();
            }
        });

        create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(Login.this,Register.class));
            }
        });

        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading();
               signIn();
            }
        });

        FacebookSdk.sdkInitialize(Login.this);
        face_book_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logInWithReadPermissions(Login.this,
                        Arrays.asList("email","public_profile"));
                LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(Login.this,"Cancel Login",Toast.LENGTH_SHORT).show();
                        System.out.println("this Cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(Login.this,"Error Login",Toast.LENGTH_SHORT).show();
                        System.out.println("this Error"+ error);
                    }
                });
            }
        });


        LoadingDialog loadingDialog=new LoadingDialog(Login.this);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginAccount();
            }
        });

    }

    private void loading(){
        dialog= new Dialog(Login.this);
        dialog.setContentView(R.layout.custom_dialog_loading);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dialog));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.show();
    }


    private void twitter_login(){

        Task<AuthResult> pendingResultTask = auth.getPendingAuthResult();
        if (pendingResultTask != null) {
            // There's something already here! Finish the sign-in for your user.
            pendingResultTask
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user=authResult.getUser();
                                    assert user != null;
                                    String display_name= user.getDisplayName();
                                    upload_UserInfo(user,display_name,null);

                                    System.out.println("This User: "+ user != null);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Error 1 :"+e);
                                    Toast.makeText(Login.this,"Error Login",Toast.LENGTH_LONG).show();
                                }
                            });
        } else {
            auth
                    .startActivityForSignInWithProvider(/* activity= */ this, provider.build())
                    .addOnSuccessListener(
                            new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user=authResult.getUser();
                                    assert user != null;
                                    String display_name= user.getDisplayName();
                                    upload_UserInfo(user,display_name,null);

                                    System.out.println("This User: "+ user != null);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println("Error 2 :"+e);
                                    Toast.makeText(Login.this,"Error Login",Toast.LENGTH_LONG).show();
                                }
                            });
        }
    }

    private void getTwitter_info(){
        FirebaseUser firebaseUser = auth.getCurrentUser();

        firebaseUser
                .startActivityForLinkWithProvider(/* activity= */ this, provider.build())
                .addOnSuccessListener(
                        new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                String display_name=firebaseUser.getDisplayName();

                                upload_UserInfo(firebaseUser,display_name,null);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure.
                            }
                        });

    }



    private void setForgot_password(){
        dialog= new Dialog(Login.this);
        dialog.setContentView(R.layout.dialog_reset_password);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background_dalog_white));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();
        EditText email=dialog.findViewById(R.id.email_reset);
        Button reset=dialog.findViewById(R.id.reset_btn);





        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email_reset=email.getText().toString().trim();
                if (TextUtils.isEmpty(email_reset)) {
                    email.setError("Please write your Email ...");

                }else{
                    System.out.println("this is the email"+email_reset);
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.sendPasswordResetEmail(email_reset)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "reset email sent", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("Error here "+e);
                        }
                    });
                }
            }
        });



    }

    private void loginAccount(){

       loading();

        String email=email_input.getText().toString().trim();
        String password=password_input.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            email_inputL.setError("Please write your Email  ......");
            dialog.dismiss();

        } else if (TextUtils.isEmpty(password)) {
            password_inputL.setError("Please write your password ...");
            defaultDrawable();
            dialog.dismiss();
        }else {

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                  if(task.isSuccessful()){
                      final FirebaseUser user= auth.getCurrentUser();
                      dialog.dismiss();
                      Toast.makeText(Login.this, "Login..", Toast.LENGTH_SHORT).show();
                      startActivity(new Intent(getApplicationContext(),Home.class));
                  }else{
                      dialog.dismiss();
                      Toast.makeText(Login.this, "Error..!"+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                  }
                }
            });
        }

    }


    //Change UI according to user data.
    public void updateUI(FirebaseUser account){

        if(account != null){
            Toast.makeText(this,"You Signed In successfully",Toast.LENGTH_LONG).show();
            startActivity(new Intent(this,Home.class));
            finish();

        }
    }
    private void defaultDrawable() {
        email_inputL.setError(null);
        password_inputL.setError(null);
    }

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        updateUI(currentUser);
    }

    private void firebaseAuthWithGoogle(String idToken,String firstName,String lastName) {



        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(Login.this, "Boy Boy Good",
                                    Toast.LENGTH_SHORT).show();

                            // Sign in success, update UI with the signed-in user's information
                            Log.d((String) TAG, "signInWithCredential:success"+task.getException());
                            FirebaseUser user = auth.getCurrentUser();
                            upload_UserInfo(user,firstName,lastName);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w((String) TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        dialog.dismiss();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    private void upload_UserInfo(FirebaseUser user,String firstName, String lastName){
        String phone_number=user.getPhoneNumber();
        String email=user.getEmail();
        String uid=user.getUid();
        String userImage= String.valueOf(user.getPhotoUrl());
        DocumentReference documentReference=firestore.collection("Users_Information").document(uid);
        Map<String,Object> user_info=new HashMap<>();
        user_info.put("First Name",firstName);
        user_info.put("Last Name",lastName);
        user_info.put("email",email);
        user_info.put("Phone Number",phone_number);
        user_info.put("User Image",userImage);
        documentReference.set(user_info).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dialog.dismiss();
                Toast.makeText(Login.this,"You Signed In successfully",Toast.LENGTH_LONG).show();
                startActivity(new Intent(Login.this,Home.class));
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

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            String firstName=user.getDisplayName();
                            String lastname="";
                            upload_UserInfo(user,firstName,lastname);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }


                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loading();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Toast.makeText(Login.this, "boy.",
                        Toast.LENGTH_SHORT).show();
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken(),account.getGivenName(),account.getFamilyName());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);

            }
        }else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }



    }



}