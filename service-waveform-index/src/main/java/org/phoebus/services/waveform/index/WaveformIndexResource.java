package org.phoebus.services.waveform.index;

import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("waveformIndex")
public class WaveformIndexResource {

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;

    @PutMapping()
    public WaveformIndex createProperty(@RequestBody final WaveformIndex waveformIndex) {
        return waveformIndexRepository.save(waveformIndex);
    }

    @DeleteMapping
    public void deleteProperty(@RequestBody final WaveformIndex waveformIndex) {
        waveformIndexRepository.delete(waveformIndex);
    }
}
