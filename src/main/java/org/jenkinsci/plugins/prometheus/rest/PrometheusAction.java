package org.jenkinsci.plugins.prometheus-plugins.rest;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import io.prometheus.client.CollectorRegistry;
import org.jenkinsci.plugins.fabric8.prometheus.JobCollector;
import org.jenkinsci.plugins.fabric8.prometheus.MetricsRequest;

/**
 */
@Extension
public class PrometheusAction implements UnprotectedRootAction {
    private CollectorRegistry collectorRegistry;
    private JobCollector jobCollector = new JobCollector();

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return "Prometheus Metrics Exporter";
    }

    @Override
    public String getUrlName() {
        return "prometheus";
    }

    public Object doIndex() {
        if (collectorRegistry == null) {
            collectorRegistry = CollectorRegistry.defaultRegistry;
            collectorRegistry.register(jobCollector);
        }
        return MetricsRequest.jsonResponse(collectorRegistry);
    }
}