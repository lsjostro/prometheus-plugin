package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiskUsageCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(DiskUsageCollector.class);

    private static Gauge newDirectoryUsageGauge() {
        return Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem())
                .name("disk_usage_bytes")
                .labelNames("directory")
                .help("Disk usage of first level folder in JENKINS_HOME in bytes")
                .create();
    }

    private static Gauge newJobUsageGauge() {
        return Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem())
                .name("job_usage_bytes")
                .labelNames("jobName", "url")
                .help("Amount of disk usage (bytes) for each job in Jenkins")
                .create();
    }

    @Override
    @Nonnull
    public List<MetricFamilySamples> collect() {
        if (!ConfigurationUtils.getCollectDiskUsage()) {
            return Collections.emptyList();
        }

        try {
            return collectDiskUsage();
        } catch (final IOException | RuntimeException e) {
            logger.warn("Failed to get disk usage data due to an unexpected error.", e);
            return Collections.emptyList();
        } catch (final java.lang.NoClassDefFoundError e) {
            logger.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not installed: " + e);
            return Collections.emptyList();
        }
    }

    @Nonnull
    private static List<MetricFamilySamples> collectDiskUsage() throws IOException {
        final com.cloudbees.simplediskusage.QuickDiskUsagePlugin diskUsagePlugin = Jenkins.get()
            .getPlugin(com.cloudbees.simplediskusage.QuickDiskUsagePlugin.class);
        if (diskUsagePlugin == null) {
            logger.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not loaded.");
            return Collections.emptyList();
        }

        final List<MetricFamilySamples> samples = new ArrayList<>();

        final Gauge directoryUsageGauge = newDirectoryUsageGauge();
        diskUsagePlugin.getDirectoriesUsages().forEach(
                i -> directoryUsageGauge.labels(i.getDisplayName())
                        .set(i.getUsage() * 1024)
        );
        samples.addAll(directoryUsageGauge.collect());

        final Gauge jobUsageGauge = newJobUsageGauge();
        diskUsagePlugin.getJobsUsages().forEach(
                i -> jobUsageGauge.labels(i.getFullName(), i.getUrl())
                        .set(i.getUsage() * 1024)
        );
        samples.addAll(jobUsageGauge.collect());
        return samples;
    }
}
