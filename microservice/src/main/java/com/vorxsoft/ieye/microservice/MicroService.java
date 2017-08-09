package com.vorxsoft.ieye.microservice;

import java.util.concurrent.ExecutionException;

public interface MicroService {
  public void init(String endpoint) throws Exception;
  public MicroServiceImpl getInstance();
  public long LeaseGrant(int ttl) throws Exception;
  public int Registe(String name,String host,int port) throws Exception;
  public long Registe(String name,String host,int port,int ttl) throws Exception;
  public int RegisteWithValue(String name,String host,int port,String value) throws ExecutionException, InterruptedException;
  public int UnRegiste(String name,String host,int port) throws Exception;
  public String Resolve(String name);
  public String ResolveWithWatcher(String name);
  public void SetMame(String name);
  public void SetHost(String host);
  public void SetPort(int port);
  public void init();
}
