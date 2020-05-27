package org.phoebus.app.waveform.index.viewer.entity;

import java.util.Objects;

public class WaveformFileTag {
    private String name;

    WaveformFileTag() {

    }
    
    public WaveformFileTag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaveformFileTag)) return false;
        WaveformFileTag that = (WaveformFileTag) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
