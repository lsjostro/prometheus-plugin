package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class JenkinsUpGauge extends BaseMetricCollector<Jenkins, Gauge> {

    public JenkinsUpGauge(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name("up")
                .labelNames()
                .subsystem(subsystem)
                .namespace(namespace)
                .help("Is Jenkins ready to receive requests")
                .create();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.set(jenkinsObject.getInitLevel() == hudson.init.InitMilestone.COMPLETED ? 1 : 0);
    }
}
