package github.xuanyunyue.loadBalance;

import github.xuanyunyue.extension.SPI;
import github.xuanyunyue.remoting.dto.RPCRequest;

import java.util.List;

/**
 * @author： zyx1128
 * @create： 2023/12/17 16:28
 * @description：TODO
 */
@SPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList, RPCRequest rpcRequest);
}
