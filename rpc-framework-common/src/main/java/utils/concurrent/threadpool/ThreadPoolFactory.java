package utils.concurrent.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.*;

import static jdk.nashorn.internal.objects.NativeMath.log;

/**
 * @author： zyx1128
 * @create： 2023/12/7 13:03
 * @description：线程池的相关工具包： 1. 创建线程池
 * 2. 关闭所有线程池
 * 3. 创建 ThreadFactory
 * 4. 打印线程池的状态
 */
@Slf4j
public class ThreadPoolFactory {

    /**
     * 利用ConcurrentHashMap来管理线程池
     * key: threadNamePrefix，key相同表示处理的业务场景相同
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    public ThreadPoolFactory() {
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix) {
        return createCustomThreadPoolIfAbsent(threadNamePrefix,new CustomThreadPoolConfig());
    }
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(threadNamePrefix,customThreadPoolConfig,false);
    }
    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig, Boolean daemon) {
//        若不存在此key，则添加到map里面
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, s -> createThreadPool(threadNamePrefix, customThreadPoolConfig, daemon));
//        判断存在的线程池是否被关闭
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
//            从map里面移除
            THREAD_POOLS.remove(threadNamePrefix);
//            创建一个新的线程池
            threadPool = createThreadPool(threadNamePrefix, customThreadPoolConfig, daemon);
//            加入到map里
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }

    private static ExecutorService createThreadPool(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
//        创建线程
        return new ThreadPoolExecutor(
                customThreadPoolConfig.getCorePoolSize(),
                customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(),
                customThreadPoolConfig.getUnit(),
                customThreadPoolConfig.getWorkQueue(),
                threadFactory);//使用了自定义的线程工厂
    }

    /**
     * - @description: 自定义线程工厂，主要是定义线程的名字，以及守护线程
     *
     * @param threadNamePrefix
     * @param daemon           - @return java.util.concurrent.ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if (StringUtils.isBlank(threadNamePrefix))
            return Executors.defaultThreadFactory();

        if (ObjectUtils.isEmpty(daemon))
            return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();

        return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
    }

    public static void shutDownAllThreadPool() {
        ThreadPoolFactory.log.info("call shutDownAllThreadPool method");
//        用并行流parallelStream来关闭线程池，并行性能更高
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
//            打印日志，看这个线程池是否完全关闭
            ThreadPoolFactory.log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                /*
                awaitTermination 方法会等待线程到达 TERMINATED 即已终止的状态。
                如果线程池已经关闭，则直接返回 true；如果线程池未关闭，该方法会根据 Timeout 的延时等待线程结束，并根据到期后的线程池状态返回 true 或者 false。
                注意该方法不会关闭线程池，只负责延时以及检测状态。
                 */
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS))
                    log("Thread pool [{}] is not terminated", entry.getKey());
            } catch (InterruptedException e) {
                ThreadPoolFactory.log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
//        定时任务，至少每隔1s执行一次
        /*
        command：执行线程
        initialDelay：初始化延时
        period：两次开始执行最小间隔时间
        unit：计时单位
         */
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
