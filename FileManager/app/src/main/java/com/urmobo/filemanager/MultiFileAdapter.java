package com.urmobo.filemanager;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MultiFileAdapter extends RecyclerView.Adapter<MultiFileAdapter.FileAdapter> {

    private Context context;
    private List<ModelFile> file;
    private OnFileSelectedListener listener;


    public MultiFileAdapter(Context context, List<ModelFile> file, OnFileSelectedListener listener) {
        this.context = context;
        this.file = file;
        this.listener = listener;
    }

    public void setModelFile(ArrayList<ModelFile> file) {
        this.file = new ArrayList<>();
        this.file = file;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_container, parent,false);
        return new FileAdapter(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FileAdapter holder, int position) {
        holder.bind(file.get(position));
    }

    @Override
    public int getItemCount() {
        return file.size();
    }


    public class FileAdapter extends RecyclerView.ViewHolder{

        public TextView tvName;
        public CardView container;
        public ImageView imgFile;

        public FileAdapter(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tv_filename);
            container = itemView.findViewById(R.id.container);
            imgFile = itemView.findViewById(R.id.img_fileType);
        }

        void bind(final ModelFile file) {

            String grayColor = "#EBEBEB";
            int GRAY = Color.parseColor(grayColor);

            tvName.setText(file.getFile().getName());

            if (file.getFile().isDirectory()){
              imgFile.setImageResource(R.drawable.ic_folder);
            }
            else if (file.getFile().getName().toLowerCase().endsWith(".jpeg") || file.getFile().getName().toLowerCase().endsWith(".jpg") || file.getFile().getName().toLowerCase().endsWith(".png") ){
                imgFile.setImageResource(R.drawable.ic_image);
            }
            else if (file.getFile().getName().toLowerCase().endsWith(".pdf")){
                imgFile.setImageResource(R.drawable.ic_pdf);
            }
            else if (file.getFile().getName().toLowerCase().endsWith(".mp4") ||  file.getFile().getName().toLowerCase().endsWith(".avi")){
                imgFile.setImageResource(R.drawable.ic_video);
            }
            else if (file.getFile().getName().toLowerCase().endsWith(".mp3") || file.getFile().getName().toLowerCase().endsWith(".wav") || file.getFile().getName().toLowerCase().endsWith(".wm3u")) {
               imgFile.setImageResource(R.drawable.ic_baseline_audiotrack_24);
            }
            else {
                imgFile.setImageResource(R.drawable.ic_file);
            }

            if (file.isChecked()){
                itemView.setBackgroundColor(GRAY);
            }
            itemView.setBackgroundColor(file.isChecked() ? GRAY: Color.WHITE);


           itemView.setOnClickListener(itemView ->
                   listener.onFileClicked(file)
            );

            itemView.setOnLongClickListener(itemView -> {
                listener.onFileLongClicked(file);
                itemView.setBackgroundColor(file.isChecked() ? GRAY : Color.WHITE);
                return true;
            });
        }
    }

    public ArrayList<ModelFile> getFilesSelected() {
        ArrayList<ModelFile> selected = new ArrayList<>();
        for (int i = 0; i < file.size(); i++) {
            if (file.get(i).isChecked()) {
                selected.add(file.get(i));
            }
        }
        return selected;
    }
}




