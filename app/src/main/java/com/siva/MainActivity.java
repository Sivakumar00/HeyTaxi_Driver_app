package com.siva;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.siva.Common.Common;
import com.siva.Model.User;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {

    Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users,drivers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //init view
        btnRegister=(Button)findViewById(R.id.btnRegister);
        btnSignIn=(Button)findViewById(R.id.btnSignIn);
        rootLayout=(RelativeLayout)findViewById(R.id.rootLayout);
        ProgressDialog pDialog=new ProgressDialog(getApplicationContext());
        Paper.init(this);
        //firebase init
        auth=FirebaseAuth.getInstance();
        db=FirebaseDatabase.getInstance();
        users=FirebaseDatabase.getInstance().getReference().child("Users");
        drivers=FirebaseDatabase.getInstance().getReference().child(Common.user_driver_tbl);
        String user=Paper.book().read(Common.user_field);
        String pwd=Paper.book().read(Common.pwd_field);
        if(user!=null&& pwd!=null)
        {
            if(!TextUtils.isEmpty(user)&&!TextUtils.isEmpty(pwd)){
               autoLogin(user,pwd);
            }
        }
        //onclick
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });
    }

    private void autoLogin(String user, String pwd) {
        final SpotsDialog waitingDialog=new SpotsDialog(MainActivity.this);
        waitingDialog.show();
        auth.signInWithEmailAndPassword(user,pwd).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {

                startActivity(new Intent(MainActivity.this,Welcome.class));
                waitingDialog.dismiss();
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                btnSignIn.setEnabled(true);
            }
        });

    }

    private void showLoginDialog() {

        btnSignIn.setEnabled(false);
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN ");
        dialog.setMessage("Use Email to Sign In");
        LayoutInflater inflater=LayoutInflater.from(this);
        View login_layout=inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail= (MaterialEditText) login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword= (MaterialEditText) login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Email address", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
                    return;
                }


                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password is too Short", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //Login user
                final SpotsDialog waitingDialog=new SpotsDialog(MainActivity.this);
                waitingDialog.show();
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();
                       Paper.book().write(Common.user_field,edtEmail.getText().toString());
                        Paper.book().write(Common.pwd_field,edtPassword.getText().toString());
                     startActivity(new Intent(MainActivity.this,Welcome.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    }
                });

            }
            });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });



        dialog.show();

    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog=new AlertDialog.Builder(this);

        dialog.setTitle("REGISTER");
        dialog.setMessage("Use Email to Register");
        LayoutInflater inflater=LayoutInflater.from(this);
        View register_layout=inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail= (MaterialEditText) register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword= (MaterialEditText) register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName= (MaterialEditText) register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone= (MaterialEditText) register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Email address", Snackbar.LENGTH_SHORT).show();

                    return;
                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtName.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Phone Number", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if (edtPassword.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password is too Short", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                //register new user

                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        User user=new User();
                        user.setEmail(edtEmail.getText().toString());
                        user.setName(edtName.getText().toString());
                        user.setPhone(edtPhone.getText().toString());
                        user.setPassword(edtPassword.getText().toString());
                        drivers.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user);
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Snackbar.make(rootLayout, "Registered Successfully..!", Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Snackbar.make(rootLayout, "Failed"+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(rootLayout, "Failed"+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
