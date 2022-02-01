package com.eneskinik.javaseyahatkitabim.adapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eneskinik.javaseyahatkitabim.databinding.RecyclerRowBinding;
import com.eneskinik.javaseyahatkitabim.model.Yer;
import com.eneskinik.javaseyahatkitabim.view.MapsActivity;

import java.util.List;

public class YerAdapter extends RecyclerView.Adapter<YerAdapter.YerHolder> {

    List<Yer> yerList;

    public YerAdapter(List<Yer> yerList) {
        this.yerList = yerList;
    }

    @NonNull
    @Override
    public YerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new YerHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull YerHolder holder, @SuppressLint("RecyclerView") int position) { //bağlanınca ne olacak
        holder.recyclerRowBinding.recyclerViewTextView.setText(yerList.get(position).isim);//ilgili listedeki her şeyin ismi tek tek görünecek
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MapsActivity.class);
                //eski tıklanan yerden mi yoksa mainActivity(onCreateOptionsMenü) den mi geliyoruz bunu öğrenmek için info gönderiyoruz
                intent.putExtra("info","eski");//adapter içinde eski bir veri yollayacağımı söylüyorum
                intent.putExtra("yer",yerList.get(position));
                holder.itemView.getContext().startActivity(intent); //intenti başlatıyor
            }
        });
    }

    @Override
    public int getItemCount() {
        return yerList.size(); //kaç tane yer varsa o kadar recycler Row oluşturur
    }

    public class YerHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding recyclerRowBinding;
        public YerHolder(RecyclerRowBinding recyclerRowBinding) {
            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding = recyclerRowBinding;
        }
    }

}
