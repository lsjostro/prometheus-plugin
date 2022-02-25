package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.apache.commons.lang.StringUtils;

public class ConfigurationUtils {
    public static String getNamespace() {
        // get the namespace from the environment first
        String namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (StringUtils.isEmpty(namespace)) {
            // when the environment variable isn't set, try the system configuration
            return PrometheusConfiguration.get().getDefaultNamespace();
        }
        return namespace;
    }

    public static String getSubSystem() {
        return "jenkins";
    }

    public static boolean getCollectDiskUsage() {
        return PrometheusConfiguration.get().getCollectDiskUsage();
    }
}
