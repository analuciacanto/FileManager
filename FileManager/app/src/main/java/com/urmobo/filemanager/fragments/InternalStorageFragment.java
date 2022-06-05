package com.urmobo.filemanager.fragments;
import android.Manifest;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.urmobo.filemanager.BuildConfig;
import com.urmobo.filemanager.FileOpener;
import com.urmobo.filemanager.ModelFile;
import com.urmobo.filemanager.MultiFileAdapter;
import com.urmobo.filemanager.OnFileSelectedListener;
import com.urmobo.filemanager.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


public class InternalStorageFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private MultiFileAdapter fileAdapter;
    private ImageView img_back;
    private TextView tv_pathHolder;
    private ArrayList<File> selectedFiles = new ArrayList<>();

    ActivityResultLauncher<String[]> activityResultLauncher;
    private boolean WRITE_PERMISSION = false;
    private boolean READ_PERMISSION = false;

    List<ModelFile> fileList;
    File storage;
    String data;
    Boolean isLoading = false;
    View view;
    String rootPath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);

        view = inflater.inflate(R.layout.fragment_internal_storage, container, false);

        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);


        if (!Environment.isExternalStorageManager()){
            if (requestAccessPemission()){
                displayFiles();
            }
        }

        runtimePermission();

        rootPath = String.valueOf(Environment.getExternalStorageDirectory());
        storage = new File(rootPath);

        try {
            data = getArguments().getString("path");
            File file = new File(data);
            storage = file;

        } catch (Exception e) {
            e.printStackTrace();
        }

        tv_pathHolder.setText(storage.getAbsolutePath());

        setHasOptionsMenu(true);
        return view;
    }

    private boolean requestAccessPemission() {
            try{
                Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                startActivity(
                        new Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                uri
                        )
                );
                return true;
            } catch (Error e){
                System.err.println(e);
                return false;
            }
    }

    private void runtimePermission() {

        if (Environment.isExternalStorageManager()) {
            Dexter.withContext(getContext()).withPermissions(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(new MultiplePermissionsListener() {

                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displayFiles();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    permissionToken.continuePermissionRequest();
                }
            }).check();
        }
    }

    public ArrayList<ModelFile> findFiles(File file) {
        ArrayList<ModelFile> arrayList = new ArrayList<>();
        File[] files = file.listFiles();

        if (files != null) {
            for (File singleFile : files) {
                ModelFile modelFile = new ModelFile(singleFile);
                arrayList.add(modelFile);
            }

        }
        return arrayList;
    }

    private void displayFiles() {

        recyclerView = view.findViewById(R.id.recycler_internal);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        fileList = new ArrayList<>();
        isLoading = true;
        ArrayList<ModelFile> files = findFiles(storage);

        if (files.size() > 0) {
            fileList.addAll(files);
        } else {
            // TODO
            //MESSAGE OF NONE FILES.
        }
        isLoading = false;

        if (!isLoading) {
            fileAdapter = new MultiFileAdapter(getContext(), fileList, this);
            recyclerView.setAdapter(fileAdapter);

        }
    }

    @Override                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    public void onFileClicked(@NonNull ModelFile file) {
        if (file.getFile().isDirectory()) {
            InternalStorageFragment internalStorageFragment = new InternalStorageFragment();
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getFile().getAbsolutePath());
            internalStorageFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, internalStorageFragment).addToBackStack(null).commit();

        } else {
            try {
                FileOpener.openFile(getContext(), file);
            } catch (IOException e) {
                e.printStackTrace();
            }   
        }
    }

    @Override
    public void onFileLongClicked(ModelFile file) {
        file.setChecked(!file.isChecked());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
       /* int filesSelected = fileAdapter.getFilesSelected().size();

        if (filesSelected > 1){
            menu.removeItem(R.id.rename);
        }
        else if (filesSelected == 0) {
            menu.removeItem(R.id.rename);
            menu.removeItem(R.id.remove);
            menu.removeItem(R.id.move);
            menu.removeItem(R.id.copy);
        } */
    }

    public void selectAll(){
       for (ModelFile modelFile: fileList ){
           modelFile.setChecked(true);
       }
        fileAdapter.notifyDataSetChanged();
    }

    public void rename() {
        ArrayList<ModelFile> selectedFiles = fileAdapter.getFilesSelected();
        if (selectedFiles.size() == 1 ) {
            File file = selectedFiles.get(0).getFile();
            AlertDialog.Builder renameDialog = new AlertDialog.Builder(getContext());
            renameDialog.setTitle("Renomear");
            final EditText name = new EditText(getContext());
            renameDialog.setView(name);

            renameDialog.setPositiveButton("Ok", (dialog, which) -> {
                String new_name = name.getEditableText().toString();
                String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                File current = new File(file.getAbsolutePath());
                File destination = new File(file.getAbsolutePath().replace(file.getName(), new_name + extention));

                if (current.renameTo(destination)){


                    Toast.makeText(getContext(), "Renamed!!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getContext(), "Couldn't rename!", Toast.LENGTH_SHORT).show();
                }
            });

            renameDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog_rename = renameDialog.create();
            alertDialog_rename.show();

        }
    }

    public void remove()  {

        ArrayList<ModelFile> selectedFiles = fileAdapter.getFilesSelected();
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());

        if (selectedFiles.size() == 1) {
            deleteDialog.setTitle("Deseja remover " + selectedFiles.get(0).getFile().getName() + "?");
        }
        else {
            deleteDialog.setTitle("Deseja remover "+ selectedFiles.size() + " arquivos ?");
        }


        deleteDialog.setPositiveButton("Sim", (dialog, which) -> {
            boolean isRemoved = false;
            for (ModelFile file: selectedFiles) {
                try {
                    file.getFile().setReadable(true);
                    FileOutputStream fileOutputStream = new FileOutputStream(file.getFile().getAbsolutePath() );
                    fileOutputStream.close();

                    File f = new File(file.getFile().getAbsolutePath());
                    isRemoved = f.delete();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getContext(), "Removido: " + isRemoved + " itens", Toast.LENGTH_SHORT).show();
                fileAdapter.notifyDataSetChanged();
            }
        });
        deleteDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog_delete = deleteDialog.create();
        alertDialog_delete.show();
    }

    void copyFiles(){
        ClipboardManager clipboard = (ClipboardManager)
                getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        Intent appIntent = new Intent();
        ClipData clip = ClipData.newIntent("Colaaaaaaar", appIntent);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_all:
                selectAll();
                fileAdapter.notifyDataSetChanged();
                break;
            case R.id.rename:
                rename();
                break;
            case R.id.copy:
                copyFiles();
                break;
            case R.id.remove:
                    remove();


        }
        return true;


    }
}


