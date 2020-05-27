package org.phoebus.app.waveform.index.viewer.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WaveformFilePVProperty {
    private String pvName;
    private List<WaveformFileAttribute> attributes = new ArrayList<>();

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
