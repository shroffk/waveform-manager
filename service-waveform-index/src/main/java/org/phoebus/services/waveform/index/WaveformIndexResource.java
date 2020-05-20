package org.phoebus.services.waveform.index;

import org.phoebus.services.waveform.index.entity.WaveformFilePVProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileTag;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("waveformIndex")
public class WaveformIndexResource {

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;

    /**
     * GET a single waveformIndex identified by FileURL
     * @param fileURI the {@link WaveformIndex} fileURI
     * @return WaveformIndex identified by fileURI
     */
    @GetMapping("{fileURI}")
    public WaveformIndex getIndex(@PathVariable String fileURI) {
        Optional<WaveformIndex> result = waveformIndexRepository.get(fileURI);
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to retrieve index:  " + fileURI + " , no such index exits");
        } else {
            return result.get();
        }
    }

    /**
     * List all the indexes
     * @return All {@link WaveformIndex}s
     */
    @GetMapping
    public List<WaveformIndex> getIndex() {
        return null;
    }

    @PutMapping
    public WaveformIndex createIndex(@RequestBody final WaveformIndex waveformIndex) {
        return waveformIndexRepository.save(waveformIndex);
    }

    @PostMapping("/{fileURI}/{tag}")
    public WaveformIndex addTag(@PathVariable String fileURI, @PathVariable String tag) {
        if (waveformIndexRepository.checkExists(fileURI)) {

            return waveformIndexRepository.addTag(null, null);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add tag to index:  " + fileURI + " , no such index exits");
        }
    }

    @PostMapping("/{fileURI}/properties")
    public WaveformIndex addProperty(@PathVariable String fileURI, @RequestBody final WaveformFileProperty waveformFileProperty) {
        if (waveformIndexRepository.checkExists(fileURI)) {

            return waveformIndexRepository.addProperty(null, null);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add property to index:  " + fileURI + " , no such index exits");
        }
    }

    @PostMapping("/{fileURI}/pvproperties")
    public WaveformIndex addPvProperty(@PathVariable String fileURI, @RequestBody WaveformFilePVProperty waveformFilePVProperty) {
        if (waveformIndexRepository.checkExists(fileURI)) {

            return waveformIndexRepository.addPvProperty(null, null);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add pvProperty to index:  " + fileURI + " , no such index exits");
        }
    }

    @DeleteMapping("/{fileURI}")
    public void deleteIndex(@PathVariable String fileURI) {
        if (waveformIndexRepository.checkExists(fileURI)) {
            waveformIndexRepository.delete(fileURI);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to delete index:  " + fileURI + " , no such index exits");
        }
    }
}
