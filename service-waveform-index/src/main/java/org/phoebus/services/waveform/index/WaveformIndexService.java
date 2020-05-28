package org.phoebus.services.waveform.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.util.logging.Logger;

/**
 *
 */
@SpringBootApplication
@ComponentScan(basePackages = { "org.phoebus" })
public class WaveformIndexService {

     /**
     * Alarm system logger
     */
    public static final Logger logger = Logger.getLogger(WaveformIndexService.class.getPackageName());
    private static ConfigurableApplicationContext context;

    public static void main(final String[] original_args) throws Exception {
        context = SpringApplication.run(WaveformIndexService.class, original_args);
    }

}
