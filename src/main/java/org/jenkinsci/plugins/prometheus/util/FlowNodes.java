package org.jenkinsci.plugins.prometheus.util;

import com.google.common.base.Objects;
import org.jenkinsci.plugins.workflow.actions.StageAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {
    private static final Logger LOG = Logger.getLogger(FlowNodes.class.getName());

    /**
     * Returns true if the given node is a stage node
     */
    public static boolean isStageNode(FlowNode node) {
        return node != null && node.getAction(StageAction.class) != null;
    }

    /**
     * Traverses through all the nodes invoking the given callback
     */
    public static void forEach(FlowExecution execution, Callback<FlowNode> callback) {
        if (execution != null) {
            forEach(execution.getCurrentHeads(), callback);
        }
    }

    /**
     * Traverses through all the nodes invoking the given callback
     */
    public static void forEach(List<FlowNode> nodes, Callback<FlowNode> callback) {
        if (nodes != null) {
            for (FlowNode node : nodes) {
                callback.invoke(node);
                forEach(node.getParents(), callback);
            }
        }
    }

    public static FlowNode getNextStageNode(FlowNode node) {
        List<FlowNode> list = getSortedFlowNodes(node.getExecution());
        if (list.isEmpty()) {
            return null;
        }
        int idx = list.indexOf(node);
        if (idx >= 0) {
            for (int i = idx + 1; i < list.size(); i++) {
                FlowNode flowNode = list.get(i);
                if (isStageNode(flowNode)) {
                    return flowNode;
                }
            }
        }
        // lets return the last node
        return list.get(list.size() - 1);
    }

    public static List<FlowNode> getSortedStageNodes(FlowExecution execution) {
        return getSortedStageNodes(execution.getCurrentHeads());
    }

    public static List<FlowNode> getSortedStageNodes(final List<FlowNode> flowNodes) {
        final List<FlowNode> answer = new ArrayList<FlowNode>();
        forEach(flowNodes, new Callback<FlowNode>() {
            @Override
            public void invoke(FlowNode node) {
                if (isStageNode(node)) {
                    for (FlowNode old : answer) {
                        if (Objects.equal(old.getId(), node.getId())) {
                            // already added
                            return;
                        }
                    }
                    answer.add(node);
                }
            }
        });
        sortInNodeIdOrder(answer);
        return answer;
    }

    public static List<FlowNode> getSortedFlowNodes(FlowExecution execution) {
        return getSortedFlowNodes(execution.getCurrentHeads());
    }

    public static List<FlowNode> getSortedFlowNodes(final List<FlowNode> flowNodes) {
        final List<FlowNode> answer = new ArrayList<FlowNode>();
        forEach(flowNodes, new Callback<FlowNode>() {
            @Override
            public void invoke(FlowNode node) {
                for (FlowNode old : answer) {
                    if (Objects.equal(old.getId(), node.getId())) {
                        // already added
                        return;
                    }
                }
                answer.add(node);
            }
        });
        sortInNodeIdOrder(answer);
        return answer;
    }

    public static void sortInNodeIdOrder(List<FlowNode> answer) {
        // lets sort by node id
        Comparator<? super FlowNode> comparator = new Comparator<FlowNode>() {
            @Override
            public int compare(FlowNode o1, FlowNode o2) {
                return getNodeIdNumber(o1) - getNodeIdNumber(o2);
            }
        };
        Collections.sort(answer, comparator);
    }

    public static int getNodeIdNumber(FlowNode node) {
        String id = node.getId();
        if (id != null && id.length() > 0) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                LOG.warning("Failed to parse FlowNode id " + id + ". " + e);
            }
        }
        return 0;
    }

    public static long getStageDuration(FlowNode node) {
        FlowNode nextStageNode = getNextStageNode(node);
        return getDuration(node, nextStageNode);
    }

    public static long getDuration(FlowNode startNode, FlowNode endNode) {
        long startTime = TimingAction.getStartTime(startNode);
        if (endNode != null) {
            long endTime = TimingAction.getStartTime(endNode);
            return endTime - startTime;
        } else {
            return 0;
        }
    }

}