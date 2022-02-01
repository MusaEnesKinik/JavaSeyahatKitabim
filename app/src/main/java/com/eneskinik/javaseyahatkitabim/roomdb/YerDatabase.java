package com.eneskinik.javaseyahatkitabim.roomdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.eneskinik.javaseyahatkitabim.model.Yer;

@Database(entities = {Yer.class},version = 1)
public abstract class YerDatabase extends RoomDatabase { //sınıfın soyut olmasını istediğimiz için "abstarct clas" tanımlıyoruz, ve bunun bir ROOm database den kalıtım alacağını (extends RoomDatabase) yazıyoruz

    public abstract YerDao yerDao();//abstract method oluşturduk, geriye (YerDao) yu döndürecek

}
