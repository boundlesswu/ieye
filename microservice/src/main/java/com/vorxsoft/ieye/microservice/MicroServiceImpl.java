package com.vorxsoft.ieye.microservice;

import com.coreos.jetcd.*;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.PutOption;

import java.security.spec.ECField;
import java.util.Random;
import java.util.concurrent.*;

import static com.vorxsoft.ieye.microservice.MicroService.PoliceType.RANDOM;

public class MicroServiceImpl implements  MicroService {
  private final String microServicePath = "/service";
  private final String version = "4.0.0.1";

  private String address_;
  private String name_;
  private String host_;
  private int port_;
  private Client client_;
  private KV kvClient_;
  private Lease leaseClient_;
  private Watch watchClient_;
  private String endpoint_;
  private ScheduledExecutorService executor_;
  private Random random = new Random();

  MicroServiceImpl(){
    name_ = "";
    host_ = "";
    port_ = 0;
    client_ = null;
    kvClient_ =  null;
    leaseClient_ = null;
    watchClient_ = null;
    endpoint_ = "";
    executor_ = Executors.newScheduledThreadPool(3);
  }

  private ScheduledExecutorService getExecutor(){
    return  executor_;
  }

  @Override
  public void init(String endpoint) throws Exception{
    endpoint_ = endpoint;
    if(!endpoint_.isEmpty()){
      try {
        client_ = ClientBuilder.newBuilder().setEndpoints(endpoint).build();
      }catch (Exception e){
        System.out.println(e);
      }
    }else{
      try{
        client_ = ClientBuilder.newBuilder().setEndpoints("http://localhost:2379").build();
      }catch (Exception e){
        System.out.println(e);
      }
    }
    watchClient_ = client_.getWatchClient();
    kvClient_ = client_.getKVClient();
    leaseClient_ = client_.getLeaseClient();
  }

  @Override
  public MicroServiceImpl getInstance() {
    return this;
  }

  private String Convert2key(String name, String host, int port)  {
    return GetServiceNameFullKey(name)+"/"+ host +":"+port;
  }
  private String GetServiceNameFullKey(String name){
    return microServicePath+"/"+ name;
  }
  @Override
  public int Registe(String name, String host, int port) throws Exception{
    String value = String.valueOf(System.currentTimeMillis());
    return Registe(name,host,port,value);
  }

  @Override
  public long LeaseGrant(int ttl) throws Exception {
    long leaseID = leaseClient_.grant(ttl).get().getID();
    return leaseID;
  }
  @Override
  public long Registe(String name, String host, int port, int ttl) throws Exception {
    String value = String.valueOf(System.currentTimeMillis());
    return Registe(name,host,port,ttl,value);
  }

  @Override
  public long Registe(String name, String host, int port, int ttl, String value) throws Exception {
    long leaseID = LeaseGrant(ttl);
    ByteSequence key = ByteSequence.fromString(Convert2key(name, host, port));
    ByteSequence bvalue = ByteSequence.fromString(String.valueOf(value));
    CompletableFuture<PutResponse> feature = kvClient_.put(key, bvalue,
        PutOption.newBuilder().withLeaseId(leaseID).build());
    PutResponse response = feature.get();
    System.out.println("store response is" + response);
    return leaseID;
}
  @Override
  public long RegisteWithHB(String name, String host, int port, int ttl) throws Exception {
    String value = String.valueOf(System.currentTimeMillis());
    return RegisteWithHB(name,host,port,ttl,value);
  }

  @Override
  public long RegisteWithHB(String name, String host, int port, int ttl, String value) throws Exception {
    long leaseID = Registe(name,host,port,ttl,value);
    getExecutor().scheduleAtFixedRate(()->{
      Lease.KeepAliveListener kal = leaseClient_.keepAlive(leaseID);
      try {
        System.out.println("keepAlive response  " + kal.listen());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    },1l,ttl, TimeUnit.SECONDS);
    return leaseID;
  }

  @Override
  public int Registe(String name, String host, int port, String value) throws Exception {
    ByteSequence key = ByteSequence.fromString(Convert2key(name,host,port));
    PutResponse response = kvClient_.put(key,ByteSequence.fromString(value)).get();
    return 0;
  }

  @Override
  public int UnRegiste(String name, String host, int port) throws Exception{
    ByteSequence key = ByteSequence.fromString(Convert2key(name,host,port));
    try{
      CompletableFuture<DeleteResponse> delResp = kvClient_.delete(key);
    }catch (Exception e){
      System.out.println(e);
    }
    return 0;
  }
  @Override
  public String Resolve(String name) throws Exception {
    PoliceType policy = RANDOM;
    return Resolve(name,policy);
  }
  private int getMaxPerformane(String name){
    ByteSequence bkey  = ByteSequence.fromString(GetServiceNameFullKey(name));
    return 0;
  }
  @Override
  public String Resolve(String name, PoliceType policy) throws Exception {
    ByteSequence bkey  = ByteSequence.fromString(GetServiceNameFullKey(name));
    GetOption option = GetOption.newBuilder()
        .withSortField(GetOption.SortTarget.KEY)
        .withSortOrder(GetOption.SortOrder.DESCEND)
        .withPrefix(bkey)
        .build();
    GetResponse response =  kvClient_.get(bkey,option).get();
    if(response.getCount() == 0)
      return null;
    int i = 0;
    switch (policy){
      case ROUND:
        i = 0;
        break;
      case RANDOM:
        i = random.nextInt(response.getKvs().size());
        break;
      case PERFORMANCE:
        i = getMaxPerformane(name);
        break;
      default:
        i = 0;
    }
    String serviceAddress =  response.getKvs().get(i).getKey().toStringUtf8();
    int pos = serviceAddress.lastIndexOf("/");
    if(pos >= 0 ) return serviceAddress.substring(pos);
    return null;
  }

  @Override
  public String SetWatcher(String name) throws Exception {
    PoliceType policy = RANDOM;
    return SetWatcher(name,policy);
  }

  @Override
  public String SetWatcher(String name,PoliceType policy) throws Exception {
    address_ = Resolve(name,policy);
    String key = microServicePath+"/"+ name +"/"+address_;
    Watch.Watcher mywatch  =  watchClient_.watch(ByteSequence.fromString(key));
    getExecutor().scheduleAtFixedRate(()->{
      //WatchResponse wreply  = mywatch.listen();
      System.out.println("watcher response  " + mywatch.listen());
    },1l,4l, TimeUnit.SECONDS);
    return address_;
  }

  @Override
  public void SetMame(String name) {
    name_ =  name;
  }

  @Override
  public void SetHost(String host) {
    host_ =  host;
  }

  @Override
  public void SetPort(int port){
    port_ = port;
  }

  @Override
  public void init() {

  }

}
