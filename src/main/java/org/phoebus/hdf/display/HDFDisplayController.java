package org.phoebus.hdf.display;

import hdf.object.h5.H5ScalarDS;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;

public class HDFDisplayController {

    // Plot


    @FXML
    TextField filter;

    @FXML
    TreeView treeView;

    // HDF file
    private File file;

    @FXML
    public void initialize() {
        this.file = new File("C:\\hdf5\\PS-20200131-11%3A43%3A08.391806.h5");
        constructTree();
    }

    public void setFile(File file) {
        this.file = file;
        constructTree();
    }

    public void constructTree() {
        try {
            TreeItem root = HDFFileProcessor.processFile(this.file);
            treeView.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
