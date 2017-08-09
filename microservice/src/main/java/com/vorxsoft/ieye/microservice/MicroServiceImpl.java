package com.vorxsoft.ieye.microservice;

import com.coreos.jetcd.*;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.options.PutOption;

import java.security.spec.ECField;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MicroServiceImpl implements  MicroService {
  private final String microServicePath = "/service";
  private final String version = "4.0.0.1";

  private String name_;
  private String host_;
  private int port_;
  private Client client_;
  private KV kvClient_;
  private Lease leaseClient_;
  private Watch watchClient_;
  private String endpoint_;
  private ScheduledExecutorService executor_;

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
    return microServicePath+"/"+name+"/"+ host +":"+port;
  }

  @Override
  public int Registe(String name, String host, int port) throws Exception{
    String value = String.valueOf(System.currentTimeMillis());
    return RegisteWithValue(name,host,port,value);
  }

  @Override
  public long LeaseGrant(int ttl) throws Exception {
    long leaseID = leaseClient_.grant(ttl).get().getID();
    return leaseID;
  }
  @Override
  public long Registe(String name, String host, int port, int ttl) throws Exception {
    long leaseID = LeaseGrant(ttl);
    ByteSequence key = ByteSequence.fromString(Convert2key(name,host,port));
    ByteSequence value = ByteSequence.fromString(String.valueOf(System.currentTimeMillis()));
    CompletableFuture<PutResponse> feature = kvClient_.put(key,value,
        PutOption.newBuilder().withLeaseId(leaseID).build());
    PutResponse response = feature.get();
    System.out.println("store response is" + response);
    return leaseID;
  }

  @Override
  public int RegisteWithValue(String name, String host, int port, String value) throws ExecutionException, InterruptedException {
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
  public String Resolve(String name) {
    return null;
  }

  @Override
  public String ResolveWithWatcher(String name) {
    return null;
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
