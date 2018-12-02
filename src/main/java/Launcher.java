import com.smart.jndi.JndiFactory;

import javax.naming.Context;
import java.util.Hashtable;

public class Launcher {
    public static void main(String[] args) throws Exception {
        System.out.println("Launcher");

        JndiFactory jndiFactory = new JndiFactory();

        Context context = jndiFactory.getJndiContext();

        Hashtable<String, String> env = (Hashtable<String, String>) context.getEnvironment();

        System.out.println("配置信息输出：");
        for (String key : env.keySet()) {
            System.out.println(String.format("key: %s, value: %s", key, env.get(key)));
        }
    }
}
