package com.urmobo.filemanager.fragments;
import android.Manifest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
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
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.urmobo.filemanager.BuildConfig;
import com.urmobo.filemanager.FileOpener;
import com.urmobo.filemanager.MainActivity;
import com.urmobo.filemanager.ModelFile;
import com.urmobo.filemanager.MultiFileAdapter;
import com.urmobo.filemanager.OnFileSelectedListener;
import com.urmobo.filemanager.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class SDCardFragment extends Fragment implements OnFileSelectedListener {

    private RecyclerView recyclerView;
    private MultiFileAdapter fileAdapter;
    private ImageView img_back;
    private TextView tv_pathHolder;

    List<ModelFile> fileList;
    File storage;
    String data;
    Boolean isLoading = false;
    View view;
    String rootPath;
    private Menu menu;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstance) {
        super.onCreate(savedInstance);

        view = inflater.inflate(R.layout.fragment_sd_card, container, false);

        tv_pathHolder = view.findViewById(R.id.tv_pathHolder);
        img_back = view.findViewById(R.id.img_back);

        File[] externalCacheDirs = getContext().getExternalCacheDirs();
        for (File file: externalCacheDirs){
            if (Environment.isExternalStorageRemovable(file)){
                rootPath =  String.valueOf(file.getPath().split("/Android")[0]);
                break;
            }
        }

        if (rootPath != null){
            storage = new File(rootPath);

            setHasOptionsMenu(true);

            try {
                data = getArguments().getString("path");
                File file = new File(data);
                storage = file;

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()){
                    requestAccessPermissions();
                }
            }
            runtimePermission();
            tv_pathHolder.setText(storage.getAbsolutePath());

        }
        return view;
  }

    private void requestAccessPermissions() {
        Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                uri);
        ActivityResultLauncher<Intent> startActivityForResult = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            runtimePermission();
                        }
                    }
                }

        );
        startActivityForResult.launch(intent);
    }


    private void runtimePermission() {
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
            SDCardFragment sdCardFragment = new SDCardFragment();
            Bundle bundle = new Bundle();
            bundle.putString("path", file.getFile().getAbsolutePath());
            sdCardFragment.setArguments(bundle);
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, sdCardFragment).addToBackStack(null).commit();

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
        this.menu=menu;
        updateMenuItems(menu);
        super.onPrepareOptionsMenu(menu);
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
                File current = new File(file.getAbsolutePath());
                ModelFile destinationModelFile;
                if (!file.isDirectory()) {
                    String extention = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("."));
                    destinationModelFile = new ModelFile(new File(file.getAbsolutePath().replace(file.getName(), new_name + extention)));
                }
                else{
                    destinationModelFile = new ModelFile(new File(file.getAbsolutePath().replace(file.getName(), new_name)));
                }

                if (current.renameTo(destinationModelFile.getFile())){
                    fileList.set(fileList.indexOf(selectedFiles.get(0)), destinationModelFile);
                    fileAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Renomeado", Toast.LENGTH_SHORT).show();

                }
                else{
                    Toast.makeText(getContext(), "Não foi possível renomear", Toast.LENGTH_SHORT).show();
                }
            });

            renameDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            AlertDialog alertDialog_rename = renameDialog.create();
            alertDialog_rename.show();
        }
    }


    public boolean deleteFiles( ArrayList<ModelFile> selectedFiles){
        boolean removed = false;
        for (ModelFile file: selectedFiles) {
            removed = recursiveDelete(file.getFile());
            if (removed){
                fileList.remove(file);
            }
        }
        return removed;
    }

    public boolean recursiveDelete(File file) {

        if (file.isDirectory() && file.list().length > 0) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        return file.delete();
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
        deleteDialog.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        deleteDialog.setPositiveButton("Sim", (dialog, which) -> {

            boolean isRemoved = deleteFiles(selectedFiles);

            if (isRemoved){
                Toast.makeText(getContext(), "Removido " + selectedFiles.size() + " arquivo(s)", Toast.LENGTH_SHORT).show();
                fileAdapter.notifyDataSetChanged();
            }

            else{
                Toast.makeText(getContext(), "Não foi possível remover os arquivos", Toast.LENGTH_SHORT).show();
            }

        });

        AlertDialog alertDialog_delete = deleteDialog.create();
        alertDialog_delete.show();
    }


    public void copyFiles() {
        ArrayList<ModelFile> selectedFiles = fileAdapter.getFilesSelected();
        ((MainActivity)getActivity()).setFilesToPaste(selectedFiles);
        for (ModelFile file: selectedFiles){
            file.setChecked(false);
        }
        fileAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), selectedFiles.size() + " arquivos copiados", Toast.LENGTH_SHORT).show();
    }


    public void pasteFiles(ArrayList<ModelFile> files, File dst){

        for (ModelFile file:  files) {
            try {
                pasteFile(file.getFile(),  new File( dst + "/" + file.getFile().getName()));
                fileList.add(new ModelFile(new File(dst + "/" + file.getFile().getName())));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        fileAdapter.notifyDataSetChanged();

    }

    public boolean pasteFile(File src, File dst) throws IOException {

        if (src.isDirectory()) {
            if (!dst.exists()) {
                dst.mkdirs();
            }

            ArrayList<ModelFile> filesFromDirectory = findFiles(src);
            for (ModelFile file: filesFromDirectory){
                pasteFile(file.getFile(),  new File( dst + "/" + file.getFile().getName()));
            }

        }
        else {
            InputStream in = new FileInputStream(src);
            try {
                OutputStream out = new FileOutputStream(dst);
                try {
                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                } finally {
                    out.close();

                }
                return true;
            } finally {
                in.close();
            }
        }
        return false;
    }


    public void moveFiles() {
        ArrayList<ModelFile> selectedFiles = fileAdapter.getFilesSelected();
        ((MainActivity)getActivity()).setFilesToMove(selectedFiles);
        for (ModelFile file: selectedFiles){
            file.setChecked(false);
        }
        fileAdapter.notifyDataSetChanged();
        Toast.makeText(getContext(), selectedFiles.size() + " arquivos selecionados", Toast.LENGTH_SHORT).show();
    }


    public void moveFilesFor(ArrayList<ModelFile> files, File dst){

        for (ModelFile file:  files) {
            moveFile(file.getFile(), new File(dst + "/" + file.getFile().getName()));
            fileList.add(new ModelFile(new File(dst + "/" + file.getFile().getName())));
        }

        fileAdapter.notifyDataSetChanged();

    }

    public void moveFile(File src, File dst){
        try {

            if (pasteFile(src, dst)){
                src.delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        fileAdapter.notifyDataSetChanged();

    }
    private void updateMenuItems(Menu menu){
        int filesSelected = fileAdapter.getFilesSelected().size();

        if (filesSelected > 1){
            menu.findItem(R.id.rename).setVisible(false);
            menu.findItem(R.id.remove).setVisible(true);
            menu.findItem(R.id.copy).setVisible(true);
            menu.findItem(R.id.move).setVisible(true);

            if (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && ((MainActivity)getActivity()).getFilesToMove().size() >= 1){
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else if  (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && !(((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
            else if  (!(((MainActivity)getActivity()).getFilesToPaste().size() >= 1) && (((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else{
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
        }
        else if (filesSelected == 0) {
            menu.findItem(R.id.rename).setVisible(false);
            menu.findItem(R.id.remove).setVisible(false);
            menu.findItem(R.id.copy).setVisible(false);
            menu.findItem(R.id.move).setVisible(false);

            if (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && ((MainActivity)getActivity()).getFilesToMove().size() >= 1){
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else if  (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && !(((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
            else if  (!(((MainActivity)getActivity()).getFilesToPaste().size() >= 1) && (((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else{
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
        }
        else{
            menu.findItem(R.id.rename).setVisible(true);
            menu.findItem(R.id.remove).setVisible(true);
            menu.findItem(R.id.copy).setVisible(true);
            menu.findItem(R.id.move).setVisible(true);

            if (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && ((MainActivity)getActivity()).getFilesToMove().size() >= 1){
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else if  (((MainActivity)getActivity()).getFilesToPaste().size() >= 1 && !(((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(true);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
            else if  (!(((MainActivity)getActivity()).getFilesToPaste().size() >= 1) && (((MainActivity)getActivity()).getFilesToMove().size() >= 1)) {
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(true);
            }
            else{
                menu.findItem(R.id.paste).setVisible(false);
                menu.findItem(R.id.moveFor).setVisible(false);
            }
        }
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
            case R.id.remove:
                remove();
                break;
            case R.id.copy:
                copyFiles();
                break;
            case R.id.paste:
                pasteFiles(((MainActivity)getActivity()).getFilesToPaste(), new File(storage.getAbsolutePath()));
                break;
            case R.id.move:
                moveFiles();
                break;
            case R.id.moveFor:
                moveFilesFor(((MainActivity)getActivity()).getFilesToMove(), new File(storage.getAbsolutePath()));
        }
        updateMenuItems(menu);
        return true;


    }
}
