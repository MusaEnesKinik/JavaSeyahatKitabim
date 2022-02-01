package com.eneskinik.javaseyahatkitabim;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater(); //Bağlamak için MenuInflater kullandık xml ile kodu birbirine bağlamada kullanılan kod
        menuInflater.inflate(R.menu.seyahat_menusu,menu); //seyahat_menusu nü menu ile bağlandık
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { //Menüden bir şey seçilirse ne olacağı burda yazılacak
        if (item.getItemId() == R.id.yer_ekle) { //yer_ekle ye mi tıklandığını kontrol ediyor
            Intent intent = new Intent(MainActivity.this,MapsActivity.class); // MainActivity den MapsActivity.class a gönderiyor
            startActivity(intent); // intent i yolladık
        }
        return super.onOptionsItemSelected(item);
    }
}