package org.jenkinsci.plugins.prometheus.collectors;

import io.prometheus.client.Collector;

import java.util.List;

public abstract class BaseMetricCollector<T, I extends Collector> implements MetricCollector<T, I> {

    protected final static String SEPARATOR = "_";

    protected final String[] labelNames;
    protected final String namespace;
    protected final String subsystem;
    protected final String namePrefix;

    protected I collector;

    public BaseMetricCollector(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        this.labelNames = labelNames;
        this.namespace = namespace;
        this.subsystem = subsystem;
        this.namePrefix = namePrefix;
        collector = initCollector();
    }

    public BaseMetricCollector(String[] labelNames, String namespace, String subsystem) {
        this.labelNames = labelNames;
        this.namespace = namespace;
        this.subsystem = subsystem;
        this.namePrefix = "";
        collector = initCollector();
    }

    protected abstract I initCollector();

    @Override
    public List<Collector.MetricFamilySamples> collect() {
        return collector.collect();
    }

    protected String calculateName(String name) {
        if (namePrefix == null || namePrefix.equals("")) {
            return getBaseName() + SEPARATOR + name;
        }
        return getBaseName() + SEPARATOR + namePrefix + SEPARATOR + name;
    }

    protected String getBaseName() {
        return "builds";
    }

}
