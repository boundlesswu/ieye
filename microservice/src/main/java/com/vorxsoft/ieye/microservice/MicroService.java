package com.vorxsoft.ieye.microservice;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface MicroService {
  enum PoliceType{
    ROUND, RANDOM,PERFORMANCE;
  }
  public void init(String endpoint) throws Exception;
  public void init(WatchCallerInterface wc) throws Exception;
  public void init(String endpoint,WatchCallerInterface wc) throws Exception;
  public void init()throws Exception;
  public MicroServiceImpl getInstance();
  public long LeaseGrant(int ttl) throws Exception;
  public int Registe(String name,String host,int port) throws Exception;
  public long Registe(String name,String host,int port,int ttl) throws Exception;
  public int Registe(String name, String host, int port, String value) throws Exception;
  public long Registe(String name,String host,int port,int ttl,String value) throws Exception;
  public long RegisteWithHB(String name,String host,int port,int ttl,String value) throws Exception;
  public long RegisteWithHB(String name,String host,int port,int ttl) throws Exception;
  public int UnRegiste(String name,String host,int port) throws Exception;
  public List<com.coreos.jetcd.data.KeyValue> ResolveAll(String name) throws Exception;
  public String Resolve(String name) throws Exception;
  public String Resolve(String name,PoliceType policy) throws Exception;
  public String SetWatcher(String name)throws Exception;;
  public String SetWatcher(String name,PoliceType policy)throws Exception;;
  public void SetMame(String name);
  public void SetHost(String host);
  public void SetPort(int port);
}
