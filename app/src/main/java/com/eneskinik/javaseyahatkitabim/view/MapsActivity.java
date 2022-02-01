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

        sharedPreferences = this.getSharedPreferences("com.eneskinik.javaseyahatkitabim",MODE_PRIVATE); //bilgi kaydediyoruz bunun içerisine. (Bir daha çalıştırıldı mı çalıştırılmadı mı)
        bilgi = false;

        db = Room.databaseBuilder(getApplicationContext(),YerDatabase.class,"Yerler").build(); //database oluşturduk
        yerDao = db.yerDao(); //MapsActivity altında yerDao methoduna ulaşabiliriz artık

        secilenEnlem = 0.0;
        secilenBoylam = 0.0;

        binding.kaydetButonu.setEnabled(false); //kullanıcı bie yer seçmeden kaydet butonu etkin hale gelmez

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
    public void onMapReady(GoogleMap googleMap) { //Harita hazır olduğunda ne yapılacağı burda yazılmış
        mMap = googleMap; //ilk başta oluşturulan googleMap objesine eşitlenmiş
        mMap.setOnMapLongClickListener(this);//oluşturduğumuz "GoogleMap.OnMapLongClickListener" bu methodu güncel haritada kullanacağımızı belirttik

        Intent intent = getIntent();//MapsActivity e gelinen intenti aldık
        String info = intent.getStringExtra("info");//intent içindeki infoyu çıkardık

        if (info.equals("yeni")) { // bu info yeni ise yeni bir şey konulmak isteniyor

            binding.kaydetButonu.setVisibility(View.VISIBLE); //kaydet butonu gösterilsin, INVISIBLE ekrandan kaldırmıyor sadece tıklanmaz yapıyor
            binding.silButonu.setVisibility(View.GONE); //sil butonu tamamen ekranda görünmeyecek, GONE nin INVISIBLE den farkı ekrandan kaldırıyor

            //alt satır yazıldığında oluşan hatayı ortadan kaldırmak için parantez içinde (LocationManager) i belirtiyoruz
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE); //genel konum yöneticisi, bütün işlem bunun üzerinden döner
            //"this.getSystemService(Context.LOCATION_SERVICE);" buradan dönen objeyi 'LocationManager' olarak kaydet demek

            //konumun değiştiğine dair uyarıları alabilmek için 'LocationListener' kullanıyoruz
            locationListener = new LocationListener() { //konum dinleyici, LocationManager in bize verdiği mesajları alıp yapılacak olan işlemleri burada yazmamızı sağlar
                @Override
                public void onLocationChanged(@NonNull Location location) { //değişen konumu bize veriyor
                    //System.out.println("Lokasyon: " + location.toString()); //logcat te konumu yazdırıyor

                    //SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.eneskinik.javaseyahatkitabim",MODE_PRIVATE);//bilgi kaydediyoruz bunun içerisine. (Bir daha çalıştırıldı mı çalıştırılmadı mı)
                    /** boolean **/ bilgi = sharedPreferences.getBoolean("bilgi",false);//sharedPreferences da bilgi diye bir şey var dedik, yoksa değeri false olsun dedik

                    if (!bilgi) {

                        LatLng kullaniciLokasyon = new LatLng(location.getLatitude(),location.getLongitude()); //enlem ve boylamı kullanıcı lokasyonuna eşitledik
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kullaniciLokasyon,15)); //kullanıcının yaptığı her değişiklikte kamera oraya zoom yapacak
                        sharedPreferences.edit().putBoolean("bilgi",true).apply(); //bir defa çalıştıktan sonra true olacak ve onLocationChanged ne kadar çağırılsa da önceki iki satır çalışmayacak

                    }

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) { //rasyoneli göstermeyi kontrol ediyor
                    Snackbar.make(binding.getRoot(),"Haritalar İçin İzin İsteniyor",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
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
                if (sonKonum != null) { //bir konum geliyorsa kamerayı ona döndürebiliriz, konum değişirse onMapReady çağırılacak ve kamera oraya dönecek
                    LatLng sonKullaniciKonumu = new LatLng(sonKonum.getLatitude(),sonKonum.getLongitude()); //son konum enlem boylamını sonKullaniciKonumu na eşitledik
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumu,15)); //kamerayı sonKullaniciKonumu na zoomladık
                }

                mMap.setMyLocationEnabled(true); // benim konumumun etkin olduğundan emin ol

            }

        } else { // eski bir veri gösterilmek isteniyor

            mMap.clear(); //öncesinde yazılan bir şey varsa temizleniyor

            secilenYer = (Yer) intent.getSerializableExtra("yer"); //bize yollanan yer

            LatLng latLng = new LatLng(secilenYer.enlem,secilenYer.boylam);

            mMap.addMarker(new MarkerOptions().position(latLng).title(secilenYer.isim));//seçilen yeri haritada göstermek için bunu yazdık, position alabilmek için de bir üst satırda LatLng aldık
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); //kamerayı odakladık

            binding.mekanAdiText.setText(secilenYer.isim); //mekan ismi yazan yeri seçilen yerle değiştirdik
            binding.kaydetButonu.setVisibility(View.GONE); //kaydet butonunu işlevsellikten çıkardık
            binding.silButonu.setVisibility(View.VISIBLE); //sil butonu görünecek

        }

        //latitude (enlem), longitude (boylam) => ikisinin bir arada kullanımı " LatLng "
        /**
        //48.8559713,2.2930037

        LatLng eifel = new LatLng(48.8559713,2.2930037); // eifell in enlemini ve boylamını girdik
        mMap.addMarker(new MarkerOptions().position(eifel).title("Eyfel Kulesi")); //eifel e kırmızı yer işareti koyduk
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eifel,15f)); //eifel e 15float zoom vererek kameranın eifel e odaklanmasını sağladık
        **/
    }

    private void baslaticiyiKaydet() {
        izinBaslaticisi = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) { //izin verildi
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //izin aldığımızdan emin olmak için bunu yapıyoruz eşittir kullandık
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                        Location sonKonum = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (sonKonum != null) { //bir konum geliyorsa kamerayı ona döndürebiliriz, konum değişirse onMapReady çağırılacak ve kamera oraya dönecek
                            LatLng sonKullaniciKonumu = new LatLng(sonKonum.getLatitude(),sonKonum.getLongitude()); //son konum enlem boylamını sonKullaniciKonumu na eşitledik
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonKullaniciKonumu,15)); //kamerayı sonKullaniciKonumu na zoomladık
                        }
                    }

                } else { //izin verilmedi,kullanıcı izni reddettiğinde
                    Toast.makeText(MapsActivity.this,"İzin Gerekli!",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) { //haritada uzun basılı tuttuğumuzda bu method çağırılıyor, nereye tıklandıysa oranın enlem boylamını veriyor

        mMap.clear(); //harita üzerinde önceden konulmuş kırmızı işaretleri temizler
        mMap.addMarker(new MarkerOptions().title("Kullanıcının Seçimi").position(latLng));//harita üzerinde kullanıcının seçtiği yere kırmızı işaret koyar

        secilenEnlem = latLng.latitude;
        secilenBoylam = latLng.longitude;

        binding.kaydetButonu.setEnabled(true);//kullanıcı kaydedeceği yeri seçtiğinde kaydet butonu aktif hale gelir

    }

    public void kaydet(View view){ //kaydet butonunun methodu

        Yer yer = new Yer(binding.mekanAdiText.getText().toString(),secilenEnlem,secilenBoylam);
        //yerDao.insert(yer);
        kullanAt.add(yerDao.insert(yer) //yerDao.insert yaptık
                .subscribeOn(Schedulers.io()) // yerDao.insert i io da yapıp
                .observeOn(AndroidSchedulers.mainThread()) // yerDao.insert ü mainThread de gözlemleyecek
                .subscribe(MapsActivity.this::handleResponse) //handleResponse çalıştırabilmek için parantez içindekini yazdık
        );

    }

    private void handleResponse() { //gelen cevabı ele al

        Intent intent = new Intent(MapsActivity.this,MapsActivity.class); //MapsActivity.this den MapsActivity.class a gidecek
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //gitmeden önce bütün aktiviteleri kapatacak
        startActivity(intent);

    }

    public void sil(View view){ //sil butonunun methodu

        if (secilenYer != null) { // seçilen yer boş değilse

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
        kullanAt.clear(); // daha önce yapılan bütün coollar burdan çöpe atılıyor, hafızada yer tutmuyor
    }
}