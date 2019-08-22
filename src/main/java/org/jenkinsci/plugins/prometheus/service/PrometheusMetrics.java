package org.jenkinsci.plugins.prometheus.service;

public interface PrometheusMetrics {

    String getMetrics();

    void collectMetrics();

}
