package org.phoebus.services.waveform.index.spi;

import org.phoebus.services.waveform.index.entity.WaveformIndex;

import java.util.concurrent.FutureTask;

/**
 * SPI for processing waveform index files
 */
public interface ProcessWaveformIndex
{
    public FutureTask<WaveformIndex> process(WaveformIndex index);
}
