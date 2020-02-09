package potato.media.server.dubbo.core;

import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Cluster;
import org.apache.dubbo.rpc.cluster.Directory;

/**
 * @author zh_zhou
 * created at 2019/04/30 17:23
 * Copyright [2019] [zh_zhou]
 */
public class ImFailoverCluster   implements Cluster {

    public final static String NAME = "imfailover";

    @Override
    public <T> Invoker<T> join(Directory<T> directory) throws RpcException {
        return new ImFailoverClusterInvoker<T>(directory);
    }

}