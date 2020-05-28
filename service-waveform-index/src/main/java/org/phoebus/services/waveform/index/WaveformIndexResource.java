package org.phoebus.services.waveform.index;

import org.phoebus.services.waveform.index.entity.WaveformFilePVProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileProperty;
import org.phoebus.services.waveform.index.entity.WaveformFileTag;
import org.phoebus.services.waveform.index.entity.WaveformIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("waveformIndex")
public class WaveformIndexResource {

    @Autowired
    private WaveformIndexRepository waveformIndexRepository;

    @GetMapping("/info")
    public String info() {
        return "Waveform Index service : version 4.6.4";
    }

    /**
     * GET a single waveformIndex identified by FileURL
     * @param fileURI the {@link WaveformIndex} fileURI
     * @return WaveformIndex identified by fileURI
     */
    @GetMapping("{fileURI}")
    public WaveformIndex getIndex(@PathVariable String fileURI) {
        try {
            Optional<WaveformIndex> result = waveformIndexRepository.get(URLDecoder.decode(fileURI, "UTF-8"));
            if (result.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to retrieve index:  " + fileURI + " , no such index exits");
            } else {
                return result.get();
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to retrieve index:  " + fileURI, e);
        }
    }

    @GetMapping()
    public List<WaveformIndex> findLogs(@RequestParam MultiValueMap<String, String> allRequestParams) {
        return waveformIndexRepository.search(allRequestParams);
    }


    @PutMapping
    public WaveformIndex createIndex(@RequestBody final WaveformIndex waveformIndex) {
        return waveformIndexRepository.save(waveformIndex);
    }

    @PostMapping("/{fileURI}/add/tags/{tag}")
    public WaveformIndex addTag(@PathVariable String fileURI, @PathVariable String tag) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                return waveformIndexRepository.addTag(waveformIndexRepository.get(parsedFileURI).get(), new WaveformFileTag(tag));
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add tag to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to add tag to index:  " + fileURI, e);
        }
    }

    @PostMapping("/{fileURI}/remove/tags/{tagName}")
    public WaveformIndex removeTag(@PathVariable String fileURI, @PathVariable String tagName) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                WaveformIndex index = waveformIndexRepository.get(parsedFileURI).get();
                index.removeTag(new WaveformFileTag(tagName));
                return waveformIndexRepository.save(index);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to remove tag to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to remove tag to index:  " + fileURI, e);
        }
    }

    @PostMapping("/{fileURI}/add/properties")
    public WaveformIndex addProperty(@PathVariable String fileURI, @RequestBody final WaveformFileProperty waveformFileProperty) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                return waveformIndexRepository.addProperty(waveformIndexRepository.get(parsedFileURI).get(), waveformFileProperty);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add property to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to  add property to index:  " + fileURI, e);
        }
    }

    @PostMapping("/{fileURI}/remove/properties/{propertyName}")
    public WaveformIndex removeProperty(@PathVariable String fileURI, @PathVariable final String propertyName) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                WaveformIndex index = waveformIndexRepository.get(parsedFileURI).get();
                index.removeProperty(propertyName);
                return waveformIndexRepository.save(index);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to remove property to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to  remove property to index:  " + fileURI, e);
        }
    }

    @PostMapping("/{fileURI}/add/pvproperties")
    public WaveformIndex addPvProperty(@PathVariable String fileURI, @RequestBody WaveformFilePVProperty waveformFilePVProperty) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                return waveformIndexRepository.addPvProperty(waveformIndexRepository.get(parsedFileURI).get(), waveformFilePVProperty);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to add pvProperty to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to  add pvProperty to index:  " + fileURI, e);
        }
    }

    @PostMapping("/{fileURI}/remove/pvproperties/{pvPropertyName}")
    public WaveformIndex removePvProperty(@PathVariable String fileURI, @PathVariable final String pvPropertyName) {
        try {
            String parsedFileURI = URLDecoder.decode(fileURI, "UTF-8");
            if (waveformIndexRepository.checkExists(parsedFileURI)) {
                WaveformIndex index = waveformIndexRepository.get(parsedFileURI).get();
                index.removePvProperty(pvPropertyName);
                return waveformIndexRepository.save(index);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to remove pv property to index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to  remove pvProperty to index:  " + fileURI, e);
        }
    }

    @DeleteMapping("/{fileURI}")
    public void deleteIndex(@PathVariable String fileURI) {
        try {
            if (waveformIndexRepository.checkExists(URLDecoder.decode(fileURI, "UTF-8"))) {
                waveformIndexRepository.delete(fileURI);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Failed to delete index:  " + fileURI + " , no such index exits");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to delete index:  " + fileURI, e);
        }
    }

}
