package com.eneskinik.javaseyahatkitabim.view;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.eneskinik.javaseyahatkitabim.R;
import com.eneskinik.javaseyahatkitabim.model.Yer;
import com.eneskinik.javaseyahatkitabim.roomdb.YerDao;
import com.eneskinik.javaseyahatkitabim.roomdb.YerDatabase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.eneskinik.javaseyahatkitabim.databinding.ActivityMapsBinding;
import com.google.android.material.snackbar.Snackbar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    ActivityResultLauncher<String> izinBaslaticisi;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean bilgi;
    YerDatabase db;
    YerDao yerDao;
    Double secilenEnlem;
    Double secilenBoylam;
    private CompositeDisposable kullanAt = new CompositeDisposable();
    Yer secilenYer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        baslaticiyiKaydet();

        sharedPreferences = this.getSharedPreferences("com.eneskinik.javaseyahatkitabim",MODE_PRIVATE); //bilgi kaydediyoruz bunun i??erisine. (Bir daha ??al????t??r??ld?? m?? ??al????t??r??lmad?? m??)
        bilgi = false;

        db = Room.databaseBuilder(getApplicationContext(),YerDatabase.class,"Yerler").build(); //database olu??turduk
        yerDao = db.yerDao(); //MapsActivity alt??nda yerDao methoduna ula??abiliriz art??k

        secilenEnlem = 0.0;
        secilenBoylam = 0.0;

        binding.kaydetButonu.setEnabled(false); //kullan??c?? bie yer se??meden kaydet butonu etkin hale gelmez

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) { //Harita haz??r oldu??unda ne yap??laca???? burda yaz??lm????
        mMap = googleMap; //ilk ba??ta olu??turulan googleMap objesine e??itlenmi??
        mMap.setOnMapLongClickListener(this);//olu??turdu??umuz "GoogleMap.OnMapLongClickListener" bu methodu g??ncel haritada kullanaca????m??z?? belirttik

        Intent intent = getIntent();//MapsActivity e gelinen intenti ald??k
        String info = intent.getStringExtra("info");//intent i??indeki infoyu ????kard??k

        if (info.equals("yeni")) { // bu info yeni ise yeni bir ??ey konulmak isteniyor

            binding.kaydetButonu.setVisibility(View.VISIBLE); //kaydet butonu g??sterilsin, INVISIBLE ekrandan kald??rm??yor sadece t??klanmaz yap??yor
            binding.silButonu.setVisibility(View.GONE); //sil butonu tamamen ekranda g??r??nmeyecek, GONE nin INVISIBLE den fark?? ekrandan kald??r??yor

            //alt sat??r yaz??ld??????nda olu??an hatay?? ortadan kald??rmak i??in parantez i??inde (LocationManager) i belirtiyoruz
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); //genel konum y??neticisi, b??t??n i??lem bunun ??zerinden d??ner
            //"this.getSystemService(Context.LOCATION_SERVICE);" buradan d??nen objeyi 'LocationManager' olarak kaydet demek

            //konumun de??i??ti??ine dair uyar??lar?? alabilmek i??in 'LocationListener' kullan??yoruz
            locationListener = new LocationListener() { //konum dinleyici, LocationManager in bize verdi??i mesajlar?? al??p yap??lacak olan i??lemleri burada yazmam??z?? sa??lar
                @Override
                public void onLocationChanged(@NonNull Location location) { //de??i??en konumu bize veriyor
                    //System.out.println("Lokasyon: " + location.toString()); //logcat te konumu yazd??r??yor

                    //SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.eneskinik.javaseyahatkitabim",MODE_PRIVATE);//bilgi kaydediyoruz bunun i??erisine. (Bir daha ??al????t??r??ld?? m?? ??al????t??r??lmad?? m??)
                    /** boolean **/ bilgi = sharedPreferences.getBoolean("bilgi",false);//sharedPreferences da bilgi diye bir ??ey var dedik, yoksa de??eri false olsun dedik

                    if (!bilgi) {

                        LatLng kullaniciLokasyon = new LatLng(location.getLatitude(),location.getLongitude()); //enlem ve boylam?? kullan??c?? lokasyonuna e??itledik
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kullaniciLokasyon,15)); //kullan??c??n??n yapt?????? her de??i??iklikte kamera oraya zoom yapacak
                        sharedPreferences.edit().putBoolean("bilgi",true).apply(); //bir defa ??al????t??ktan sonra true olacak ve onLocationChanged ne kadar ??a????r??lsa da ??nceki iki sat??r ??al????mayacak

                    }

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) { //rasyoneli g??stermeyi kontrol ediyor
                    Snackbar.make(binding.getRoot(),"Haritalar ????in ??zin ??steniyor",Snackbar.LENGTH_INDEFINITE).setAction("??zin Ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission (izin istememiz gerekiyor)
                            izinBaslaticisi.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                    }).show();
                } else {
                    //request permission (izin istememiz gerekiyor)
                    izinBaslaticisi.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }

            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                Location sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (sonKonum != null) { //bir konum geliyorsa kameray?? ona d??nd??rebiliriz, konum de??i??irse onMapReady ??a????r??lacak ve kamera oraya d??necek
                    LatLng sonKullaniciKonumu = new LatLng(sonKonum.getLatitude(),sonKonum.getLongitude()); //son konum enlem boylam??n?? sonKullaniciKonumu na e??itledik
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumu,15)); //kameray?? sonKullaniciKonumu na zoomlad??k
                }

                mMap.setMyLocationEnabled(true); // benim konumumun etkin oldu??undan emin ol

            }

        } else { // eski bir veri g??sterilmek isteniyor

            mMap.clear(); //??ncesinde yaz??lan bir ??ey varsa temizleniyor

            secilenYer = (Yer) intent.getSerializableExtra("yer"); //bize yollanan yer

            LatLng latLng = new LatLng(secilenYer.enlem,secilenYer.boylam);

            mMap.addMarker(new MarkerOptions().position(latLng).title(secilenYer.isim));//se??ilen yeri haritada g??stermek i??in bunu yazd??k, position alabilmek i??in de bir ??st sat??rda LatLng ald??k
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); //kameray?? odaklad??k

            binding.mekanAdiText.setText(secilenYer.isim); //mekan ismi yazan yeri se??ilen yerle de??i??tirdik
            binding.kaydetButonu.setVisibility(View.GONE); //kaydet butonunu i??levsellikten ????kard??k
            binding.silButonu.setVisibility(View.VISIBLE); //sil butonu g??r??necek

        }

        //latitude (enlem), longitude (boylam) => ikisinin bir arada kullan??m?? " LatLng "
        /**
        //48.8559713,2.2930037

        LatLng eifel = new LatLng(48.8559713,2.2930037); // eifell in enlemini ve boylam??n?? girdik
        mMap.addMarker(new MarkerOptions().position(eifel).title("Eyfel Kulesi")); //eifel e k??rm??z?? yer i??areti koyduk
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eifel,15f)); //eifel e 15float zoom vererek kameran??n eifel e odaklanmas??n?? sa??lad??k
        **/
    }

    private void baslaticiyiKaydet() {
        izinBaslaticisi = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) { //izin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //izin ald??????m??zdan emin olmak i??in bunu yap??yoruz e??ittir kulland??k
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                        Location sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (sonKonum != null) { //bir konum geliyorsa kameray?? ona d??nd??rebiliriz, konum de??i??irse onMapReady ??a????r??lacak ve kamera oraya d??necek
                            LatLng sonKullaniciKonumu = new LatLng(sonKonum.getLatitude(),sonKonum.getLongitude()); //son konum enlem boylam??n?? sonKullaniciKonumu na e??itledik
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumu,15)); //kameray?? sonKullaniciKonumu na zoomlad??k
                        }
                    }

                } else { //izin verilmedi,kullan??c?? izni reddetti??inde
                    Toast.makeText(MapsActivity.this,"??zin Gerekli!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) { //haritada uzun bas??l?? tuttu??umuzda bu method ??a????r??l??yor, nereye t??kland??ysa oran??n enlem boylam??n?? veriyor

        mMap.clear(); //harita ??zerinde ??nceden konulmu?? k??rm??z?? i??aretleri temizler
        mMap.addMarker(new MarkerOptions().title("Kullan??c??n??n Se??imi").position(latLng));//harita ??zerinde kullan??c??n??n se??ti??i yere k??rm??z?? i??aret koyar

        secilenEnlem = latLng.latitude;
        secilenBoylam = latLng.longitude;

        binding.kaydetButonu.setEnabled(true);//kullan??c?? kaydedece??i yeri se??ti??inde kaydet butonu aktif hale gelir

    }

    public void kaydet(View view){ //kaydet butonunun methodu

        Yer yer = new Yer(binding.mekanAdiText.getText().toString(),secilenEnlem,secilenBoylam);
        //yerDao.insert(yer);
        kullanAt.add(yerDao.insert(yer) //yerDao.insert yapt??k
                .subscribeOn(Schedulers.io()) // yerDao.insert i io da yap??p
                .observeOn(AndroidSchedulers.mainThread()) // yerDao.insert ?? mainThread de g??zlemleyecek
                .subscribe(MapsActivity.this::handleResponse) //handleResponse ??al????t??rabilmek i??in parantez i??indekini yazd??k
        );

    }

    private void handleResponse() { //gelen cevab?? ele al

        Intent intent = new Intent(MapsActivity.this,MapsActivity.class); //MapsActivity.this den MapsActivity.class a gidecek
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //gitmeden ??nce b??t??n aktiviteleri kapatacak
        startActivity(intent);

    }

    public void sil(View view){ //sil butonunun methodu

        if (secilenYer != null) { // se??ilen yer bo?? de??ilse

            kullanAt.add(yerDao.delete(secilenYer)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        kullanAt.clear(); // daha ??nce yap??lan b??t??n coollar burdan ????pe at??l??yor, haf??zada yer tutmuyor
    }
}