package org.phoebus.services.waveform.index.timestamp;

import hdf.object.Dataset;
import hdf.object.FileFormat;
import hdf.object.Group;
import hdf.object.HObject;
import hdf.object.h5.H5File;
import hdf.object.h5.H5Group;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.epics.waveform.index.util.spi.ProcessWaveformIndex;

import java.io.File;
import java.util.logging.Logger;

/**
 * Process the time stamp for the index file
 */
public class ProcessTimestamp  implements ProcessWaveformIndex
{
    // HDF group names
    private static String TRIGGER = "/TRIGGER";
    private static String PV_NAMES = "/TRIGGER";
    private static String PV_NAME = "/TRIGGER";

    private static final Logger logger = Logger.getLogger(ProcessTimestamp.class.getName());

    @Override
    public WaveformIndex process(WaveformIndex index)
    {
        try {
            File file = new File(index.getFile());
            final H5File h5file = new H5File(file.getAbsolutePath(), FileFormat.READ);
            long file_id = -1;
            file_id = h5file.open();

            HObject theRoot = h5file.getRootObject();
            Group root = (Group) theRoot;
            root.breadthFirstMemberList().stream().forEach( h -> {
                if (h instanceof H5Group) {

                    if (h.getFullName().equalsIgnoreCase(TRIGGER)) {
                        logger.info("Processing Tiggers ");

                        ((H5Group) h).getMemberList().stream().forEach(m -> {
                            if (m instanceof Dataset) {
                                Dataset data = (Dataset) m;
                                String groupName = data.getName();
                                logger.info("Processing group: " + groupName);
                            }
                        });
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return index;
    }
}
