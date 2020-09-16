package org.phoebus.services.waveform.index.processors;

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
public class ProcessTriggerPV implements ProcessWaveformIndex
{
    private static final Logger logger = Logger.getLogger(ProcessTriggerPV.class.getName());

    // HDF group names
    private static String TRIGGER = "/TRIGGER";
    private static String TRIGGER_PVS = "PV_names";

    public static final String TRIGGER_PV_PROPERTY = "Trigger_PV";
    public static final String TRIGGER_PV_NAME_ATTRIBUTE = "pv_name";

    @Override
    public WaveformIndex process(WaveformIndex index)
    {
        try
        {
            WaveformFileProperty triggerPVProperty = new WaveformFileProperty(TRIGGER_PV_PROPERTY);

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
                        logger.info("Processing Triggers ");
                        ((H5Group) h).getMemberList().stream().forEach(m -> {
                            if (m instanceof Dataset) {
                                Dataset data = (Dataset) m;
                                String groupName = data.getName();
                                logger.info("Processing Trigger pvs : " + groupName);
                                if(TRIGGER_PVS.equalsIgnoreCase(groupName))
                                {
                                    // read the pv names
                                    H5ScalarDS pvNamesData = new H5ScalarDS(h5file, groupName, data.getPath());
                                    try {
                                        List<String> pvNames = List.of((String[]) pvNamesData.getData());

                                        pvNames.forEach(pv -> {
                                            triggerPVProperty.addAttribute(
                                                    new WaveformFileAttribute(TRIGGER_PV_NAME_ATTRIBUTE, pv));
                                        });
                                        logger.info("Processing Trigger pv : " + pvNames);
                                    } catch (Exception e)
                                    {
                                        logger.log(Level.WARNING, "Failed to process the trigger pv ", e);
                                    }
                                }
                            }
                        });
                    }
                }
            });
            index.addProperty(triggerPVProperty);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return index;
    }
}
