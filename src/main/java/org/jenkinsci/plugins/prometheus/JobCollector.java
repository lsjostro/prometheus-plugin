package org.jenkinsci.plugins.prometheus;

import hudson.model.Job;
import hudson.model.Run;
import io.prometheus.client.Collector;
import io.prometheus.client.Summary;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.prometheus.util.Callback;
import org.jenkinsci.plugins.prometheus.util.FlowNodes;
import org.jenkinsci.plugins.prometheus.util.Jobs;
import org.jenkinsci.plugins.prometheus.util.Runs;

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.jenkinsci.plugins.prometheus.util.FlowNodes.getSortedStageNodes;

public class JobCollector extends Collector {
    private static final String DEFAULT_NAMESPACE = "default";

    private String namespace;
    private Summary summary;
    private Summary stageSummary;

    public JobCollector() {
        namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (StringUtils.isEmpty(namespace)) {
            namespace = DEFAULT_NAMESPACE;
        }
    }

    @Override
    public List<MetricFamilySamples> collect() {
        final List<MetricFamilySamples> samples = new ArrayList<>();
        final List<Job> jobs = new ArrayList<>();
        final String fullname = "builds";
        final String subsystem = "jenkins";
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
                appendJobMetrics(job);
            }
        });
        if (summary.collect().get(0).samples.size() > 0)
            samples.addAll(summary.collect());
        if (stageSummary.collect().get(0).samples.size() > 0)
            samples.addAll(stageSummary.collect());
        return samples;
    }

    protected void appendJobMetrics(Job job) {
        String[] labelValueArray = {job.getFullName()};
        Run run = job.getLastBuild();
        while (run != null) {
            if (Runs.includeBuildInMetrics(run)) {
                long buildDuration = run.getDuration();
                summary.labels(labelValueArray).observe(buildDuration);

                if (run instanceof WorkflowRun) {
                    WorkflowRun workflowRun = (WorkflowRun) run;
                    if (workflowRun.getExecution() == null) {
                        run = run.getPreviousBuild();
                        continue;
                    }
                    try {
                        List<FlowNode> stages = getSortedStageNodes(workflowRun.getExecution());
                        for (FlowNode stage : stages) {
                            observeStage(job, run, stage);
                        }
                    } catch (final NullPointerException e){}
                }
            }
            run = run.getPreviousBuild();
        }
    }
    private void observeStage(Job job, Run run, FlowNode stage) {
        String jobName = job.getFullName();
        String stageName = stage.getDisplayName();
        String[] labelValueArray = {jobName, stageName};

        long duration = FlowNodes.getStageDuration(stage);
        stageSummary.labels(labelValueArray).observe(duration);
    }
}