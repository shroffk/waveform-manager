package org.phoebus.services.waveform.index.entity;

import java.util.Objects;

/**
 * An class describing an attribute of either a {@link WaveformFileProperty} or {@link WaveformFilePVProperty}
 */
public class WaveformFileAttribute {

    private String name;
    private String value;

    WaveformFileAttribute() {

    }

    /**
     * Contrustor for a {@link WaveformFileProperty} or {@link WaveformFilePVProperty} attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public WaveformFileAttribute(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaveformFileAttribute)) return false;
        WaveformFileAttribute that = (WaveformFileAttribute) o;
        return name.equals(that.name) &&
                value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
}
