package com.example.signup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.itextpdf.text.pdf.PdfReader;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.signup.MainActivity.userID;

public class pdfActivity extends AppCompatActivity {

    Button upload;
    TextView notification,noOfPages,cost;

    FirebaseStorage storage;         //used for uploading files .. Ex: pdf
    FirebaseDatabase database;       //used to store URLs of uploaded files..

    ProgressDialog progressDialog;

    Uri pdfUri;                      //uri are actually URLs that are meant for local storage
    String upiIdEt,name, note;
    int noPages;

    String approvalRefNo ="";

    final int UPI_PAYMENT=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        upload = findViewById(R.id.upload);
        notification = findViewById(R.id.notification);
        noOfPages=findViewById(R.id.noOfPages);
        cost=findViewById(R.id.cost);
         storage=FirebaseStorage.getInstance();             //Return an object of firebase storage
        database=FirebaseDatabase.getInstance();           //Return an object of firebase database

        upiIdEt="7979757341@ybl";
        name ="Subhashish Anand";
        note ="Paying for Print";

        if(ContextCompat.checkSelfPermission(pdfActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            selectPdf();
        }else{
            ActivityCompat.requestPermissions(pdfActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},9);
        }


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pdfUri!=null) {                  //The user has selected the file
                    payUsingUpi(noPages*2+"",upiIdEt,name, note);
                }else{
                    Toast.makeText(pdfActivity.this,"Please select a file",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void payUsingUpi(String amount,String upiIdEt,String name, String note){
        Uri uri=Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",upiIdEt)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am",amount)
                .appendQueryParameter("cu","INR")
                .build();

        Intent upiPayIntent =new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        //will always show a dialog to user to choose an app
        Intent chooser = Intent.createChooser(upiPayIntent,"Pay with");

        //check if intent resolves
        if(null!=chooser.resolveActivity(getPackageManager())){
            startActivityForResult(chooser, UPI_PAYMENT);
        }else{
            Toast.makeText(pdfActivity.this,"No UPI found",Toast.LENGTH_SHORT).show();
        }
    }

    public void uploadFile(final Uri pdfUri) {

        progressDialog=new ProgressDialog(pdfActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle("Uploading file...");
        progressDialog.setProgress(0);
        progressDialog.show();

        final String fileName=  System.currentTimeMillis()+".pdf";
        final String fileName1 = System.currentTimeMillis()+"";
        StorageReference storageReference = storage.getReference();       //returns root path
        storageReference.child(fileName).putFile(pdfUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                String type= ".pdf";  //type
                //Store the url in realtime database.
                DatabaseReference reference=database.getReference();        //return the path to root


                reference.child("orders").child("pending").child(fileName1).child("user").setValue(userID);
                reference.child("orders").child("pending").child(fileName1).child("type").setValue(type).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            Toast.makeText(pdfActivity.this,"File successfully uploaded",Toast.LENGTH_SHORT).show();
                            goMainActivity();
                        }else{
                            Toast.makeText(pdfActivity.this,"file not uploaded",Toast.LENGTH_SHORT).show();
                        }

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(pdfActivity.this,"file not uploaded",Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                //Track the progress of our upload
                int currentProgress= (int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                progressDialog.setProgress(currentProgress);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 9 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }else{
            Toast.makeText(pdfActivity.this,"Please provide permission",Toast.LENGTH_SHORT).show();
            goMainActivity();
        }
    }

    private void selectPdf() {
        //to offer user to select a file using file manager
        //we will be using an Intent
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction((Intent.ACTION_GET_CONTENT));   //to fetch files
        startActivityForResult(intent,86);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Whether user has selected a file or not(ex: pdf)

        switch (requestCode){
            case UPI_PAYMENT:
                if((RESULT_OK==resultCode)||(resultCode==11)){
                    if(data!= null){
                        String trxt = data.getStringExtra("response");
                        Log.d("msg","onActivityResult: "+trxt);
                        ArrayList<String> datalist = new ArrayList<>();
                        datalist.add(trxt);
                        upiPaymentDataOperation(datalist);
                    }else{
                        Log.d("UPI","onActivityResult: "+"Return data is null");
                        ArrayList<String> datalist = new ArrayList<>();
                        datalist.add("nothing");
                        upiPaymentDataOperation(datalist);
                    }
                }else{
                    Log.d("UPI","onActivityResult:+ "+"Return data is null"); //when user simply back without payment
                    ArrayList<String> datalist = new ArrayList<>();
                    datalist.add("nothing");
                    upiPaymentDataOperation(datalist);
                }
                break;
        }

        if (requestCode== 86 && resultCode==RESULT_OK && data!=null){
            pdfUri = data.getData();   //return the uri of selected file.
            notification.setText(data.getData().getLastPathSegment());
            try {
                PdfReader document = new PdfReader(pdfActivity.this.getContentResolver().openInputStream(pdfUri));
                noPages = document.getNumberOfPages();
                noOfPages.setText("No of pages: "+noPages);
                cost.setText("Cost: "+noPages*2);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(pdfActivity.this, "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.i("A Error",e.getMessage());
                pdfActivity.this.finish();
            }
        }else if(requestCode== 86 && data==null){
            goMainActivity();
        }

    }
    private void upiPaymentDataOperation(ArrayList<String>data){
        if (isConnectionAvailable(pdfActivity.this)){
            String str = data.get(0);
            Log.d("UPIPAY","upiPaymentDataOperation: "+str);
            String paymentcancel="";
            if(str == null) str = "discard";
            String status="";
            String response[]= str.split("&");
            for (int i =0; i<response.length;i++){
                String equalStr[]=response[i].split("=");
                if(equalStr.length>=2){
                    if(equalStr[0].toLowerCase().equals("Status".toLowerCase())){
                        status = equalStr[1].toLowerCase();
                    }else if(equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase())||equalStr[0].toLowerCase().equals("txnRef".toLowerCase())){
                        approvalRefNo=equalStr[1];
                    }
                }else{
                    paymentcancel = "Payment cancelled by user";
                }
            }
            if (status.equals("success")){
                //Code to handle successful transaction here.
                Toast.makeText(pdfActivity.this, "Transaction successful",Toast.LENGTH_SHORT).show();
                Log.d("UPI", "responseStr"+approvalRefNo);
                uploadFile(pdfUri);
            }else if("Payment cancelled by User.".equals(paymentcancel)){
                Toast.makeText(pdfActivity.this,"Payment cancelled by user.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(pdfActivity.this,"Transaction failed. Please Try again",Toast.LENGTH_SHORT).show();

            }
        }else{
            Toast.makeText(pdfActivity.this,"Internet connection is not available. Please check and try again",Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    public void goMainActivity(){
        Intent intent= new Intent(pdfActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
