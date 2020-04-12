package org.phoebus.hdf.display;

import hdf.object.Dataset;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5File;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.phoebus.hdf.display.HDFDisplayApp.logger;
public class HDFFileProcessor {

    private static String PV_NAME = "/PV_Names";
    private static String PV_TIMESTAMP = "/PV_TimeStamp";
    private static String WFDATA = "/WFdata";

    static TreeItem processFile(File file) throws Exception {

        TreeItem treeRoot = new TreeItem(new HDFDisplayTreeNode(false, file.getName(), null, null));

        //addLibraryPath(Paths.get(HDFDisplayPreferences.hdf_lib_path).toString());
        //Open file
        System.out.println("Loading file.........");
//        System.load(Paths.get(HDFDisplayPreferences.hdf_lib_path).toString());
        System.out.println(HDFFileProcessor.class.getClassLoader());

        final H5File h5file = new H5File(file.getAbsolutePath(), FileFormat.READ);
        long file_id = -1;
        file_id = h5file.open();

        HObject theRoot = h5file.getRootObject();
        Group root = (Group) theRoot;
        root.breadthFirstMemberList().stream().forEach( h -> {
            if (h instanceof H5Group) {

                if (h.getFullName().equalsIgnoreCase(PV_NAME)) {
                    ((H5Group) h).getMemberList().stream().forEach(m -> {
                        if (m instanceof Dataset) {
                            Dataset data = (Dataset) m;
                            String groupName = data.getName();
                            logger.info("Processing group: " + groupName);

                            TreeItem groupItem = new TreeItem(new HDFDisplayTreeNode(false, groupName, null, null));

                            try {
                                // read the pv names
                                H5ScalarDS pvNames = new H5ScalarDS(h5file, groupName, data.getPath());
                                String[] names = (String[]) pvNames.getData();

                                // read the time stamps for each pv
                                H5ScalarDS pvTimestamps = new H5ScalarDS(h5file, groupName,
                                        PV_TIMESTAMP);
                                String[] timestamps = (String[]) pvTimestamps.getData();

                                for (int i=0; i < names.length; i++) {
                                    // create a data object with the pv specific data
                                    H5ScalarDS pvData = new H5ScalarDS(h5file, groupName,
                                            WFDATA);
                                    long[] selectedDims = pvData.getSelectedDims();
                                    selectedDims[0] = i;
                                    selectedDims[1] = pvData.getDims()[1];
                                    groupItem.getChildren().add(new TreeItem<>(
                                            new HDFDisplayTreeNode(true, names[i], timestamps[i], pvData)
                                    ));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            treeRoot.getChildren().add(groupItem);
                        }
                    });
                }
            }
        });
        return treeRoot;
    }


    private static class HDFDisplayTreeNode {

        private final boolean isLeaf;
        private final String name;
        private final String timestamp;

        private final H5ScalarDS data;

        private SimpleBooleanProperty plotted;

        private HDFDisplayTreeNode(boolean isLeaf, String name, String timestamp, H5ScalarDS data) {
            this.isLeaf = isLeaf;
            this.name = name;
            this.timestamp = timestamp;
            this.data = data;
        }

        public SimpleBooleanProperty isPlotted() {
            return plotted;
        }

        public String getName() {
            return name;
        }

        public boolean isLeaf() {
            return isLeaf;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public H5ScalarDS getData() {
            return data;
        }
    }
}
