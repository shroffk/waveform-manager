package org.phoebus.services.waveform.index.entity;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * A class representing an index for a waveform file and its associated tags and properties.
 */
public class WaveformIndex {
    // The hdf file location
    private URI file;

    // The list of tags
    private List<WaveformFileTag> tags = Collections.emptyList();
    // The list of properties
    private List<WaveformFileProperties> properties = Collections.emptyList();
    // The list of pv properties
    private List<WaveformFilePVProperties> pvProperties = Collections.emptyList();

    public WaveformIndex(URI file) {
        this.file = file;
    }

    public URI getFile() {
        return file;
    }

    public void setFile(URI file) {
        this.file = file;
    }

    public List<WaveformFileTag> getTags() {
        return tags;
    }

    public void setTags(List<WaveformFileTag> tags) {
        this.tags = tags;
    }

    public List<WaveformFileProperties> getProperties() {
        return properties;
    }

    public void setProperties(List<WaveformFileProperties> properties) {
        this.properties = properties;
    }

    public void addProperties(WaveformFileProperties property) {
        this.properties.add(property);
    }

    public List<WaveformFilePVProperties> getPvProperties() {
        return pvProperties;
    }

    public void setPvProperties(List<WaveformFilePVProperties> pvProperties) {
        this.pvProperties = pvProperties;
    }

    public void addPvProperties(WaveformFilePVProperties pvProperty) {
        this.pvProperties.add(pvProperty);
    }
}
