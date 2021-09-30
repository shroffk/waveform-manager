package org.phoebus.hdf.image.display;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;
import hdf.object.Dataset;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5File;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import javafx.scene.control.TreeItem;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.phoebus.hdf.image.display.HDFImageDisplayApp.logger;
public class HDFImageFileProcessor {

    // HDF group names
    private static final String REF = "/#refs#";
    private static final String DATA = "/data";
    static final String IMG_MONO = "img_mono";
    static final String IMG_RGB = "img_rgb";

    // HDF dataset names
    private static final String NANO = "nanoseconds";
    private static final String SECS = "secondsPastEpoch";

    private static final String ALARM = "alarmSeverity";
    private static final String MASK = "mask";
    private static final String VALUE = "value";


    static TreeItem processFile(H5File h5file) throws Exception {

        TreeItem treeRoot = new TreeItem(new HDFImageDisplayTreeNode(false, h5file.getName(), null, null, null));

        HObject theRoot = h5file.getRootObject();
        Group root = (Group) theRoot;

        root.breadthFirstMemberList().stream().forEachOrdered(node -> {
            // Search for the data part of the file
            if (node instanceof H5Group && ((H5Group) node).getFullName().equalsIgnoreCase(DATA)) {
                TreeItem dataRoot = new TreeItem(new HDFImageDisplayTreeNode(false, DATA, null, null, null));

                ((H5Group) node).breadthFirstMemberList().stream().forEachOrdered(dataNode -> {
                    if (dataNode instanceof H5Group) {
                        String groupName = ((H5Group) dataNode).getName();
                        logger.config("group:" + groupName);
                        switch (groupName) {
                            case IMG_MONO:
                                TreeItem imgMonoRoot = new TreeItem(new HDFImageDisplayTreeNode(false, IMG_MONO, null, null, null));

                                // look up the 10 image value datasets
                                ((H5Group) dataNode).breadthFirstMemberList().stream().forEachOrdered(img_mono -> {
                                    try {
                                        H5ScalarDS dset = (H5ScalarDS) img_mono;
                                        if (dset.getName().equalsIgnoreCase(VALUE)) {
                                            long dataset_id = -1;
                                            long dataspace_id = -1;
                                            int object_type = -1;
                                            long object_id = -1;
                                            long[] dims = {2};
                                            byte[][] dset_data;

                                            dataset_id = dset.open();
                                            dataspace_id = H5.H5Dget_space(dataset_id);
                                            H5.H5Sget_simple_extent_dims(dataspace_id, dims, null);

                                            // Allocate array of pointers to two-dimensional arrays (the
                                            // elements of the dataset.
                                            dset_data = new byte[(int) dims[0]][8];

                                            if (dataset_id >= 0) {
                                                H5.H5Dread(dataset_id,
                                                        HDF5Constants.H5T_STD_REF_OBJ,
                                                        HDF5Constants.H5S_ALL,
                                                        HDF5Constants.H5S_ALL,
                                                        HDF5Constants.H5P_DEFAULT,
                                                        dset_data);
                                            }

                                            for (int indx = 0; indx < dims[0]; indx++) {
                                                logger.config(VALUE + "[" + indx + "]:");
                                                logger.config("  ->");
                                                // Open the referenced object, get its name and type.
                                                if (dataset_id >= 0) {
                                                    object_id = H5.H5Rdereference(dataset_id, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5R_OBJECT, dset_data[indx]);
                                                    object_type = H5.H5Rget_obj_type(dataset_id, HDF5Constants.H5R_OBJECT, dset_data[indx]);
                                                }
                                                if (object_type >= 0) {
                                                    // Get the name.
                                                    Path path = Paths.get(H5.H5Iget_name(object_id));
                                                    String valueName = path.getFileName().toString();
                                                    String valuePath = FilenameUtils.separatorsToUnix(path.getParent().toString());
                                                    H5ScalarDS final_value = new H5ScalarDS(h5file, valueName, valuePath);
                                                    imgMonoRoot.getChildren().add(new TreeItem(new HDFImageDisplayTreeNode(true, "value["+indx+"]", null, final_value, IMG_MONO)));
                                                    H5.H5Dclose(object_id);
                                                }

                                            }
                                            dataRoot.getChildren().add(imgMonoRoot);
                                            H5.H5Sclose(dataspace_id);
                                            dset.close(dataset_id);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                break;
                            case IMG_RGB:
                                TreeItem imgRGBRoot = new TreeItem(new HDFImageDisplayTreeNode(false, IMG_RGB, null, null, null));

                                // look up the 10 image value datasets
                                ((H5Group) dataNode).breadthFirstMemberList().stream().forEachOrdered(img_rgb -> {
                                    try {
                                        H5ScalarDS dset = (H5ScalarDS) img_rgb;
                                        if (dset.getName().equalsIgnoreCase(VALUE)) {
                                            long dataset_id = -1;
                                            long dataspace_id = -1;
                                            int object_type = -1;
                                            long object_id = -1;
                                            long[] dims = {2};
                                            byte[][] dset_data;

                                            dataset_id = dset.open();
                                            dataspace_id = H5.H5Dget_space(dataset_id);
                                            H5.H5Sget_simple_extent_dims(dataspace_id, dims, null);

                                            // Allocate array of pointers to two-dimensional arrays (the
                                            // elements of the dataset.
                                            dset_data = new byte[(int) dims[0]][8];

                                            if (dataset_id >= 0) {
                                                H5.H5Dread(dataset_id,
                                                        HDF5Constants.H5T_STD_REF_OBJ,
                                                        HDF5Constants.H5S_ALL,
                                                        HDF5Constants.H5S_ALL,
                                                        HDF5Constants.H5P_DEFAULT,
                                                        dset_data);
                                            }

                                            for (int indx = 0; indx < dims[0]; indx++) {
                                                logger.config(VALUE + "[" + indx + "]:");
                                                logger.config("  ->");
                                                // Open the referenced object, get its name and type.
                                                if (dataset_id >= 0) {
                                                    logger.config("dereferencing a name for :" + dataset_id);
                                                    object_id = H5.H5Rdereference(dataset_id, HDF5Constants.H5P_DEFAULT, HDF5Constants.H5R_OBJECT, dset_data[indx]);
                                                    object_type = H5.H5Rget_obj_type(dataset_id, HDF5Constants.H5R_OBJECT, dset_data[indx]);
                                                }
                                                if (object_type >= 0) {
                                                    // Get the name.
                                                    logger.config("finding a name for :" + object_id);
                                                    Path path = Paths.get(H5.H5Iget_name(object_id));
                                                    String valueName = path.getFileName().toString();
                                                    String valuePath = FilenameUtils.separatorsToUnix(path.getParent().toString());
                                                    H5ScalarDS final_value = new H5ScalarDS(h5file, valueName, valuePath);
                                                    imgRGBRoot.getChildren().add(
                                                            new TreeItem(
                                                                    new HDFImageDisplayTreeNode(true, "value["+indx+"]", null, final_value, IMG_RGB)));
                                                    H5.H5Dclose(object_id);
                                                }

                                            }
                                            dataRoot.getChildren().add(imgRGBRoot);
                                            H5.H5Sclose(dataspace_id);
                                            dset.close(dataset_id);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                                break;
                            default:
                        }
                    } else if (dataNode instanceof Dataset) {
                        String name = ((Dataset) dataNode).getName();
                    }
                });
                treeRoot.getChildren().add(dataRoot);
            }
        });
        return treeRoot;
    }

    static class HDFImageDisplayTreeNode {
        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.nnnnnn");

        private final boolean isLeaf;
        private final String name;
        private final String timestamp;

        private final H5ScalarDS data;
        private final String dataType;

        /**
         * Create a new node for the hdf image file tree
         *
         * @param isLeaf
         * @param name
         * @param timestamp
         * @param data
         * @param dataType
         */
        private HDFImageDisplayTreeNode(boolean isLeaf, String name, String timestamp, H5ScalarDS data, String dataType) {
            this.isLeaf = isLeaf;
            this.name = name;
            this.timestamp = timestamp;
            this.data = data;
            this.dataType = dataType;
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

        public Instant getInstant() {
            return LocalDateTime.parse(this.timestamp, formatter).atZone(ZoneId.systemDefault()).toInstant();
        }

        public H5ScalarDS getData() {
            return data;
        }

        public String getDataType() {
            return dataType;
        }
    }

    // Values for the status of object type
    enum H5O_TYPE_obj {
        H5O_TYPE_UNKNOWN(HDF5Constants.H5O_TYPE_UNKNOWN), /* Unknown object type */
        H5O_TYPE_GROUP(HDF5Constants.H5O_TYPE_GROUP), /* Object is a group */
        H5O_TYPE_DATASET(HDF5Constants.H5O_TYPE_DATASET), /* Object is a dataset */
        H5O_TYPE_NAMED_DATATYPE(HDF5Constants.H5O_TYPE_NAMED_DATATYPE); /* Object is a named data type */
        private static final Map<Integer, H5O_TYPE_obj> lookup = new HashMap<Integer, H5O_TYPE_obj>();

        static {
            for (H5O_TYPE_obj s : EnumSet.allOf(H5O_TYPE_obj.class))
                lookup.put(s.getCode(), s);
        }

        private int code;

        H5O_TYPE_obj(int layout_type) {
            this.code = layout_type;
        }

        public int getCode() {
            return this.code;
        }

        public static H5O_TYPE_obj get(int code) {
            return lookup.get(code);
        }
    }

}
