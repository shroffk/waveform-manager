package org.phoebus.services.waveform.index.timestamp;

import hdf.object.Dataset;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5File;
import hdf.object.h5.H5Group;
import hdf.object.h5.H5ScalarDS;
import org.epics.waveform.index.util.entity.WaveformFileAttribute;
import org.epics.waveform.index.util.entity.WaveformFileProperty;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.epics.waveform.index.util.spi.ProcessWaveformIndex;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Process the time stamp for the index file
 */
public class ProcessTimestamp  implements ProcessWaveformIndex
{
    private static final Logger logger = Logger.getLogger(ProcessTimestamp.class.getName());

    private static final String time_pattern = "yyyyMMdd-HH:mm:ss.nnnnnn";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern(time_pattern).withZone(ZoneId.systemDefault());

    // HDF group names
    private static String TRIGGER = "/TRIGGER";
    private static String TRIGGER_TIME_STAMPS = "Time_stamps";

    public static final String TRIGGER_TIME_PROPERTY = "Trigger_Timestamp";
    public static final String TRIGGER_TIME_ATTRIBUTE = "epoch";

    @Override
    public WaveformIndex process(WaveformIndex index)
    {
        try
        {
            WaveformFileProperty triggerTimeStampProperty = new WaveformFileProperty(TRIGGER_TIME_PROPERTY);

            File file = new File(index.getFile());
            final H5File h5file = new H5File(file.getAbsolutePath(), FileFormat.READ);
            long file_id = -1;
            file_id = h5file.open();


            HObject theRoot = h5file.getRootObject();
            Group root = (Group) theRoot;
            root.breadthFirstMemberList().stream().forEach(h ->
            {
                if (h instanceof H5Group)
                {
                    if (h.getFullName().equalsIgnoreCase(TRIGGER))
                    {
                        logger.info("Processing Tiggers ");
                        ((H5Group) h).getMemberList().stream().forEach(m -> {
                            if (m instanceof Dataset) {
                                Dataset data = (Dataset) m;
                                String groupName = data.getName();
                                logger.info("Processing Trigger Timestamp : " + groupName);
                                if(TRIGGER_TIME_STAMPS.equalsIgnoreCase(groupName))
                                {
                                    // read the pv names
                                    H5ScalarDS pvNames = new H5ScalarDS(h5file, groupName, data.getPath());
                                    try {
                                        List<Instant> timestamps = List.of((String[]) pvNames.getData()).stream()
                                                                    .map(TIME_FORMAT::parse)
                                                                    .map(Instant::from)
                                                                    .collect(Collectors.toList());

                                        timestamps.forEach(t -> {
                                            triggerTimeStampProperty.addAttribute(
                                                    new WaveformFileAttribute(TRIGGER_TIME_ATTRIBUTE, String.valueOf(t.toEpochMilli())));
                                        });
                                        logger.info("Processing Trigger timestamp : " + timestamps);
                                    } catch (Exception e)
                                    {
                                        logger.log(Level.WARNING, "Failed to process the trigger timestamp", e);
                                    }
                                }
                            }
                        });
                    }
                }
            });
            index.addProperty(triggerTimeStampProperty);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return index;
    }
}
