package org.jenkinsci.plugins.prometheus.metrics.executors;

import hudson.model.LoadStatistics;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.metrics.BaseMetricCollector;

public class ExecutorsBusyGauge extends BaseMetricCollector<LoadStatistics.LoadStatisticsSnapshot, Gauge> {


    public ExecutorsBusyGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("busy"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Executors Busy")
                .create();
    }

    @Override
    public void calculateMetric(LoadStatistics.LoadStatisticsSnapshot jenkinsObject, String[] labelValues) {
        if (jenkinsObject == null) {
            return;
        }
        this.collector.labels(labelValues).set(jenkinsObject.getBusyExecutors());
    }
}
