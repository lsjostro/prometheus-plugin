package org.jenkinsci.plugins.prometheus.util;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

public class ConfigurationUtils {
    public static String getNamespace() {
        // get the namespace from the environment first
        String namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (StringUtils.isEmpty(namespace)) {
            // when the environment variable isn't set, try the system configuration
            namespace = PrometheusConfiguration.get().getDefaultNamespace();
        }
        return namespace;
    }
}
