package com.vorxsoft.ieye.logservice;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;

import java.util.concurrent.TimeUnit;

public class SimpleLogServerStart {
    private int PORT=8888;
    private Server server;


    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new SimpleLogServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleLogServerStart.this.stop();
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
        final SimpleLogServerStart simpleServerStart = new SimpleLogServerStart();
        SimpleLogServer.getLogger().entry();
        simpleServerStart.start();
        TimeUnit.SECONDS.sleep(3600*24);
        SimpleLogServer.getLogger().exit();
    }

}
