package org.jenkinsci.plugins.prometheus.util;

import io.prometheus.client.Collector;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JenkinsNodeBuildsSampleBuilderTest {
    @Test
    public void master_node_count_format() {
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds_count",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("master", "0.5"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.builds",
                        "_count",
                        Arrays.asList("quantile"),
                        Arrays.asList("0.5"),
                        0.091670452
                )
        );
    }

    @Test
    public void master_node_histogram_format() {
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("master", "0.999"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.builds",
                        "",
                        Arrays.asList("quantile"),
                        Arrays.asList("0.999"),
                        0.091670452
                )
        );
    }

    @Test
    public void named_node_count_format() {
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds_count",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("evil node_name.com", "0.5"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.evil node_name.com.builds",
                        "_count",
                        Arrays.asList("quantile"),
                        Arrays.asList("0.5"),
                        0.091670452
                )
        );
    }

    @Test
    public void named_node_histogram_format() {
        Assert.assertEquals(
                new Collector.MetricFamilySamples.Sample(
                        "jenkins_node_builds",
                        Arrays.asList("node", "quantile"),
                        Arrays.asList("evil node_name.com", "0.999"),
                        0.091670452
                ),
                new JenkinsNodeBuildsSampleBuilder().createSample(
                        "jenkins.node.evil node_name.com.builds",
                        "",
                        Arrays.asList("quantile"),
                        Arrays.asList("0.999"),
                        0.091670452
                )
        );
    }
}
