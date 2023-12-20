package factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author： zyx1128
 * @create： 2023/12/19 16:47
 * @description：TODO 获取单例对象的工厂类
 */
public class SingletonFactory {
    //
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        if (OBJECT_MAP.containsKey(key)) {
            //将OBJECT_MAP.get(key)对象强制转换为传入的c类型
            return c.cast(OBJECT_MAP.get(key));
        } else {
            //computeIfAbsent() 方法是用于在 map 中不存在指定键的情况下，执行一个指定的函数，并将其结果放入 map 中。如果键已存在，则不执行计算操作，直接返回现有的值。
            return c.cast(OBJECT_MAP.computeIfAbsent(key, k -> {
                try {
                    //是 Java 中一种通过反射机制创建对象的方式。
                    //getDeclaredConstructor()：用于获取类中声明的构造函数。
                    //newInstance()：通过构造函数创建类的实例。
                    return c.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }));
        }
    }
}
