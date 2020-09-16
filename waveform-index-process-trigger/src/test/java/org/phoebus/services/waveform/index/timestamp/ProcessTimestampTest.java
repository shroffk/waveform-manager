package org.phoebus.services.waveform.index.timestamp;

import org.epics.waveform.index.util.entity.WaveformIndex;
import org.junit.Test;
import org.phoebus.hdf.util.HDFConfigure;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProcessTimestampTest {

    private ExecutorService executor
            = Executors.newSingleThreadExecutor();

    @Test
    public void extractTimestamp() throws URISyntaxException, ExecutionException, InterruptedException
    {
        HDFConfigure.initialize();

        String resourceName = "timestamp-test1.h5";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());

        WaveformIndex index = new WaveformIndex(file.toURI());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        WaveformIndex result = executor.submit(() -> {
            return new ProcessTimestamp().process(index);
        }).get();

    }

}