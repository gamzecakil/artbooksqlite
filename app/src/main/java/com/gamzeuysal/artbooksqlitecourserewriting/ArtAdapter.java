package com.gamzeuysal.artbooksqlitecourserewriting;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gamzeuysal.artbooksqlitecourserewriting.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.MyViewHolder> {

    ArrayList<Art> list;

    //Constructor
    public ArtAdapter(ArrayList<Art> list){
        this.list = list;
    }
    @NonNull
    @Override
    //layout bağlama
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MyViewHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.recyclerRowTextView.setText(list.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recyler row hangisine tıklarsa onun bilgilerini Art activity de gösterecek.
                Intent intent = new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","recylerRowItemSeleceted");
                intent.putExtra("artId",list.get(position).getId());//id sine göre elemanları okuyup dolduracağım.
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        private  RecyclerRowBinding binding;
        public MyViewHolder(RecyclerRowBinding binding) { // parametre olarak ilk gelen View itemView silip recyclerRowBinding ile yapalım.
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
