package org.jenkinsci.plugins.prometheus;

import com.cloudbees.simplediskusage.QuickDiskUsagePlugin;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskUsageCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(DiskUsageCollector.class);
    private Jenkins jenkins;
    private Gauge directoryUsageGauge;
    private Gauge jobUsageGauge;
    private boolean collectDiskUsage;

    public DiskUsageCollector() {
        jenkins = Jenkins.get();
        directoryUsageGauge = Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem())
                .name("disk_usage_bytes")
                .labelNames("directory")
                .help("Disk usage of first level folder in JENKINS_HOME in bytes")
                .create();
        jobUsageGauge = Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem())
                .name("job_usage_bytes")
                .labelNames("jobName", "url")
                .help("Amount of disk usage (bytes) for each job in Jenkins")
                .create();

        this.collectDiskUsage = PrometheusConfiguration.get().getDefaultCollectDiskUsage();
    }

    @Override
    @Nonnull
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> samples = new ArrayList<>();
        if(!this.collectDiskUsage) { return samples; }

        try {
            QuickDiskUsagePlugin diskUsagePlugin = jenkins.getPlugin(QuickDiskUsagePlugin.class);
            if (diskUsagePlugin == null) {
                logger.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not installed.");
                return samples;
            }
            diskUsagePlugin.getDirectoriesUsages().forEach(
                    i -> directoryUsageGauge.labels(i.getDisplayName())
                            .set(i.getUsage() * 1024)
            );
            samples.addAll(directoryUsageGauge.collect());

            diskUsagePlugin.getJobsUsages().forEach(
                    i -> jobUsageGauge.labels(i.getFullName(), i.getUrl())
                            .set(i.getUsage() * 1024)
            );
            samples.addAll(jobUsageGauge.collect());
        } catch (IOException e) {
            logger.warn("Cannot get disk usage data due to an unexpected error", e);
        }
        return samples;
    }
}
