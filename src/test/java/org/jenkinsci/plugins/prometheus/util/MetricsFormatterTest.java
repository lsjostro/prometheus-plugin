package org.jenkinsci.plugins.prometheus.util;

import org.junit.Assert;
import org.junit.Test;

public class MetricsFormatterTest {

    private final String inString = "jenkins_node_builds{quantile=\"0.5\",} 0.091670452\n" +
            "jenkins_node_builds{quantile=\"0.75\",} 0.091670452\n" +
            "jenkins_node_builds{quantile=\"0.95\",} 0.091670452\n" +
            "jenkins_node_builds{quantile=\"0.98\",} 0.091670452\n" +
            "jenkins_node_builds{quantile=\"0.99\",} 0.091670452\n" +
            "jenkins_node_builds{quantile=\"0.999\",} 0.091670452\n" +
            "jenkins_node_my_node_1_builds{quantile=\"0.99\",} 0.091670452\n" +
            "jenkins_node_my_node_4_builds{quantile=\"0.999\",} 0.091670452\n" +
            "jenkins_node_builds_count 28458.0\n" +
            "jenkins_node_my_node_1_builds_count 12345";

    @Test
    public void master_node_count_format() {
        String formatString = MetricsFormatter.formatMetrics(inString);
        //master node count
        Assert.assertFalse(formatString.contains("jenkins_node_builds_count 28458.0\n"));
        Assert.assertTrue(formatString.contains("jenkins_node_builds_count{node=\"master\"} 28458.0\n"));
    }

    @Test
    public void master_node_histogram_format() {
        String formatString = MetricsFormatter.formatMetrics(inString);
        //master node histogram
        Assert.assertFalse(formatString.contains("jenkins_node_builds{quantile=\"0.999\",} 0.091670452\n"));
        Assert.assertTrue(formatString.contains("jenkins_node_builds{node=\"master\", quantile=\"0.999\",} 0.091670452\n"));
    }

    @Test
    public void named_node_count_format() {
        String formatString = MetricsFormatter.formatMetrics(inString);
        //named node count
        Assert.assertFalse(formatString.contains("jenkins_node_my_node_1_builds_count"));
        Assert.assertTrue(formatString.contains("jenkins_node_builds_count{node=\"my_node_1\"} 12345"));
    }

    @Test
    public void named_node_histogram_format() {
        String formatString = MetricsFormatter.formatMetrics(inString);
        //named node histogram
        Assert.assertFalse(formatString.contains("jenkins_node_my_node_4_builds{quantile=\"0.999\",} 0.091670452\n"));
        Assert.assertTrue(formatString.contains("jenkins_node_builds{node=\"my_node_4\", quantile=\"0.999\",} 0.091670452\n"));
    }
}
