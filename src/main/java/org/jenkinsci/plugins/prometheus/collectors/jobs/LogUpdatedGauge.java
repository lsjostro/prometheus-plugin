package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.model.Job;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;
import org.jenkinsci.plugins.prometheus.collectors.builds.BuildsMetricCollector;

public class LogUpdatedGauge extends BuildsMetricCollector<Job<?, ?>, Gauge> {

    protected LogUpdatedGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.JOB_LOG_UPDATED_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Provides a hint if a job is still logging. Uses Jenkins function Job#isLogUpdated";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Job<?, ?> jenkinsObject, String[] labelValues) {

        if (jenkinsObject != null && jenkinsObject.isBuilding()) {
            boolean logUpdated = jenkinsObject.isLogUpdated();
            this.collector.labels(labelValues).set(logUpdated ? 1.0 : 0.0);
        }
    }
}
