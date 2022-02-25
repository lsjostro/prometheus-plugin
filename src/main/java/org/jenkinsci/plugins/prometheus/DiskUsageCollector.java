package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.prometheus.config.PrometheusConfiguration;
import org.jenkinsci.plugins.prometheus.util.ConfigurationUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DiskUsageCollector extends Collector {

    private static final Logger logger = LoggerFactory.getLogger(DiskUsageCollector.class);

    private static Gauge newDirectoryUsageGauge() {
        return newGaugeBuilder()
                .name("disk_usage_bytes")
                .labelNames("file_store", "directory")
                .help("Disk usage of first level folder in JENKINS_HOME in bytes")
                .create();
    }

    private static Gauge newJobUsageGauge() {
        return newGaugeBuilder()
                .name("job_usage_bytes")
                .labelNames("file_store", "jobName", "url")
                .help("Amount of disk usage (bytes) for each job in Jenkins")
                .create();
    }

    private static Gauge newFileStoreCapacityGauge() {
        return newGaugeBuilder()
                .name("file_store_capacity_bytes")
                .labelNames("file_store")
                .help("Total size in bytes of the file stores used by Jenkins")
                .create();
    }

    private static Gauge newFileStoreAvailableGauge() {
        return newGaugeBuilder()
                .name("file_store_available_bytes")
                .labelNames("file_store")
                .help("Estimated available space on the file stores used by Jenkins")
                .create();
    }

    private static Gauge.Builder newGaugeBuilder() {
        return Gauge.build()
                .namespace(ConfigurationUtils.getNamespace())
                .subsystem(ConfigurationUtils.getSubSystem());
    }

    @Override
    @Nonnull
    public List<MetricFamilySamples> collect() {
        if (!PrometheusConfiguration.get().getCollectDiskUsage()) {
            return Collections.emptyList();
        }

        try {
            return collectDiskUsage();
        } catch (final IOException | RuntimeException e) {
            logger.warn("Failed to get disk usage data due to an unexpected error.", e);
            return Collections.emptyList();
        } catch (final java.lang.NoClassDefFoundError e) {
            logger.warn("Cannot collect disk usage data because plugin CloudBees Disk Usage Simple is not installed: {}", e.toString());
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
        final Set<FileStore> usedFileStores = new HashSet<>();

        final Gauge directoryUsageGauge = newDirectoryUsageGauge();
        diskUsagePlugin.getDirectoriesUsages().forEach(i -> {
                final Optional<FileStore> fileStore = getFileStore(i.getPath());
                fileStore.ifPresent(usedFileStores::add);
                directoryUsageGauge.labels(toLabelValue(fileStore), i.getDisplayName()).set(i.getUsage() * 1024);
        });
        samples.addAll(directoryUsageGauge.collect());

        final Gauge jobUsageGauge = newJobUsageGauge();
        diskUsagePlugin.getJobsUsages().forEach(i -> {
                final Optional<FileStore> fileStore = getFileStore(i.getPath());
                fileStore.ifPresent(usedFileStores::add);
                jobUsageGauge.labels(toLabelValue(fileStore), i.getFullName(), i.getUrl()).set(i.getUsage() * 1024);
        });
        samples.addAll(jobUsageGauge.collect());

        final Gauge fileStoreCapacityGauge = newFileStoreCapacityGauge();
        final Gauge fileStoreAvailableGauge = newFileStoreAvailableGauge();
        usedFileStores.forEach(store -> {
                final String labelValue = toLabelValue(Optional.of(store));
    
                try {
                    fileStoreCapacityGauge.labels(labelValue).set(store.getTotalSpace());
                } catch (final IOException | RuntimeException e) {
                    logger.debug("Failed to get total space of {}", store, e);
                    fileStoreCapacityGauge.labels(labelValue).set(Double.NaN);
                }
    
                try {
                    fileStoreAvailableGauge.labels(labelValue).set(store.getUsableSpace());
                } catch (final IOException | RuntimeException e) {
                    logger.debug("Failed to get usable space of {}", store, e);
                    fileStoreAvailableGauge.labels(labelValue).set(Double.NaN);
                }
        });
        samples.addAll(fileStoreCapacityGauge.collect());
        samples.addAll(fileStoreAvailableGauge.collect());

        return samples;
    }

    private static String toLabelValue(Optional<FileStore> fileStore) {
        // At least on Linux, FileStore::name is not unique, whereas FileStore::toString includes the mount point, which
        // makes it unique. So it's possible to have duplicate metrics with different label values for the same file
        // store mounted to different paths.
        return fileStore.map(FileStore::toString).orElse("<unknown>");
    }

    private static Optional<FileStore> getFileStore(File file) {
        try {
            return Optional.of(Files.getFileStore(file.toPath().toRealPath()));
        } catch (IOException | RuntimeException e) {
            logger.debug("Failed to get file store for {}", file, e);
            return Optional.empty();
        }
    }
}
