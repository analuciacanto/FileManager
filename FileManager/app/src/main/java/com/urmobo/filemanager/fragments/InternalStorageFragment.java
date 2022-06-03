package com.urmobo.filemanager.fragments;

import android.Manifest;

import android.app.AlertDialog;
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
import com.urmobo.filemanager.FileOpener;
import com.urmobo.filemanager.ModelFile;
import com.urmobo.filemanager.MultiFileAdapter;
import com.urmobo.filemanager.OnFileSelectedListener;
import com.urmobo.filemanager.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalStorageFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private MultiFileAdapter fileAdapter;
    private ImageView img_back;
    private TextView tv_pathHolder;
    private ArrayList<File> selectedFiles = new ArrayList<>();
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
        runtimePermission();
        setHasOptionsMenu(true);

        return view;
    }

    private void runtimePermission() {
        Dexter.withContext(getContext()).withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
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

    public void rename(File file) {
        ArrayList<ModelFile> selectedFiles = fileAdapter.getFilesSelected();
    }

    public void remove() {
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
                isRemoved = file.getFile().delete();
                //fileList.remove(file);
            }
            fileAdapter.notifyDataSetChanged();
            if (isRemoved)
                Toast.makeText(getContext(), "Removido", Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(getContext(), "Erro inesperado", Toast.LENGTH_SHORT).show();
            }
        });

        deleteDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel() );

        AlertDialog alertDialog_delete = deleteDialog.create();
        alertDialog_delete.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_all:
                selectAll();
                fileAdapter.notifyDataSetChanged();
                break;
            case R.id.rename:
                break;
            case R.id.copy:
                break;
            case R.id.remove:
                remove();
                break;



        }
        return true;


    }
}


