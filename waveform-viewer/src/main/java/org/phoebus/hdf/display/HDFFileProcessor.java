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
import org.phoebus.app.waveform.index.viewer.entity.WaveformFileAttribute;
import org.phoebus.app.waveform.index.viewer.entity.WaveformFilePVProperty;
import org.phoebus.app.waveform.index.viewer.entity.WaveformIndex;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.phoebus.hdf.display.HDFDisplayApp.logger;
public class HDFFileProcessor {

    // HDF group names
    private static String PV_NAME = "/PV_Names";
    private static String PV_TIMESTAMP = "/PV_TimeStamp";
    private static String WFDATA = "/WFdata";

    // waveform index service pv property field names
    private static final String plot = "plot";
    private static final WaveformFileAttribute plotAttribute = new WaveformFileAttribute("plot", "true");

    //
    private static Map<String, Boolean> pvMap = Collections.emptyMap();

    static TreeItem processFile(File file) throws Exception {
        return processFile(file, null);
    }

    static TreeItem processFile(File file, WaveformIndex waveformIndex) throws Exception {

        if (waveformIndex != null) {
            pvMap = waveformIndex.getPvProperties().stream().collect(
                    Collectors.toMap(
                            WaveformFilePVProperty::getPvName,
                            (waveformFilePVProperty) -> {
                                return waveformFilePVProperty.getAttributes().contains(plotAttribute);
                            }));
        }

        TreeItem treeRoot = new TreeItem(new HDFDisplayTreeNode(false, file.getName(), null, null, false));

        final H5File h5file = new H5File(file.getAbsolutePath(), FileFormat.READ);
        long file_id = -1;
        file_id = h5file.open();

        HObject theRoot = h5file.getRootObject();
        Group root = (Group) theRoot;
        root.breadthFirstMemberList().stream().forEach( h -> {
            if (h instanceof H5Group) {

                if (h.getFullName().equalsIgnoreCase(PV_NAME)) {
                    ((H5Group) h).getMemberList().stream().forEach(m -> {
                        boolean expandRoot = false;
                        if (m instanceof Dataset) {
                            Dataset data = (Dataset) m;
                            String groupName = data.getName();
                            logger.info("Processing group: " + groupName);

                            TreeItem groupItem = new TreeItem(new HDFDisplayTreeNode(false, groupName, null, null, false));

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
                                    H5ScalarDS pvData = new H5ScalarDS(h5file, groupName, WFDATA);
                                    long[] selectedDims = pvData.getSelectedDims();
                                    selectedDims[0] = 1;
                                    selectedDims[1] = pvData.getDims()[1];
                                    long[] startDims2 = pvData.getStartDims();
                                    startDims2[0] = i;
                                    startDims2[1] = 0;

                                    if (pvMap.getOrDefault(names[i], false)) {
                                        groupItem.setExpanded(true);
                                        expandRoot = true;
                                    }
                                    groupItem.getChildren().add(new TreeItem<>(
                                            new HDFDisplayTreeNode(true, names[i], timestamps[i], pvData, pvMap.getOrDefault(names[i], false))
                                    ));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            treeRoot.setExpanded(expandRoot);
                            treeRoot.getChildren().add(groupItem);
                        }
                    });
                }
            }
        });
        return treeRoot;
    }


    static class HDFDisplayTreeNode {
        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HH:mm:ss.nnnnnn");

        private final boolean isLeaf;
        private final String name;
        private final String timestamp;

        private final H5ScalarDS data;

        private final SimpleBooleanProperty plotted;

        private HDFDisplayTreeNode(boolean isLeaf, String name, String timestamp, H5ScalarDS data, boolean plotted) {
            this.isLeaf = isLeaf;
            this.name = name;
            this.timestamp = timestamp;
            this.data = data;
            this.plotted  = new SimpleBooleanProperty(plotted);
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

        public Instant getInstant() {
            return LocalDateTime.parse(this.timestamp, formatter).atZone(ZoneId.systemDefault()).toInstant();
        }

        public H5ScalarDS getData() {
            return data;
        }
    }
}
