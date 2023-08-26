package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import hudson.Extension;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class NamedDisabledMetric extends Entry {

    private final String metricName;

    @DataBoundConstructor
    public NamedDisabledMetric(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }

    @Override
    public Descriptor<Entry> getDescriptor() {
        return new DescriptorImpl();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Entry> {
        @Override
        public String getDisplayName() {
            return "Fully qualified Name Entry";
        }

    }

}
