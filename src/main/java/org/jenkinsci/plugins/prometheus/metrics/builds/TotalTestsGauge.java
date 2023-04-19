package org.jenkinsci.plugins.prometheus.metrics.builds;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.metrics.TestBasedMetricCollector;

public class TotalTestsGauge extends TestBasedMetricCollector<Run, Gauge> {
    public TotalTestsGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return  Gauge.build()
                .name(calculateName("build_tests_total"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Number of total tests during the last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (!canBeCalculated(jenkinsObject)) {
            return;
        }

        int testsTotal = jenkinsObject.getAction(AbstractTestResultAction.class).getTotalCount();
        this.collector.labels(labelValues).set(testsTotal);
    }
}
