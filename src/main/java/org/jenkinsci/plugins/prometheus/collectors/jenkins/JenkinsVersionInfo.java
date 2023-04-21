package org.jenkinsci.plugins.prometheus.collectors.jenkins;

import io.prometheus.client.Info;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public class JenkinsVersionInfo extends BaseMetricCollector<Jenkins, Info> {

    public JenkinsVersionInfo(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected Info initCollector() {
        return Info.build()
                .name("version")
                .help("Jenkins Application Version")
                .subsystem(subsystem)
                .namespace(namespace)
                .create();
    }

    @Override
    public void calculateMetric(Jenkins jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.info("version", Jenkins.VERSION);
    }
}
