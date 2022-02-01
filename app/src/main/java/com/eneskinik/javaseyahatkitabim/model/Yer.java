package com.eneskinik.javaseyahatkitabim.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity //burada parantez içinde oluşturulacak olan SQLite tablo adını verebiliyoruz, eğer burada vermezsek isimi "public clas Yer" yazan yerden alacak ve tablo adı 'Yer' olacak
public class Yer implements Serializable {

    @PrimaryKey(autoGenerate = true) //autoGenerate = true otomatik olarak bizim için bütün id ler oluşturulacak
    public int id;

    @ColumnInfo(name = "Yer İsmi") //SQLite içinde "Yer İsmi" olarak kolon ismi kaydedilecek
    public String isim; //kaydedilen yerin ismi

    @ColumnInfo(name = "Enlem") //SQLite içinde "Enlem" olarak kolon ismi kaydedilecek
    public Double enlem;

    @ColumnInfo(name = "Boylam") //SQLite içinde "Boylam" olarak kolon ismi kaydedilecek
    public Double boylam;

    public Yer(String isim, Double enlem, Double boylam) { //id istemedik çünkü otomatik oluşturulacak
        this.isim = isim;
        this.enlem = enlem;
        this.boylam = boylam;

    }

}
