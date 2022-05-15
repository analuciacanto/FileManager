package com.urmobo.filemanager;

import java.io.File;

public interface OnFileSelectedListener {

    void onFileClicked(File file);
    void onFileLongClicked(File file);

}
