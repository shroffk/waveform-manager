package org.epics.waveform.index.util.entity;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        List<WaveformFileTag> existingTag = this.tags.stream().filter((t) -> {
            return t.getName().equals(tag.getName());
        }).collect(Collectors.toList());

        if (existingTag.size() == 0) {
            this.tags.add(tag);
        }
    }

    public void removeTag(WaveformFileTag tag) {
        this.tags.remove(tag);
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

    public void removeProperty(String property) {
        setProperties(this.properties.stream().filter(p -> { return !p.getName().equalsIgnoreCase(property); }).collect(Collectors.toList()));
    }

    public List<WaveformFilePVProperty> getPvProperties() {
        return pvProperties;
    }

    public void setPvProperties(List<WaveformFilePVProperty> pvProperties) {
        this.pvProperties = pvProperties;
    }

    public void addPvProperty(WaveformFilePVProperty pvProperty) {
        List<WaveformFilePVProperty> existingPvProperty = this.pvProperties.stream().filter((pv) -> {
            return pv.getPvName().equals(pvProperty.getPvName());
        }).collect(Collectors.toList());

        if (existingPvProperty.size() > 0) {
            Set<String> newAttributes = pvProperty.getAttributes().stream().map(WaveformFileAttribute::getName).collect(Collectors.toSet());
            Set<WaveformFileAttribute> updatedAttributes = existingPvProperty.get(0).getAttributes().stream().filter(attribute -> {
                        return !newAttributes.contains(attribute.getName());
                    }
            ).collect(Collectors.toSet());
            updatedAttributes.addAll(pvProperty.getAttributes());
            existingPvProperty.get(0).setAttributes(updatedAttributes);
        } else {
            this.pvProperties.add(pvProperty);
        }
    }

    public void removePvProperty(String pvProperty) {
        setPvProperties(this.pvProperties.stream().filter(p -> { return !p.getPvName().equalsIgnoreCase(pvProperty); }).collect(Collectors.toList()));
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
