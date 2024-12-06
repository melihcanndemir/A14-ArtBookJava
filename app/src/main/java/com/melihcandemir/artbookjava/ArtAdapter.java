package com.melihcandemir.artbookjava;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.melihcandemir.artbookjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {
    ArrayList<Art> artArrayList;
    ArtAdapterCallback callback;

    public ArtAdapter(ArrayList<Art> artArrayList, ArtAdapterCallback callback) {
        this.artArrayList = artArrayList;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ArtHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, @SuppressLint("RecyclerView") int position) {
        Art art = artArrayList.get(position);
        byte[] imageBytes = art.image;
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            holder.binding.recyclerImageView.setImageBitmap(bitmap);
        } else {
            holder.binding.recyclerImageView.setImageResource(R.drawable.selectimage);
        }

        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);
        holder.binding.recyclerViewTextView.setSelected(true);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
            intent.putExtra("info", "old");
            intent.putExtra("artId", artArrayList.get(position).id);
            holder.itemView.getContext().startActivity(intent);
        });

        holder.itemView.setOnLongClickListener(v -> {
            int artId = artArrayList.get(position).id;
            callback.onArtDelete(artId, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }

    public static class ArtHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding binding;

        public ArtHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface ArtAdapterCallback {
        void onArtDelete(int artId, int position);
    }
}
