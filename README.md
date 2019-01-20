# Jenkins Prometheus Metrics Plugin

[![Join the chat at https://gitter.im/jenkinsci/prometheus-plugin](https://badges.gitter.im/jenkinsci/prometheus-plugin.svg)](https://gitter.im/jenkinsci/prometheus-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## About
Jenkins Prometheus Plugin expose an endpoint (default `/prometheus`) with metrics where a Prometheus Server can scrape.

Documentation can be found [here](https://plugins.jenkins.io/prometheus)

Please note that the documentation is a WIP.

## Metrics exposed
Currently only metrics from the [Metrics-plugin](https://github.com/jenkinsci/metrics-plugin) and summary of build
duration of jobs and pipeline stages

## Environment variables

`PROMETHEUS_NAMESPACE` Prefix of metric (Default: `default`).

`PROMETHEUS_ENDPOINT` REST Endpoint (Default: `prometheus`)

## Building

    mvn clean install
    mvn hpi:hpi

## Author / Maintainer
[Lars Sjöström](https://github.com/lsjostro)

[Marky Jackson](https://github.com/markyjackson-taulia)


