package potato.media.server.dubbo.core;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.directory.AbstractDirectory;
import org.apache.dubbo.rpc.cluster.support.FailoverClusterInvoker;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_LOADBALANCE;
import static org.apache.dubbo.common.constants.CommonConstants.LOADBALANCE_KEY;

/**
 * @author zh_zhou
 * created at 2019/04/30 17:24
 * Copyright [2019] [zh_zhou]
 */
@SuppressWarnings("all")
public class ImFailoverClusterInvoker<T> extends FailoverClusterInvoker<T> {
    public static URL curUrl = null;

    public ImFailoverClusterInvoker(Directory<T> directory) {
        super(directory);
        AbstractDirectory<T> abstractDirectory = (AbstractDirectory<T>) directory;
        URL url = abstractDirectory.getUrl();
        curUrl = url;
    }


    @Override
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation, List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {
        if (invokers == null || invokers.size() == 0) {
            throw new RpcException("no instance of server exists");
        }
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);
        return invoker;
    }

    @Override
    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (invokers == null || invokers.size() == 0) {
//            Cat.logEvent("service.not.exist", getInterface().getName() + "." + invocation.getMethodName());
        }
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
//        Transaction t = Cat.newTransaction("ImInvoker." + invocation.getMethodName(), JavaImUtils.getCatName());
        try {
            Result result = super.invoke(invocation);
//            Cat.logEvent("imResult",JavaImUtils.getCatName(), Message.SUCCESS, JSON.toJSONString(result.getValue()));
//            t.setStatus(Message.SUCCESS);
            return result;
        } catch (RpcException ex) {
//            Cat.logError(ex);
//            t.setStatus(ex);
            throw ex;
        } finally {
//            t.complete();
        }


    }

    @Override
    protected LoadBalance initLoadBalance(List<Invoker<T>> invokers, Invocation invocation) {
        return ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("ip");
    }
}
