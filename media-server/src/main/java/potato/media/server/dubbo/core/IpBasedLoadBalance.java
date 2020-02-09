package potato.media.server.dubbo.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Constants;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance;
import org.apache.dubbo.rpc.support.MockInvoker;

import java.util.*;

/**
 * @author zh_zhou
 * created at 2019/04/30 16:14
 * Copyright [2019] [zh_zhou]
 */
@SuppressWarnings("all")
public class IpBasedLoadBalance<P extends  IpCommand> implements LoadBalance {

    RandomLoadBalance randomLoadBalance = new RandomLoadBalance();

    public IpBasedLoadBalance() {
        System.out.println(1);
    }

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        try {
            Object[] arguments = invocation.getArguments();
            String name = invocation.getMethodName();
            if (arguments == null || arguments.length != 1) {
                return randomLoadBalance.select(invokers, url, invocation);
            }
            Object argument = arguments[0];
            if ((argument instanceof IpCommand)|| (argument!=null&&IpCommand.class.isAssignableFrom(argument.getClass()))) {
                IpCommand command=(IpCommand) argument;
                Invoker<T> invoker = doselect(invokers, command.getHostIp());
                return invoker;
            } else  {
                return randomLoadBalance.select(invokers, url, invocation);
            }
        } catch (RpcException e) {
            throw e;
        } finally {
        }
    }


    Map<String, Invoker<?>> tcpInvokerMap = new HashMap<>();


    private <T> Invoker<T> doselect(List<Invoker<T>> invokers, String host ) {
        Invoker<T> invoker = (Invoker<T>) tcpInvokerMap.get(host);
        if (invokers.contains(invoker)) {
            return invoker;
        }
        Set<String> hosts=new HashSet<>();
        for (Invoker<T> tmpInvoker : invokers) {
            String invokerHost = tmpInvoker.getUrl().getHost();
            hosts.add(invokerHost);
            if (StringUtils.equalsIgnoreCase(host, invokerHost)) {
                tcpInvokerMap.put(host, tmpInvoker);
                return tmpInvoker;
            }
        }
        throw new RpcException("no instance of ip:"+host+" exist");
    }
}
