package org.jenkinsci.plugins.prometheus.metrics.builds;

import hudson.model.Run;
import hudson.tasks.test.AbstractTestResultAction;
import io.prometheus.client.Gauge;
import org.jenkinsci.plugins.prometheus.metrics.TestBasedMetricCollector;

public class SkippedTestsGauge extends TestBasedMetricCollector<Run, Gauge> {

    public SkippedTestsGauge(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    @Override
    protected Gauge initCollector() {
        return Gauge.build()
                .name(calculateName("last_build_tests_skipped"))
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNames)
                .help("Number of skipped tests during the last build")
                .create();
    }

    @Override
    public void calculateMetric(Run jenkinsObject, String[] labelValues) {
        if (!canBeCalculated(jenkinsObject)) {
            return;
        }

        int testsSkipped = jenkinsObject.getAction(AbstractTestResultAction.class).getSkipCount();
        collector.labels(labelValues).set(testsSkipped);
    }
}
