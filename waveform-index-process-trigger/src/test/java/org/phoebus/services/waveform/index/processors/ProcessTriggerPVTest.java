package org.phoebus.services.waveform.index.processors;

import org.epics.waveform.index.util.entity.WaveformFileAttribute;
import org.epics.waveform.index.util.entity.WaveformFileProperty;
import org.epics.waveform.index.util.entity.WaveformIndex;
import org.junit.Test;
import org.phoebus.hdf.util.HDFConfigure;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.phoebus.services.waveform.index.processors.ProcessTriggerPV.TRIGGER_PV_PROPERTY;
import static org.phoebus.services.waveform.index.processors.ProcessTriggerPV.TRIGGER_PV_NAME_ATTRIBUTE;

public class ProcessTriggerPVTest {

    private ExecutorService executor
            = Executors.newSingleThreadExecutor();

    @Test
    public void extractTriggerPV() throws URISyntaxException, ExecutionException, InterruptedException
    {
        HDFConfigure.initialize();

        String resourceName = "timestamp-test1.h5";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());

        WaveformIndex index = new WaveformIndex(file.toURI());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        WaveformIndex result = executor.submit(() -> {
            return new ProcessTriggerPV().process(index);
        }).get();


        WaveformIndex expectedResult = new WaveformIndex(file.toURI());
        WaveformFileProperty attr = new WaveformFileProperty(TRIGGER_PV_PROPERTY);
        attr.addAttribute(new WaveformFileAttribute(TRIGGER_PV_NAME_ATTRIBUTE, "SR:C23-BI{BPM:10}PM:Status-I"));
        expectedResult.addProperty(attr);
        assertEquals("Failed to parse trigger pvs for : " + file.getName() , expectedResult, result);

    }

}