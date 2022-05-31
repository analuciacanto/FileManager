package com.urmobo.filemanager;
import java.io.File;

public class ModelFile {
    private File file;
    private boolean isChecked;

    public ModelFile(File file){

        this.file = file;
        this.isChecked = false;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }



}
