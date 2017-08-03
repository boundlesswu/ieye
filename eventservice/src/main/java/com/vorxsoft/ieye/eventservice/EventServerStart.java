package com.vorxsoft.ieye.eventservice;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class EventServerStart {
    private int PORT=9999;
    private Server server;


    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new EventServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                EventServerStart.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop(){
        try {
            server.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final EventServerStart simpleServerStart = new EventServerStart();
        simpleServerStart.start();
        TimeUnit.DAYS.sleep(365*2000);
    }
}
