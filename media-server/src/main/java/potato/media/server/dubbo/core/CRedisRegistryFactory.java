package potato.media.server.dubbo.core;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.Registry;
import org.apache.dubbo.registry.support.AbstractRegistryFactory;

/**
 * @author zh_zhou
 * created at 2019/04/30 14:49
 * Copyright [2019] [zh_zhou]
 */
public class CRedisRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected Registry createRegistry(URL url) {
        return new CRedisRegistry("",url);
    }

}