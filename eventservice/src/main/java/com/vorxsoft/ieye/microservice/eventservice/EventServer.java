package com.vorxsoft.ieye.microservice.eventservice;

import com.vorxsoft.ieye.proto.VSEventRequest;
import com.vorxsoft.ieye.proto.VSEventResponse;
import com.vorxsoft.ieye.proto.VSEventServiceGrpc;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class EventServer extends VSEventServiceGrpc.VSEventServiceImplBase{
    @Override
    public void sentEvent(VSEventRequest request,io.grpc.stub.StreamObserver<com.vorxsoft.ieye.proto.VSEventResponse> response){
        System.out.println("receive : " +  request);
        VSEventResponse reply = VSEventResponse.newBuilder().setDeviceNo(request.getDeviceNo()).
                                                             setResourceUid(request.getResourceUid()).
                                                              setResult(true).build();
        response.onNext(reply);
        response.onCompleted();
    }
}
