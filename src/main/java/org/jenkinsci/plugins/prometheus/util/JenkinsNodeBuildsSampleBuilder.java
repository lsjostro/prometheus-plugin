package org.jenkinsci.plugins.prometheus.util;

import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that converts jenkins.node[.&lt;node_name&gt;].builds to jenkins.node.builds with a label of node=&lt;node_name&gt; (or master if not set)
 * before creating a Sample
 */
public class JenkinsNodeBuildsSampleBuilder extends DefaultSampleBuilder {
    // Note that nodes can have '.' in their name
    static Pattern PATTERN = Pattern.compile("jenkins\\.node(\\.(?<node>.*))?\\.builds");

    @Override
    public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {
        Matcher matcher = PATTERN.matcher(dropwizardName);

        if (matcher.matches()) {
            String processedDropwizardName = "jenkins.node.builds";
            String node = matcher.group("node");

            if (node == null) {
                node = "master";
            }

            List<String> processedAdditionalLabelNames = new ArrayList<String>();
            List<String> processedAdditionalLabelValues = new ArrayList<String>();

            processedAdditionalLabelNames.add("node");
            processedAdditionalLabelNames.addAll(additionalLabelNames);

            processedAdditionalLabelValues.add(node);
            processedAdditionalLabelValues.addAll(additionalLabelValues);

            return super.createSample(processedDropwizardName, nameSuffix, processedAdditionalLabelNames, processedAdditionalLabelValues, value);
        } else {
            return super.createSample(dropwizardName, nameSuffix, additionalLabelNames, additionalLabelValues, value);
        }
    }
}
