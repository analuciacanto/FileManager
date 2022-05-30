package com.urmobo.filemanager.fragments;

import android.Manifest;

import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;

import android.widget.ImageView;

import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.urmobo.filemanager.FileAdapter;
import com.urmobo.filemanager.FileOpener;
import com.urmobo.filemanager.OnFileSelectedListener;
import com.urmobo.filemanager.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalStorageFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<File> fileList;
    private ImageView img_back;
    private TextView tv_pathHolder;
    private ArrayList <File> selectedFiles = new ArrayList<>();

    File storage;
    String data;
    String[] items = {"Copy", "Rename", "Delete", "Move"};
    Boolean isLoading = false;
    View view;

    public void populate() {



        String rootPath = String.valueOf(Environment.getExternalStorageDirectory());
        fileList = new ArrayList<>();
        storage = new File(rootPath);

        isLoading = true;
        ArrayList<File> files = findFiles(storage);

        if (files.size() > 0){
            fileList.addAll(files);
        }
        else{
            // TODO
            //MESSAGE OF NONE FILES.
        }

        isLoading = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstance){
        view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);

        populate();

        try {
            data = getArguments().getString("path");
            File file = new File(data);
            storage = file;

        } catch (Exception e ){
            e.printStackTrace();
        }

        tv_pathHolder.setText(storage.getAbsolutePath());
        runtimePermission();

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport){
                displayFiles();
            }
            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken){
                permissionToken.continuePermissionRequest();
            }
        }).check();

    }
    public ArrayList<File> findFiles(File file) {
       ArrayList<File> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for ( File singleFile: files) {
                arrayList.add(singleFile);
            }

        }
        return arrayList;


    }

    private void displayFiles(){
        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        if (!isLoading) {
            fileAdapter = new FileAdapter(getContext(), fileList, selectedFiles, this, false);
            recyclerView.setAdapter(fileAdapter);
        }
    }


    @Override
    public void onFileClicked(@NonNull File file) {
        if (file.isDirectory()){
            InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getAbsolutePath());
            internalStorageFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, internalStorageFragment).addToBackStack(null).commit();

        }
        else{
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void selectAllFiles(){
         populate();

        if (fileList.size() > 0) {
            selectedFiles.addAll(fileList);
            fileAdapter.notifyAll();
        }
    }


    @Override
    public void onFileLongClicked(File file, int position) {
        if (selectedFiles.contains(file)){
            selectedFiles.remove(file);
        }
        else {
            selectedFiles.add(file);
        }
        fileAdapter.notifyItemChanged(position);

 /*
        final Dialog optionDialog = new Dialog( getContext());
        optionDialog.setContentView(R.layout.option_dialog);
        optionDialog.setTitle("Select Options");
        ListView options = (ListView) optionDialog.findViewById(R.id.List);

        CustomAdapter customAdapter = new CustomAdapter();
        options.setAdapter(customAdapter);
        optionDialog.show();

        options.setOnItemClickListener((parent, view, position1, id) -> {
            String selectedItem = parent.getItemAtPosition(position1).toString();

            switch (selectedItem){
                case "Rename":
                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
                    renameDialog.setTitle("Rename File");
                    final EditText name = new EditText(getContext());
                    renameDialog.setView(name);

                     renameDialog.setPositiveButton("Ok", (dialog, which) -> {
                         String new_name = name.getEditableText().toString();
                         String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                         File current = new File(file.getAbsolutePath());
                         File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name + extention));

                         if (current.renameTo(destination)){
                                 fileList.set(position1, destination);
                                 fileAdapter.notifyItemChanged(position1);
;
                                 Toast.makeText(getContext(), "Renamed!!", Toast.LENGTH_SHORT).show();

                         }
                         else{
                             Toast.makeText(getContext(), "Couldn't rename!", Toast.LENGTH_SHORT).show();
                         }
                     });
                     renameDialog.setNegativeButton("Cancel", (dialog, which) -> optionDialog.cancel());
                     AlertDialog alertDialog_rename = renameDialog.create();
                        alertDialog_rename.show();

                    break;

                case "Delete":
                    AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
                    deleteDialog.setTitle("Delete" + file.getName() + "?");
                    deleteDialog.setPositiveButton("Yes", (dialog, which) -> {
                        file.delete();
                        fileList.remove(position1);
                        fileAdapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    });
                    deleteDialog.setNegativeButton("No", (dialog, which) -> optionDialog.cancel());

                    AlertDialog alertDialog_delete = deleteDialog.create();
                    alertDialog_delete.show();
                    break;




            }

        });*/

    }




    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = getLayoutInflater().inflate(R.layout.option_dialog, null);
            TextView textOptions = myView.findViewById(R.id.textOption);
            ImageView imgOptions = myView.findViewById(R.id.imgOption);
            textOptions.setText(items[position]);
            if (items[position].equals("Copy")) {
                imgOptions.setImageResource(R.drawable.ic_copy);
            }
            else if (items[position].equals("Rename")) {
                imgOptions.setImageResource(R.drawable.ic_rename);
            }
            else if (items[position].equals("Delete")) {
                imgOptions.setImageResource(R.drawable.ic_delete);
            }
            else if (items[position].equals("Move")) {
                imgOptions.setImageResource(R.drawable.ic_move);
            }
            return myView;
        }
    }
}
