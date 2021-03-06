package org.apereo.cas.web.report.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.apereo.cas.util.ResourceUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.util.Map;
import java.util.Optional;

/**
 * This is {@link ControllerUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@UtilityClass
public class ControllerUtils {

    /**
     * Configure model map for config server cloud bus endpoints.
     *
     * @param path  the path
     * @param model the model
     */
    public static void configureModelMapForConfigServerCloudBusEndpoints(final String path, final Map model) {
        model.put("refreshEndpoint", path + "/status/refresh");
        model.put("refreshMethod", "POST");
    }

    /**
     * Build logger context logger context.
     *
     * @param environment    the environment
     * @param resourceLoader the resource loader
     * @return the logger context
     */
    @SneakyThrows
    public static Optional<Pair<Resource, LoggerContext>> buildLoggerContext(final Environment environment, final ResourceLoader
            resourceLoader) {
        final String logFile = environment.getProperty("logging.config", "classpath:/log4j2.xml");
        LOGGER.debug("Located logging configuration reference in the environment as [{}]", logFile);

        if (ResourceUtils.doesResourceExist(logFile, resourceLoader)) {
            final Resource logConfigurationFile = resourceLoader.getResource(logFile);
            LOGGER.debug("Loaded logging configuration resource [{}]. Initializing logger context...", logConfigurationFile);
            final LoggerContext loggerContext = Configurator.initialize("CAS", null, logConfigurationFile.getURI());
            LOGGER.debug("Installing log configuration listener to detect changes and update");
            loggerContext.getConfiguration().addListener(reconfigurable -> loggerContext.updateLoggers(reconfigurable.reconfigure()));
            return Optional.of(Pair.of(logConfigurationFile, loggerContext));
        }
        LOGGER.warn("Logging configuration cannot be found in the environment settings");
        return Optional.empty();
    }
}
