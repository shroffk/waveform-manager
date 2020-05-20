package org.phoebus.services.waveform.index.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WaveformFileProperty {
    private String name;
    private List<WaveformFileAttribute> attributes = new ArrayList<>();

    WaveformFileProperty() {
    }

    public WaveformFileProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WaveformFileAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<WaveformFileAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(WaveformFileAttribute attribute) {
        this.attributes.add(attribute);
    }

    public void removeAttribute(WaveformFileAttribute attribute) {
        this.attributes.remove(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaveformFileProperty)) return false;
        WaveformFileProperty that = (WaveformFileProperty) o;
        return name.equals(that.name) &&
                attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, attributes);
    }
}
