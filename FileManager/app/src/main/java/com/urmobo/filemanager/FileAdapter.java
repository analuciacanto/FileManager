package com.urmobo.filemanager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private Context context;
    private List<ModelFile> file;
    private OnFileSelectedListener listener;
    ArrayList<File> selected;
    private Boolean selectedFile;

    public FileAdapter(Context context, List<ModelFile> file, ArrayList<File> selected, OnFileSelectedListener listener, Boolean selectedFile) {
        this.context = context;
        this.file = file;
        this.selected = selected;
        this.listener = listener;
        this.selectedFile = selectedFile;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.tvName.setText(file.get(position).getName());

        System.out.println(selected);

        if (file.get(position).isDirectory()){
            holder.imgFile.setImageResource(R.drawable.ic_folder);
        }

        else if (file.get(position).getName().toLowerCase().endsWith(".jpeg") || file.get(position).getName().toLowerCase().endsWith(".jpg") || file.get(position).getName().toLowerCase().endsWith(".png") ){
            holder.imgFile.setImageResource(R.drawable.ic_image);
        }
        else if (file.get(position).getName().toLowerCase().endsWith(".pdf")){
            holder.imgFile.setImageResource(R.drawable.ic_pdf);
        }
        else if (file.get(position).getName().toLowerCase().endsWith(".mp4") ||  file.get(position).getName().toLowerCase().endsWith(".avi")){
            holder.imgFile.setImageResource(R.drawable.ic_video);
        }
        else if (file.get(position).getName().toLowerCase().endsWith(".mp3") || file.get(position).getName().toLowerCase().endsWith(".wav") || file.get(position).getName().toLowerCase().endsWith(".wm3u")) {
            holder.imgFile.setImageResource(R.drawable.ic_baseline_audiotrack_24);
        }
        else {
            holder.imgFile.setImageResource(R.drawable.ic_file);
        }

        for (File selectedFile2 : selected){
            if (selected.size() > 0 && selectedFile2.getAbsolutePath().equals(file.get(position).getAbsolutePath())){
                holder.itemView.setBackgroundColor(Color.LTGRAY);
            }
        }
        holder.container.setOnClickListener(view -> listener.onFileClicked(file.get(position)));

        holder.container.setOnLongClickListener(view -> {
            listener.onFileLongClicked(file.get(position), position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return file.size();
    }
}
