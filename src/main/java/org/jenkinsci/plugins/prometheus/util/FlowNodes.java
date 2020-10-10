package org.jenkinsci.plugins.prometheus.util;

import com.cloudbees.workflow.rest.external.ChunkVisitor;
import com.cloudbees.workflow.rest.external.StageNodeExt;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graphanalysis.ForkScanner;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.pipelinegraphanalysis.StageChunkFinder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {

    public static List<StageNodeExt> getSortedStageNodes(WorkflowRun run) {
        ChunkVisitor visitor = new ChunkVisitor(run);
        FlowExecution execution = run.getExecution();
        List<StageNodeExt> answer = new ArrayList<>();
        if (execution == null) {
            return answer;
        }
        ForkScanner.visitSimpleChunks(execution.getCurrentHeads(), visitor, new StageChunkFinder());
        answer.addAll(visitor.getStages());
        sortInNodeIdOrder(answer);
        return answer;
    }

    public static void sortInNodeIdOrder(List<StageNodeExt> answer) {
        answer.sort(Comparator.comparing(StageNodeExt::getId));
    }
}
