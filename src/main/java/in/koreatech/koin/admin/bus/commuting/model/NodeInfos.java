package in.koreatech.koin.admin.bus.commuting.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeInfos {
    private static final Pattern NODE_INFO_NAME_PATTERN = Pattern.compile("^(.+)\\((.+)\\)$");
    private static final Integer NODE_INFO_NAME_INDEX = 1;
    private static final Integer NODE_INFO_DETAIL_INDEX = 2;

    private final List<NodeInfo> nodeInfos;

    public NodeInfos() {
        this.nodeInfos = new ArrayList<>();
    }

    public void addNodeInfo(String nodeInfoName) {
        Matcher matcher = NODE_INFO_NAME_PATTERN.matcher(nodeInfoName);

        if (matcher.matches()) {
            String name = matcher.group(NODE_INFO_NAME_INDEX).trim();
            String detail = matcher.group(NODE_INFO_DETAIL_INDEX).trim();
            nodeInfos.add(new NodeInfo(name, detail));
        } else {
            nodeInfos.add(new NodeInfo(nodeInfoName, null));
        }
    }

    public List<NodeInfo> getNodeInfos() {
        return List.copyOf(nodeInfos);
    }
}
