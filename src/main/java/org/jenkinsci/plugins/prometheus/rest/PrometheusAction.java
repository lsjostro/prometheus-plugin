package org.jenkinsci.plugins.prometheus.rest;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.util.HttpResponses;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.hotspot.DefaultExports;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.JenkinsStatusCollector;
import org.jenkinsci.plugins.prometheus.JobCollector;
import org.jenkinsci.plugins.prometheus.MetricsRequest;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PrometheusAction implements UnprotectedRootAction {

    private CollectorRegistry collectorRegistry;

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
        return PrometheusConfiguration.get().getUrlName();
    }

    public Object doDynamic(StaplerRequest request) {
        if (request.getRestOfPath().equals(PrometheusConfiguration.get().getAdditionalPath())) {
            checkPermission();
            if (collectorRegistry == null) {
                collectorRegistry = CollectorRegistry.defaultRegistry;
                collectorRegistry.register(new JobCollector());
                collectorRegistry.register(new JenkinsStatusCollector());
                collectorRegistry.register(new DropwizardExports(Metrics.metricRegistry()));
                DefaultExports.initialize();
            }
            return MetricsRequest.prometheusResponse(collectorRegistry);
        }
        throw HttpResponses.notFound();
    }

    private void checkPermission() {
        if (PrometheusConfiguration.get().isUseAuthenticatedEndpoint()) {
            if (!Jenkins.getInstance().hasPermission(Metrics.VIEW)) {
                throw HttpResponses.forbidden();
            }
        }
    }
}
