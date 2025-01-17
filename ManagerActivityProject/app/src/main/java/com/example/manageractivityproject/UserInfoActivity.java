package com.example.manageractivityproject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.manageractivityproject.Constants.Constant;
import com.example.manageractivityproject.Convert.ConvertBitMap;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoActivity extends AppCompatActivity {

    private TextInputLayout layoutFullName;
    private TextInputEditText txtFullName;
    private TextView txtSelectPhoto;
    private Button btnContinue;
    private CircleImageView circleImageView;
    private static final int GALLERY_ADD_PROFILE = 1;
    private Bitmap bitmap = null;
    private SharedPreferences userPref;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        init();
    }

    private void init() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        layoutFullName = findViewById(R.id.txtLayoutNameUserInfo);
        txtFullName = findViewById(R.id.txtNameUserInfo);
        txtSelectPhoto = findViewById(R.id.txtSelectPhoto);
        btnContinue = findViewById(R.id.btnContinue);
        circleImageView = findViewById(R.id.imgUserInfo);

        //pick photo from gallery
        txtSelectPhoto.setOnClickListener(v->{
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i,GALLERY_ADD_PROFILE);
        });


        btnContinue.setOnClickListener(v->{
            // validate fields
            if(validate()){
                saveUserInfo();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==GALLERY_ADD_PROFILE && resultCode==RESULT_OK){

            Uri imgUri = data.getData();
            circleImageView.setImageURI(imgUri);

            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    private boolean validate(){
        if (txtFullName.getText().toString().isEmpty()){
            layoutFullName.setErrorEnabled(true);
            layoutFullName.setError("Full name is Required");
            return false;
        }

        return true;
    }


    private void saveUserInfo(){
        dialog.setMessage("Saving");
        dialog.show();
        String fullname = txtFullName.getText().toString().trim();

        StringRequest request = new StringRequest(Request.Method.POST, Constant.SAVE_USER_INFO, response->{
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("photo",object.getString("photo"));
                    editor.putString("fullname",object.getString("fullname"));
                    editor.apply();
                    startActivity(new Intent(UserInfoActivity.this,HomeActivity.class));
                    Toast.makeText(UserInfoActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                Toast.makeText(UserInfoActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            dialog.dismiss();

        },error ->{
            error.printStackTrace();
            dialog.dismiss();
        } ){
            //add params
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("username", userPref.getString("username", ""));
                map.put("fullname",fullname);
                map.put("photo", ConvertBitMap.getConvertBitMap().bitmapToString(bitmap));
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(UserInfoActivity.this);
        queue.add(request);
    }








}