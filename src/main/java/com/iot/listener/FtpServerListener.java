package com.iot.listener;


import com.iot.dbUtil.PropsUtil;
import org.apache.log4j.Logger;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by surfacepc on 2016/3/11.
 */
public class FtpServerListener implements ServletContextListener {

    private static Logger logger =Logger.getLogger(FtpServerListener.class);

    public void contextInitialized(ServletContextEvent servletContextEvent) {

        long init = 10;
        long interver = 10;

        PropsUtil propsUtil = new PropsUtil("config.properties");

        try {
            int listenerSwitch  = Integer.parseInt(propsUtil.get("listener"));

            if (listenerSwitch==0){
                System.out.println("Listener is coming~~");

                ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

                logger.debug("Listener Is coming~!");
                scheduledExecutorService.scheduleWithFixedDelay( new DataBaseSynchronize(), init, interver, TimeUnit.SECONDS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {



    }





}
