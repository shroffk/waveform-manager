package org.phoebus.services.waveform.index.entity;

import java.util.ArrayList;
import java.util.List;

public class WaveformFilePVProperty {
    private String pvname;
    private List<WaveformFileAttribute> attributes = new ArrayList<>();

    public WaveformFilePVProperty(String pvname) {
        this.pvname = pvname;
    }

    public String getPvname() {
        return pvname;
    }

    public void setPvname(String pvname) {
        this.pvname = pvname;
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
