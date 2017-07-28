package com.vorxsoft.ieye.eventservice;

import com.vorxsoft.ieye.proto.VSEventRequest;
import com.vorxsoft.ieye.proto.VSEventResponse;
import com.vorxsoft.ieye.proto.VSEventServiceGrpc;

public class SimpleEventServer extends VSEventServiceGrpc.VSEventServiceImplBase{
    private get
    Override
    public void sentEvent(VSEventRequest request,io.grpc.stub.StreamObserver<com.vorxsoft.ieye.proto.VSEventResponse> response){
        String deviceNo = request.getDeviceNo();
        String resourceUid = request.getResourceUid();
        System.out.print("evenType :"+ request.getEvenType()+"deviceNo:"+ deviceNo);
        System.out.println("resourceUid:"+ resourceUid+"happenTime:"+ request.getHappenTime());

        VSEventResponse reply = VSEventResponse.newBuilder().setDeviceNo(deviceNo).
                                                setResourceUid(resourceUid).setResult(true).build();
        response.onNext(reply);
        response.onCompleted();

    }
}
