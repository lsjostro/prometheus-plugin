package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Run;
import hudson.util.RunList;
import io.prometheus.client.Collector;
import io.prometheus.client.Summary;
import org.jenkinsci.plugins.prometheus.util.Callback;
import org.jenkinsci.plugins.prometheus.util.FlowNodes;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class JobCollector extends Collector {
    private static final Logger LOG = Logger.getLogger(JobCollector.class.getName());
    private static final String DEFAULT_NAMESPACE = "default";

    private String fullname = "builds";
    private String subsystem = "jenkins";
    private String namespace;
    private Summary summary;
    private Summary stageSummary;

    public JobCollector() {
        namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (namespace == null || namespace.length() == 0) {
            namespace = DEFAULT_NAMESPACE;
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        final List<MetricFamilySamples> samples = new ArrayList<MetricFamilySamples>();
        final List<Job> jobs = new ArrayList<Job>();

        String[] labelNameArray = {"job"};
        String[] labelStageNameArray = {"job", "stage"};
        summary = Summary.build().
                name(fullname + "_duration_milliseconds_summary").
                subsystem(subsystem).namespace(namespace).
                labelNames(labelNameArray).
                help("Summary of Jenkins build times in milliseconds by Job").
                create();
        stageSummary = Summary.build().name(fullname + "_stage_duration_milliseconds_summary").
                subsystem(subsystem).namespace(namespace).
                labelNames(labelStageNameArray).
                help("Summary of Jenkins build times by Job and Stage").
                create();

        Jobs.forEachJob(new Callback<Job>() {
            @Override
            public void invoke(Job job) {
                for (Job old : jobs) {
                    if (old.getFullName().equals(job.getFullName())) {
                        // already added
                        return;
                    }
                }
                jobs.add(job);
                appendJobMetrics(samples, job);
            }
        });
        if (summary.collect().get(0).samples.size() > 0)
            samples.addAll(summary.collect());
        if (stageSummary.collect().get(0).samples.size() > 0)
            samples.addAll(stageSummary.collect());
        return samples;
    }

    protected void appendJobMetrics(List<MetricFamilySamples> mfsList, Job job) {
        String[] labelValueArray = {job.getFullName()};
        RunList<Run> builds = job.getBuilds();
        if (builds != null) {
            for (Run build : builds) {
                if (Runs.includeBuildInMetrics(build)) {
                    long buildDuration = build.getDuration();
                    summary.labels(labelValueArray).observe(buildDuration);

                    if (build instanceof WorkflowRun) {
                        WorkflowRun workflowRun = (WorkflowRun) build;
                        if (workflowRun.getExecution() == null) {
                            continue;
                        }
                        List<FlowNode> stages = getSortedStageNodes(workflowRun.getExecution());
                        for (FlowNode stage : stages) {
                            observeStage(job, build, stage);
                        }
                    }
                }
            }
        }
    }

    private void observeStage(Job job, Run build, FlowNode stage) {
        String jobName = job.getFullName();
        String stageName = stage.getDisplayName();
        String[] labelValueArray = {jobName, stageName};

        long duration = FlowNodes.getStageDuration(stage);
        LOG.warning(stageName);
        stageSummary.labels(labelValueArray).observe(duration);
    }
}