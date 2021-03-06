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
import org.epics.waveform.index.util.entity.WaveformFileAttribute;
import org.epics.waveform.index.util.entity.WaveformFilePVProperty;
import org.epics.waveform.index.util.entity.WaveformIndex;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.phoebus.hdf.display.HDFDisplayApp.logger;
public class HDFFileProcessor {

    // HDF group names
    private static String PV_NAME = "/PV_Names";
    private static String PV_TIMESTAMP = "/PV_TimeStamp";
    private static String WFDATA = "/WFdata";

    // Regular expression for cell
    private static final Pattern cellPattern = Pattern.compile("^.*?:(.*?)\\-.*");
    // Regular expression for device name
    private static final Pattern devicePattern = Pattern.compile(".*\\{(.*)\\}.*");

    // waveform index service pv property field names
    private static final String plot = "plot";
    private static final WaveformFileAttribute plotAttribute = new WaveformFileAttribute("plot", "true");

    // pv plot/unplot info from the waveform index service
    private static Map<String, Boolean> pvMap = Collections.emptyMap();

    static TreeItem processFile(File file) throws Exception {
        return processFile(file, null, Optional.empty());
    }

    static TreeItem processFile (File file, WaveformIndex waveformIndex) throws Exception {
        return processFile(file, waveformIndex, Optional.empty());
    }

    static TreeItem processFile(File file, WaveformIndex waveformIndex, Optional<String> filter) throws Exception {

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

                            TreeItem<HDFDisplayTreeNode> groupItem = new TreeItem(new HDFDisplayTreeNode(false, groupName, null, null, false));

                            try {
                                // read the pv names
                                H5ScalarDS pvNames = new H5ScalarDS(h5file, groupName, data.getPath());
                                String[] names = (String[]) pvNames.getData();

                                // read the time stamps for each pv
                                H5ScalarDS pvTimestamps = new H5ScalarDS(h5file, groupName, PV_TIMESTAMP);
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

                                    String pvName = names[i];

                                    if (pvMap.getOrDefault(pvName, false)) {
                                        groupItem.setExpanded(true);
                                        expandRoot = true;
                                    }

                                    // Filter the pv names based on the users specified filter
                                    if( filter.isEmpty() || pvName.matches(wildcardToRegex(filter.get()))) {
                                        // Add cell node
                                        Matcher cellMatcher = cellPattern.matcher(pvName);
                                        if (cellMatcher.matches()) {
                                            final String cell = cellMatcher.group(1);
                                            if (cell != null && !cell.isBlank()) {
                                                Optional<TreeItem<HDFDisplayTreeNode>> foundCellItem = groupItem.getChildren().stream().filter(node -> {
                                                    return node.getValue().getName().equals(cell);
                                                }).findFirst();

                                                TreeItem<HDFDisplayTreeNode> cellItem;
                                                if (foundCellItem.isEmpty()) {
                                                    cellItem = new TreeItem<HDFDisplayTreeNode>
                                                            (new HDFDisplayTreeNode(false, cell, null, null, false));
                                                    groupItem.getChildren().add(cellItem);
                                                } else{
                                                    cellItem = foundCellItem.get();
                                                }

                                                // Add device
                                                Matcher deviceMatcher = devicePattern.matcher(pvName);
                                                if (deviceMatcher.matches()) {
                                                    final String device = deviceMatcher.group(1);
                                                    if(device != null && !device.isBlank()) {
                                                        Optional<TreeItem<HDFDisplayTreeNode>> foundDeviceItem = cellItem.getChildren().stream().filter(node -> {
                                                            return node.getValue().getName().equals(device);
                                                        }).findFirst();

                                                        TreeItem<HDFDisplayTreeNode> deviceItem;
                                                        if (foundDeviceItem.isEmpty()) {
                                                            deviceItem = new TreeItem<HDFDisplayTreeNode>
                                                                    (new HDFDisplayTreeNode(false, device, null, null, false));
                                                            cellItem.getChildren().add(deviceItem);
                                                        } else{
                                                            deviceItem = foundDeviceItem.get();
                                                        }
                                                        deviceItem.getChildren().add(new TreeItem<>(
                                                                new HDFDisplayTreeNode(true, pvName, timestamps[i], pvData, pvMap.getOrDefault(pvName, false))
                                                        ));
                                                    }

                                                }
                                            }
                                        } else {
                                            groupItem.getChildren().add(new TreeItem<>(
                                                    new HDFDisplayTreeNode(true, pvName, timestamps[i], pvData, pvMap.getOrDefault(pvName, false))
                                            ));
                                        }
                                    }

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

    /**
     * Convert the string from wildcard to regex
     * @param s - pattern string with wildcards
     * @return pattern string with wildcards converted to regex
     */
    private static String wildcardToRegex(String s) {
        return ".*" + s.replace("*", ".*").replace("?",".?") + ".*";
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
