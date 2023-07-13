package com.gamzeuysal.artbooksqlitecourserewriting;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gamzeuysal.artbooksqlitecourserewriting.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {

    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent>  isImageSelectedLauncher;
    ActivityResultLauncher<String> isPermissionAllowedLauncher;
    Bitmap selectedImage;
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //daha onceden izin verilmiş mi verilmemiş mi burada hafıza tutup buradan okunuyor.
        //Buna göre izin soruyor ya da sormuyor
        registerLauncher();

        sqLiteDatabase = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

        Intent intent = getIntent();
        //Art Activity menu item tıklanmasına göre (save butonu cıkıp veri kaydetme yapacak.Ya da
        //recyler row tıklanmasına göre o elemanın değerlerini gösterecek.(save butonu yok)
        String info = intent.getStringExtra("info");
        System.out.println("info : "+info);
        if(info.equals("menuItemSelected")){
            //Yeni eleman eklenecek
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.imageView.setImageResource(R.drawable.selectimage);
            binding.buttonSave.setVisibility(View.VISIBLE);
        }else if(info.equals("recylerRowItemSeleceted")){
            //tıklanan elemanın verileri gösterilecek
            int artId = intent.getIntExtra("artId",0);
            binding.buttonSave.setVisibility(View.INVISIBLE);
            //seçilen elemanının id'sine göre veri tabanından verilerini okuyalım.
            try {
                Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts WHERE  id = ?",new String[]{String.valueOf(artId)});
                //Degere göre bulunacağı için selection arguments
                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");
                while(cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    //resim veritabanına byte[] array olarak kaydedilmişti.
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }



            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

    public void save(View view){
      String name = binding.nameText.getText().toString();
      String artistName = binding.artistText.getText().toString();
      String year = binding.yearText.getText().toString();

      //image küçültmek
        Bitmap smallImage = makeSmaller(selectedImage,300);

        //bitmap byte[] dizisine çevrilerek veritabanina kaydedilir.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        //database
        try{
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY,artname VARCHAR,paintername VARCHAR,year VARCHAR,image BLOB)");
            //Sql statement edittext alınana verileri kaydetme
            String sqlString = "INSERT INTO arts (artname,paintername,year,image) VALUES (?,?,?,?)";
            SQLiteStatement  sqLiteStatement = sqLiteDatabase.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();
        }catch (Exception e){
            System.out.println("Database create error : ");
            e.printStackTrace();
        }
        //kayıt olduktan sonra main activity'e geri dönelim
        Intent intentToMainActivity = new Intent(this,MainActivity.class);
        intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//içinde bulunduğum aktivite de dahil tüm aktiviteleri  kapat sadece gidecegim aktiviteyi aç.
        startActivity(intentToMainActivity);
    }
    private Bitmap makeSmaller(Bitmap image,int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio = (float) width / (float) height;
        if(bitmapRatio > 1){
            //landscape image
            width = maximumSize;
            height = (int)(width / bitmapRatio);
        }else{
            //portrait image
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }
      return image.createScaledBitmap(image,width,height,true);
    }
    public void selectImage(View view){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
          //Android 33+ ->READ_MEDIA_IMAGES
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                //request permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give Permission", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //request permission
                                    //permission launch et
                                    isPermissionAllowedLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                                }
                            }).show();
                }else{
                    //request permission
                    //permission launch et
                    isPermissionAllowedLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //resim seçilmiş mi launchla
                isImageSelectedLauncher.launch(intentToGallery);
            }

        }else{
            //Android 32- ->READ_EXTERNAL_STORAGE
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //request permission
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE)
                            .setAction("Give Permission", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    //request permission
                                    //permission launch et
                                    isPermissionAllowedLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                                }
                            }).show();
                }else{
                    //request permission
                    //permission launch et
                    isPermissionAllowedLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }else{
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //resim seçilmiş mi launchla
                isImageSelectedLauncher.launch(intentToGallery);
            }

        }

    }
private  void registerLauncher(){
isImageSelectedLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
    @Override
    public void onActivityResult(ActivityResult result) {
        if(result.getResultCode() == RESULT_OK){
            //kullanıcı galeriden image seçti
            Intent intentFromResult = result.getData();
            //geriye veri dönmüş mü bir bakalım.
            if(intentFromResult!= null){
                //geriye veri dönmüşse
                //Uri --> image tel deki yolu
                Uri imageData = intentFromResult.getData();
                //binding.imageView.setImageURI(imageData);
                try{
                    //uri 'yi bitmap e çevirme
                    if(Build.VERSION.SDK_INT >= 28){
                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                        selectedImage = ImageDecoder.decodeBitmap(source);
                        binding.imageView.setImageBitmap(selectedImage);
                    }else{
                         selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                         binding.imageView.setImageBitmap(selectedImage);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
});
isPermissionAllowedLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
    @Override
    public void onActivityResult(Boolean result) {
        if(result){
            //permission granted
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            //permission verildi resim seçilmiş mi launcher ile yakala
            isImageSelectedLauncher.launch(intentToGallery);
        }else{
            //permission denied
            Toast.makeText(ArtActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
        }
    }
});
}
}