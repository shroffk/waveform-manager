package org.phoebus.services.waveform.index.entity;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class representing an index for a waveform file and its associated tags and properties.
 */
public class WaveformIndex {
    // The hdf file location
    private URI file;

    // The list of tags
    private List<WaveformFileTag> tags = Collections.emptyList();
    // The list of properties
    private List<WaveformFileProperty> properties = Collections.emptyList();
    // The list of pv properties
    private List<WaveformFilePVProperty> pvProperties = Collections.emptyList();

    /**
     * Default constructor for Object Mapping
     */
    WaveformIndex() {

    }

    public WaveformIndex(String fileURI) {
        this.file = URI.create(fileURI);
    }

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

    public void addTag(WaveformFileTag tag) {
        this.tags.add(tag);
    }

    public List<WaveformFileProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<WaveformFileProperty> properties) {
        this.properties = properties;
    }

    public void addProperty(WaveformFileProperty property) {
        this.properties.add(property);
    }

    public List<WaveformFilePVProperty> getPvProperties() {
        return pvProperties;
    }

    public void setPvProperties(List<WaveformFilePVProperty> pvProperties) {
        this.pvProperties = pvProperties;
    }

    public void addPvProperty(WaveformFilePVProperty pvProperty) {
        this.pvProperties.add(pvProperty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaveformIndex)) return false;
        WaveformIndex that = (WaveformIndex) o;
        return file.equals(that.file) &&
                tags.equals(that.tags) &&
                properties.equals(that.properties) &&
                pvProperties.equals(that.pvProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, tags, properties, pvProperties);
    }
}
