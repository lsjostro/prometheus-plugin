package org.jenkinsci.plugins.prometheus.util;

public class MetricsFormatter {

    public static String formatMetrics(String formatString) {
        //node specific build counts
        formatString = formatString.replaceAll("jenkins_node_builds_count (.*)", "jenkins_node_builds_count{node=\"master\"} $1");
        formatString = formatString.replaceAll("jenkins_node_(.*)_builds_count (.*)", "jenkins_node_builds_count{node=\"$1\"} $2");

        //node specific histograms
        formatString = formatString.replaceAll("jenkins_node_builds\\{(.*)} (.*)", "jenkins_node_builds{node=\"master\", $1} $2");
        formatString = formatString.replaceAll("jenkins_node_(.*)_builds\\{(.*)} (.*)", "jenkins_node_builds{node=\"$1\", $2} $3");

        return formatString;
    }
}
