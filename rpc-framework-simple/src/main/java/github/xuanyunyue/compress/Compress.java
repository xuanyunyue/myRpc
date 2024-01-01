package github.xuanyunyue.compress;

import github.xuanyunyue.extension.SPI;

/**
 * @author： zyx1128
 * @create： 2023/12/15 19:38
 * @description：TODO
 */
@SPI
public interface Compress {

    byte[] compress(byte[] bytes);


    byte[] decompress(byte[] bytes);
}