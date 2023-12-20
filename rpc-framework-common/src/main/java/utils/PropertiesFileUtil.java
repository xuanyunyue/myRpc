package utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author： zyx1128
 * @create： 2023/12/18 15:34
 * @description：TODO
 * 获取配置类的数据
 */
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil() {
    }

    public static Properties readPropertiesFile(String fileName) {
        /*
        file:/D:/0project/myRPC/rpc-framework-common/target/classes/
        也就是java包/resource包下的路径
         */
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if (url != null) {
            /*
            /D:/0project/myRPC/rpc-framework-common/target/classes/     +     fileName
             */
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());
    }
}
