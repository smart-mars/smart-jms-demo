package com.smart.jndi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class JndiFactory {
    protected Context context = null;

    public void initialize() throws NamingException
    {
        Properties props = new Properties();
        try{
            props.load(this.getClass().getClassLoader().getResourceAsStream("jndi.properties"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
        context = new InitialContext(props);
    }

    public Context getJndiContext() throws NamingException {
        if(context == null){
            initialize();
        }
        return context;
    }
}
