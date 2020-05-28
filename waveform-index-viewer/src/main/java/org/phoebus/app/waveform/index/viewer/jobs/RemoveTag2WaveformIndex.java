/**
 *
 */
package org.phoebus.app.waveform.index.viewer.jobs;

import com.sun.jersey.api.client.WebResource;
import org.phoebus.framework.jobs.Job;
import org.phoebus.framework.jobs.JobManager;
import org.phoebus.framework.jobs.JobMonitor;
import org.phoebus.framework.jobs.JobRunnable;

import java.net.URLEncoder;
import java.util.Collection;
import java.util.function.BiConsumer;


/**
 * @author Kunal Shroff
 */
public class RemoveTag2WaveformIndex implements JobRunnable {

    private final String tagName;
    private final Collection<String> wavefromIndexFileURIs;
    private final BiConsumer<String, Exception> error_handler;
    private final WebResource client;


    public static Job submit(WebResource client,
                             final Collection<String> wavefromIndexFileURIs,
                             final String tagName,
                             final BiConsumer<String, Exception> error_handler) {
        return JobManager.schedule("Remove tag : " + tagName + " to " + wavefromIndexFileURIs.size() + " files",
                new RemoveTag2WaveformIndex(client, wavefromIndexFileURIs, tagName, error_handler));
    }

    private RemoveTag2WaveformIndex(WebResource client, Collection<String> wavefromIndexFileURIs, String tagName,
                                    BiConsumer<String, Exception> error_handler) {
        super();
        this.client = client;
        this.error_handler = error_handler;
        this.wavefromIndexFileURIs = wavefromIndexFileURIs;
        this.tagName = tagName;
    }

    @Override
    public void run(JobMonitor monitor) throws Exception {
        monitor.beginTask("Remove tag : " + tagName + " to " + wavefromIndexFileURIs.size() + " channels");
        wavefromIndexFileURIs.forEach((fileURI) -> {
            try {
                client.path("/remove/tags/" + tagName).queryParam("fileURI", URLEncoder.encode(fileURI, "UTF-8")).post();
            } catch (Exception e) {
                this.error_handler.accept("failed to remove tag " + tagName, e);
            }
        });
    }


}
