package com.vorxsoft.ieye.microservice;

import com.coreos.jetcd.Watch;

public class MicroServiceTest implements WatchCallerInterface{
  @Override
  public void WatchCaller(Watch.Watcher watch){
    System.out.println("watcher response  " + watch.listen());
  }
  public static void main(String args[]) throws Exception {
    MicroService myservice = new MicroServiceImpl();
    myservice.init("http://192.168.20.251:2379",new MicroServiceTest());

    String name = "vag";
    myservice.Registe("vag","192.168.1.1",12345);
    myservice.SetWatcher(name);

  }

}
