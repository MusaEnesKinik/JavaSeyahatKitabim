package com.eneskinik.javaseyahatkitabim.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.eneskinik.javaseyahatkitabim.model.Yer;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;

@Dao
public interface YerDao { //burada methodları yazacağız

    @Query("SELECT * FROM Yer")
    Flowable<List<Yer>> herSeyiAl(); //bize bir liste döndüreceği için Yer tablosundan bütün verileri listelemesi istendi

    @Insert
    Completable insert(Yer yer); //veri eklemeyi bu yapacak,

    @Delete
    Completable delete(Yer yer); //veri silmeyi bu yapacak,

}
