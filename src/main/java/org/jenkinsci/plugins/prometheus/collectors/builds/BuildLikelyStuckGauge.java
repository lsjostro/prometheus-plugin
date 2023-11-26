package org.jenkinsci.plugins.prometheus.collectors.builds;

import hudson.model.Executor;
import hudson.model.Run;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import org.jenkinsci.plugins.prometheus.collectors.CollectorType;

public class BuildLikelyStuckGauge extends BuildsMetricCollector<Run<?, ?>, Gauge> {

    protected BuildLikelyStuckGauge(String[] labelNames, String namespace, String subsystem, String prefix) {
        super(labelNames, namespace, subsystem, prefix);
    }

    @Override
    protected CollectorType getCollectorType() {
        return CollectorType.BUILD_LIKELY_STUCK_GAUGE;
    }

    @Override
    protected String getHelpText() {
        return "Provides a hint if a build is likely stuck. Uses Jenkins function Executor#isLikelyStuck.";
    }

    @Override
    protected SimpleCollector.Builder<?, Gauge> getCollectorBuilder() {
        return Gauge.build();
    }

    @Override
    public void calculateMetric(Run<?, ?> run, String[] labelValues) {
        if (run == null || !run.isBuilding()) {
            return;
        }

        Executor executor = run.getExecutor();
        if (executor == null) {
            return;
        }

        double likelyStuckDoubleValue = executor.isLikelyStuck() ? 1.0 : 0.0;
        this.collector.labels(labelValues).set(likelyStuckDoubleValue);
    }
}
