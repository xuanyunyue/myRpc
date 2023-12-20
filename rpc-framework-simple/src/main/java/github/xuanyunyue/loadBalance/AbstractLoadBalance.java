package github.xuanyunyue.loadBalance;

import github.xuanyunyue.remoting.dto.RPCRequest;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author： zyx1128
 * @create： 2023/12/18 20:41
 * @description：TODO
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    /**
     * @param serviceAddresses
     * @param rpcRequest
     * @return String
     * 当serviceAddresses的list只有1个or0个的时候，直接返回；否则采用负载均衡。
     */
    @Override
    public String selectServiceAddress(List<String> serviceAddresses, RPCRequest rpcRequest) {
        if (CollectionUtils.isEmpty(serviceAddresses)) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RPCRequest rpcRequest);


}
