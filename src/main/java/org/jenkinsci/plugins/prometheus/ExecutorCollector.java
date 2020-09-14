package org.jenkinsci.plugins.prometheus;

import hudson.model.Label;
import hudson.model.LoadStatistics;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExecutorCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorCollector.class);

    private Gauge executorsAvailable;
    private Gauge executorsBusy;
    private Gauge executorsConnecting;
    private Gauge executorsDefined;
    private Gauge executorsIdle;
    private Gauge executorsOnline;
    private Gauge queueLength;


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
        executorsAvailable = Gauge.build()
                .name(prefix + "_available")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Available")
                .create();

        // Number of executors (among the online executors) that are carrying out builds.
        executorsBusy = Gauge.build()
                .name(prefix + "_busy")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Busy")
                .create();

        // Number of executors that are currently in the process of connecting to Jenkins.
        executorsConnecting = Gauge.build()
                .name(prefix + "_connecting")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Connecting")
                .create();

        // Number of executors that Jenkins currently knows, this includes all off-line agents.
        executorsDefined = Gauge.build()
                .name(prefix + "_defined")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Defined")
                .create();

        // Number of executors that are currently on-line and idle.
        executorsIdle = Gauge.build()
                .name(prefix + "_idle")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Idle")
                .create();

        // Sum of all executors across all online computers in this label.
        executorsOnline = Gauge.build()
                .name(prefix + "_online")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Online")
                .create();

        // Number of jobs that are in the build queue, waiting for an available executor of this label.
        queueLength = Gauge.build()
                .name(prefix + "_queue_length")
                .subsystem(subsystem).namespace(namespace)
                .labelNames(labelNameArray)
                .help("Executors Queue Length")
                .create();

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
        executorsAvailable.labels(labelValueArray).set(computeSnapshot.getAvailableExecutors());
        executorsBusy.labels(labelValueArray).set(computeSnapshot.getBusyExecutors());
        executorsConnecting.labels(labelValueArray).set(computeSnapshot.getConnectingExecutors());
        executorsDefined.labels(labelValueArray).set(computeSnapshot.getDefinedExecutors());
        executorsIdle.labels(labelValueArray).set(computeSnapshot.getIdleExecutors());
        executorsOnline.labels(labelValueArray).set(computeSnapshot.getOnlineExecutors());
        queueLength.labels(labelValueArray).set(computeSnapshot.getQueueLength());
    }
}
