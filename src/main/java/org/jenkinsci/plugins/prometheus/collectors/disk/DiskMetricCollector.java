package org.jenkinsci.plugins.prometheus.collectors.disk;

import io.prometheus.client.Collector;
import org.jenkinsci.plugins.prometheus.collectors.BaseMetricCollector;

public abstract class DiskMetricCollector<T, I extends Collector>  extends BaseMetricCollector<T,I > {

    public DiskMetricCollector(String[] labelNames, String namespace, String subsystem, String namePrefix) {
        super(labelNames, namespace, subsystem, namePrefix);
    }

    public DiskMetricCollector(String[] labelNames, String namespace, String subsystem) {
        super(labelNames, namespace, subsystem);
    }

    @Override
    protected String getBaseName() {
        return "";
    }
}
