package org.jenkinsci.plugins.prometheus.util;

public interface Callback<T> {
    void invoke(T value);
}