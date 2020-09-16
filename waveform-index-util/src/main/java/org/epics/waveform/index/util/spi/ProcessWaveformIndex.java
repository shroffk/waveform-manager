package org.epics.waveform.index.util.spi;

import org.epics.waveform.index.util.entity.WaveformIndex;

/**
 * SPI for processing waveform index files
 */
public interface ProcessWaveformIndex
{
    public WaveformIndex process(WaveformIndex index) throws Exception;
}
