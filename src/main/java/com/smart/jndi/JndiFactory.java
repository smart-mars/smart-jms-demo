package com.smart.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class JndiFactory {
    private Context context = null;

    private void initialize() throws NamingException {
        Properties props = new Properties();
        try {
            // 默认加载 classes 目录配置文件
            props.load(this.getClass().getClassLoader().getResourceAsStream("jndi.properties"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // InitialContext 对象初始化事，必现传入加载的 jndi.properties 配置
        // 网上搜索的资料，大多是不用传入 Properties 对象的。但是尝试过，不能初始化
        // 查看 InitialContext 源码，发现如果不传入 Properties 对象
        // 则默认创建长度为 7 的 HashTable 对象，里面没有 jndi 配置信息，也就是说获取不到 jndi 服务
        context = new InitialContext(props);
    }

    public Context getJndiContext() throws NamingException {
        if (context == null) {
            initialize();
        }
        return context;
    }
}
