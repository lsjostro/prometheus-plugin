package org.jenkinsci.plugins.prometheus.collectors.jobs;

import hudson.tasks.LogRotator;
import io.prometheus.client.Collector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.when;

public class BuildDiscardGaugeTest extends JobCollectorTest {


    @Test
    public void testCollectResult() {
        when(job.getBuildDiscarder()).thenReturn(null);

        BuildDiscardGauge sut = new BuildDiscardGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);
        validateNames(samples, new String[]{"default_jenkins_builds_discard_active"});
        validateMetricFamilySampleSize(samples, 1);
        validateValue(samples.samples.get(0), 0.0);

    }

    @Test
    public void testBuildDiscarderActive() {
        when(job.getBuildDiscarder()).thenReturn(new LogRotator(1,2,3,4));

        BuildDiscardGauge sut = new BuildDiscardGauge(new String[]{"jenkins_job", "repo"}, "default", "jenkins");

        sut.calculateMetric(job, new String[]{"job1", "NA"});
        List<Collector.MetricFamilySamples> collect = sut.collect();

        validateMetricFamilySampleListSize(collect, 1);

        Collector.MetricFamilySamples samples = collect.get(0);
        validateNames(samples, new String[]{"default_jenkins_builds_discard_active"});
        validateMetricFamilySampleSize(samples, 1);
        validateValue(samples.samples.get(0), 1.0);
    }
}