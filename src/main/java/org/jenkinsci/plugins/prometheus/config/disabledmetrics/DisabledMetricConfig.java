package org.jenkinsci.plugins.prometheus.config.disabledmetrics;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisabledMetricConfig extends AbstractDescribableImpl<DisabledMetricConfig> {

    private final List<Entry> entries;

    @DataBoundConstructor
    public DisabledMetricConfig(List<Entry> entries) {
        this.entries = entries != null ? new ArrayList<>(entries) : Collections.emptyList();
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DisabledMetricConfig> {
    }


}
