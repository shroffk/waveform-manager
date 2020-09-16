package org.epics.waveform.index.util.entity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class WaveformFilePVProperty {
    private String pvName;
    private Set<WaveformFileAttribute> attributes = new HashSet<>();

    WaveformFilePVProperty() {

    }

    public WaveformFilePVProperty(String pvname) {
        this.pvName = pvname;
    }

    public String getPvName() {
        return pvName;
    }

    public void setPvName(String pvname) {
        this.pvName = pvname;
    }

    public Set<WaveformFileAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<WaveformFileAttribute> attributes) {
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
        if (!(o instanceof WaveformFilePVProperty)) return false;
        WaveformFilePVProperty that = (WaveformFilePVProperty) o;
        return pvName.equals(that.pvName) &&
                attributes.equals(that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pvName, attributes);
    }
}
