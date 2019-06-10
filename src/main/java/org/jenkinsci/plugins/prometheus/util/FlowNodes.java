package org.jenkinsci.plugins.prometheus.util;

import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.ThreadNameAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

/**
 * Helper methods for working with flow nodes
 */
public class FlowNodes {

    private static final Logger logger = LoggerFactory.getLogger(FlowNodes.class);
    private static FlowNode endNode;

    /**
     * Returns true if the given node is a stage node
     */
    public static boolean isStageNode(FlowNode node) {
        return node != null && (node.getAction(LabelAction.class) != null && node.getAction(ThreadNameAction.class) == null);
    }

    /**
     * Recursively traverses through all nodes and serializes the stage nodes
     */
    public static List<FlowNode> traverseTree(List<FlowNode> nodes, TreeMap<Integer, Boolean> detector) {
        final List<FlowNode> answer = new ArrayList<>();
        if (nodes != null) {
            for (FlowNode node : nodes) {
                int id = Integer.parseInt(node.getId());
                if (detector.get(id) != null) {
                    // already added
                    return answer;
                }

                detector.put(id, true);
                if (isStageNode(node)) {
                    answer.add(node);
                }

                answer.addAll(traverseTree(node.getParents(), detector));
            }
        }
        return answer;
    }

    public static FlowNode getNextStageNode(FlowNode node) {
        List<FlowNode> list = getSortedStageNodes(node.getExecution());
        if (list.isEmpty()) {
            return null;
        }
        int idx = list.indexOf(node) + 1;
        if (idx < list.size()) {
            return list.get(idx);
        }
        // lets return the last node
        return endNode;
    }

    public static List<FlowNode> getSortedStageNodes(FlowExecution execution) {
        return getSortedStageNodes(execution.getCurrentHeads());
    }

    public static List<FlowNode> getSortedStageNodes(final List<FlowNode> flowNodes) {
        final List<FlowNode> answer = traverseTree(flowNodes, new TreeMap<>());
        sortInNodeIdOrder(answer);
        getEndNode(flowNodes);
        return answer;
    }

    private static void getEndNode(final List<FlowNode> flowNodes) {
        for (FlowNode node : flowNodes) {
            if (endNode == null || Integer.parseInt(endNode.getId()) < Integer.parseInt(node.getId())) {
                endNode = node;
            }
        }
    }

    public static void sortInNodeIdOrder(List<FlowNode> answer) {
        answer.sort(Comparator.comparingInt(FlowNodes::getNodeIdNumber));
    }

    public static int getNodeIdNumber(FlowNode node) {
        String id = node.getId();
        if (id != null && id.length() > 0) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse FlowNode id [{}].", id);
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
        }
        return 0;
    }
}
