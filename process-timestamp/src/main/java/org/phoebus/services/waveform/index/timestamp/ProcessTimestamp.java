package org.phoebus.services.waveform.index.timestamp;

import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.phoebus.services.waveform.index.spi.ProcessWaveformIndex;

import java.util.concurrent.FutureTask;

/**
 * Process the time stamp for the index file
 */
public class ProcessTimestamp  implements ProcessWaveformIndex
{
    @Override
    public FutureTask<WaveformIndex> process(WaveformIndex index)
    {
        return null;
    }
}
