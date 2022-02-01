package com.eneskinik.javaseyahatkitabim.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.eneskinik.javaseyahatkitabim.R;
import com.eneskinik.javaseyahatkitabim.adapter.YerAdapter;
import com.eneskinik.javaseyahatkitabim.databinding.ActivityMainBinding;
import com.eneskinik.javaseyahatkitabim.model.Yer;
import com.eneskinik.javaseyahatkitabim.roomdb.YerDao;
import com.eneskinik.javaseyahatkitabim.roomdb.YerDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    YerDatabase db;
    YerDao yerDao;
    private Schedulers Shedulars;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        db = Room.databaseBuilder(getApplicationContext(),YerDatabase.class,"Yerler").build(); //database oluşturduk
        yerDao = db.yerDao(); //MainActivity altında yerDao methoduna ulaşabiliriz artık

        compositeDisposable.add(yerDao.herSeyiAl() // bunlar arka planda yapılacak
            .subscribeOn(Shedulars.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(MainActivity.this::handleResponse)
        );

    }

    private void handleResponse(List<Yer> yerList) { //yerList bu method içinde verilecek
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); // compositeDisposable.add içindekileri alt alta verecek
        YerAdapter yerAdapter = new YerAdapter(yerList); //YerAdapter oluşturduk, oluştururken istediği listeye yerList verdil
        binding.recyclerView.setAdapter(yerAdapter); //oluşturduğum yerAdapter i recyclerView a verdim, veriler görünecek

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
            intent.putExtra("info","yeni");//yeni bir yer oluşturmaya çalıştığını info ile belirtiyoruz
            startActivity(intent); // intent i yolladık
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}