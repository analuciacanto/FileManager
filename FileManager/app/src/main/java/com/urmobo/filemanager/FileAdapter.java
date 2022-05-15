package com.urmobo.filemanager;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private Context context;
    private List<File> file;
    private OnFileSelectedListener listener;

    public FileAdapter(Context context, List<File> file, OnFileSelectedListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.tvName.setText(file.get(position).getName());
        holder.tvName.setSelected(true);
        int items = 0;
        if (file.get(position).isDirectory()){
            File[] files = file.get(position).listFiles();
            for (File singleFile : files){
                if (!singleFile.isHidden()){
                    items +=1;
                }
            }
            holder.tvSize.setText(String.valueOf(items) + "Files");
        }
        else {
            holder.tvSize.setText(Formatter.formatShortFileSize(context, file.get(position).length()));

            if (file.get(position).getName().toLowerCase().endsWith(".jpeg") || file.get(position).getName().toLowerCase().endsWith(".jpg") || file.get(position).getName().toLowerCase().endsWith(".png") ){
                holder.imgFile.setImageResource(R.drawable.ic_image);
            }
            if (file.get(position).getName().toLowerCase().endsWith(".pdf")){
                holder.imgFile.setImageResource(R.drawable.ic_pdf);
            }
            if (file.get(position).getName().toLowerCase().endsWith(".mp4") ||  file.get(position).getName().toLowerCase().endsWith(".avi")){
                holder.imgFile.setImageResource(R.drawable.ic_video);
            }
            if (file.get(position).getName().toLowerCase().endsWith(".mp3") || file.get(position).getName().toLowerCase().endsWith(".wav")) {
                holder.imgFile.setImageResource(R.drawable.ic_baseline_audiotrack_24);
            }
            else {
                holder.imgFile.setImageResource(R.drawable.ic_file);
            }
        }
        holder.container.setOnClickListener(view -> listener.onFileClicked(file.get(position)));

        holder.container.setOnLongClickListener(view -> {
            listener.onFileLongClicked(file.get(position));
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
