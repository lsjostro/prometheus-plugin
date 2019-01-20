package org.jenkinsci.plugins.prometheus.rest;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.UnprotectedRootAction;
import hudson.security.Messages;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import jenkins.metrics.api.Metrics;
import jenkins.model.Jenkins;
import org.acegisecurity.Authentication;
import org.jenkinsci.plugins.prometheus.JenkinsStatusCollector;
import org.jenkinsci.plugins.prometheus.JobCollector;
import org.jenkinsci.plugins.prometheus.MetricsRequest;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class PrometheusAction implements UnprotectedRootAction {
    private CollectorRegistry collectorRegistry;
    private JobCollector jobCollector = new JobCollector();
    private JenkinsStatusCollector jenkinsStatusCollector = new JenkinsStatusCollector();

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
            checkPermission(Metrics.VIEW);
            if (collectorRegistry == null) {
                collectorRegistry = CollectorRegistry.defaultRegistry;
                collectorRegistry.register(jobCollector);
                collectorRegistry.register(jenkinsStatusCollector);
                if (Metrics.metricRegistry() != null) {
                    collectorRegistry.register(new DropwizardExports(Metrics.metricRegistry()));
                }
            }
            return MetricsRequest.prometheusResponse(collectorRegistry);
        }
        throw HttpResponses.notFound();
    }

    private void checkPermission(Permission permission) {
        if (PrometheusConfiguration.get().isUseAuthenticatedEndpoint()) {
            Authentication authentication = Jenkins.getAuthentication();
            if (!Jenkins.getActiveInstance().getACL().hasPermission(authentication, permission)) {
                String message = Messages.AccessDeniedException2_MissingPermission(authentication.getName(),
                                                                                   permission.group.title + "/" + permission.name);
                throw HttpResponses.errorWithoutStack(403, message);
            }
        }
    }
}
