package github.xuanyunyue.loadBalance.loadBlanceImpl;

import github.xuanyunyue.loadBalance.AbstractLoadBalance;
import github.xuanyunyue.remoting.dto.RPCRequest;

import java.util.List;
import java.util.Random;

/**
 * @author： zyx1128
 * @create： 2023/12/18 20:45
 * @description：TODO
 */
public class randomLoadbalance extends AbstractLoadBalance {
    /**
     * @param serviceAddresses
     * @param rpcRequest
     * @return
     */
    @Override
    protected String doSelect(List<String> serviceAddresses, RPCRequest rpcRequest) {
        return serviceAddresses.get(new Random().nextInt(serviceAddresses.size()));
    }
}
