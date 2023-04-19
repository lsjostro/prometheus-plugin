package org.jenkinsci.plugins.prometheus.metrics.jenkins;

import hudson.model.Computer;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.metrics.BaseMetricCollector;

public class JenkinsUptimeGauge extends BaseMetricCollector<Jenkins, Gauge> {

    public JenkinsUptimeGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name("uptime")
                .labelNames()
                .subsystem(subsystem)
                .namespace(namespace)
                .help("Time since Jenkins machine was initialized")
                .create();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        Computer computer = jenkinsObject.toComputer();
        if (computer == null) {
            return;
        }
        long upTime = computer.getConnectTime();
        collector.set(System.currentTimeMillis() - upTime);
    }
}
