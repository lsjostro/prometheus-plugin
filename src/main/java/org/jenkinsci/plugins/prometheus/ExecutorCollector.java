package org.jenkinsci.plugins.prometheus;

import hudson.model.Label;
import hudson.model.LoadStatistics;
import io.prometheus.client.Collector;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.metrics.executors.*;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecutorCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorCollector.class);

    private ExecutorsAvailableGauge executorsAvailable;
    private ExecutorsBusyGauge executorsBusy;
    private ExecutorsConnectingGauge executorsConnecting;
    private ExecutorsDefinedGauge executorsDefined;
    private ExecutorsIdleGauge executorsIdle;
    private ExecutorsOnlineGauge executorsOnline;
    private ExecutorsQueueLengthGauge queueLength;


    public ExecutorCollector() {
    }

    @Override
    public List<MetricFamilySamples> collect() {
        logger.debug("Collecting executor metrics for prometheus");

        String namespace = ConfigurationUtils.getNamespace();
        List<MetricFamilySamples> samples = new ArrayList<>();
        String prefix = "executors";
        String subsystem = ConfigurationUtils.getSubSystem();
        String[] labelNameArray = {"label"};

        // Number of executors (among the online executors) that are available to carry out builds.
        executorsAvailable = new ExecutorsAvailableGauge(labelNameArray, namespace, subsystem, prefix);

        // Number of executors (among the online executors) that are carrying out builds.
        executorsBusy = new ExecutorsBusyGauge(labelNameArray, namespace, subsystem, prefix);

        // Number of executors that are currently in the process of connecting to Jenkins.
        executorsConnecting = new ExecutorsConnectingGauge(labelNameArray, namespace, subsystem, prefix);

        // Number of executors that Jenkins currently knows, this includes all off-line agents.
        executorsDefined = new ExecutorsDefinedGauge(labelNameArray, namespace, subsystem, prefix);

        // Number of executors that are currently on-line and idle.
        executorsIdle = new ExecutorsIdleGauge(labelNameArray, namespace, subsystem, prefix);

        // Sum of all executors across all online computers in this label.
        executorsOnline = new ExecutorsOnlineGauge(labelNameArray, namespace, subsystem, prefix);

        // Number of jobs that are in the build queue, waiting for an available executor of this label.
        queueLength = new ExecutorsQueueLengthGauge(labelNameArray, namespace, subsystem, prefix);

        logger.debug("getting load statistics for Executors");

        Label[] labels = Jenkins.get().getLabels().toArray(new Label[0]);
        for (Label l : labels) {
            appendExecutorMetrics(l.getDisplayName(), l.loadStatistics.computeSnapshot());
        }

        samples.addAll(executorsAvailable.collect());
        samples.addAll(executorsBusy.collect());
        samples.addAll(executorsConnecting.collect());
        samples.addAll(executorsDefined.collect());
        samples.addAll(executorsIdle.collect());
        samples.addAll(executorsOnline.collect());
        samples.addAll(queueLength.collect());

        return samples;
    }

    protected void appendExecutorMetrics(String labelDisplayName, LoadStatistics.LoadStatisticsSnapshot computeSnapshot) {
        String[] labelValueArray = {labelDisplayName};
        executorsAvailable.calculateMetric(computeSnapshot, labelValueArray);
        executorsBusy.calculateMetric(computeSnapshot, labelValueArray);
        executorsConnecting.calculateMetric(computeSnapshot, labelValueArray);
        executorsDefined.calculateMetric(computeSnapshot, labelValueArray);
        executorsIdle.calculateMetric(computeSnapshot, labelValueArray);
        executorsOnline.calculateMetric(computeSnapshot, labelValueArray);
        queueLength.calculateMetric(computeSnapshot, labelValueArray);;
    }
}
