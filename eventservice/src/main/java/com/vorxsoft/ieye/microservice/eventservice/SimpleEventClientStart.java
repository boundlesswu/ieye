package com.vorxsoft.ieye.microservice.eventservice;

import com.vorxsoft.ieye.proto.VSEventRequest;
import com.vorxsoft.ieye.proto.VSEventResponse;
import com.vorxsoft.ieye.proto.VSEventServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.vorxsoft.ieye.proto.VSEventType.VSEventTypeDigitalIOCommon;

/**
 * Created by Administrator on 2017/8/4 0004.
 */
public class SimpleEventClientStart {
    private ManagedChannel managedChannel;
    private int PORT = 9999;

    private void createChannel(){
        managedChannel = NettyChannelBuilder.forAddress("localhost",PORT).usePlaintext(true).build();
    }

    private void shutdown(){
        if(managedChannel!=null){
            try {
                managedChannel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getDateTimeNs(){
        String msg="";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss.SSS");
        msg += sdf.format(date);
        System.out.println(msg);
        return msg;
    }


    public static String randomString() {
        return java.util.UUID.randomUUID().toString();
    }

    public static void main(String[] args) throws  Exception{
        SimpleEventClientStart simpleEventClient = new SimpleEventClientStart();
        simpleEventClient.createChannel();

        VSEventServiceGrpc.VSEventServiceBlockingStub stub = VSEventServiceGrpc.newBlockingStub(simpleEventClient.managedChannel);


        do{
            VSEventResponse reply =  stub.sentEvent(VSEventRequest.newBuilder().
                                                                   setDeviceNo(randomString()).
                                                                   setEvenType(VSEventTypeDigitalIOCommon).
                                                                   setResourceUid(randomString()).
                                                                   setHappenTime(getDateTimeNs()).build());
            System.out.println("reponse is " + reply);
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }while(true);

    }

}
