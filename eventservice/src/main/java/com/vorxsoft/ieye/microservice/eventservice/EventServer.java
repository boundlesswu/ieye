package com.vorxsoft.ieye.microservice.eventservice;

import com.vorxsoft.ieye.proto.VSEventRequest;
import com.vorxsoft.ieye.proto.VSEventResponse;
import com.vorxsoft.ieye.proto.VSEventServiceGrpc;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class EventServer extends VSEventServiceGrpc.VSEventServiceImplBase{

  private Jedis jedis ;
  private long count=0;

  EventServer(String ip,int port)  {
    jedis  = new  Jedis(ip, port);
  }
  public Jedis getJedis(){
    return jedis;
  }
  public Map<String, String> Event2map(VSEventRequest request){
    Map<String, String> map = new HashMap<String, String>();
    map.put("evenType",request.getEvenType().name());
    map.put("deviceNo", request.getDeviceNo());
    map.put("ResourceUid",request.getResourceUid());
    map.put("happenTime",request.getHappenTime());
    return map;
  }
    @Override
    public void sentEvent(VSEventRequest request,io.grpc.stub.StreamObserver<com.vorxsoft.ieye.proto.VSEventResponse> response){
        System.out.println("receive : " +  request);
        count++;
      Map<String, String> map = Event2map(request);
      String key = "alarm_" + count;
      jedis.hmset(key,map);
        VSEventResponse reply = VSEventResponse.newBuilder().setDeviceNo(request.getDeviceNo()).
                                                             setResourceUid(request.getResourceUid()).
                                                              setResult(true).build();
        response.onNext(reply);
        response.onCompleted();
    }
}
