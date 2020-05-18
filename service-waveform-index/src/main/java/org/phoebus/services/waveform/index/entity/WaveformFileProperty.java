package org.phoebus.services.waveform.index.entity;

import java.util.ArrayList;
import java.util.List;

public class WaveformFileProperties {
    private String name;
    private List<WaveformFileAttribute> attributes = new ArrayList<>();

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
}
