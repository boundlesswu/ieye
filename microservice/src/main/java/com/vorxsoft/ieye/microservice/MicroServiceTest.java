package com.vorxsoft.ieye.microservice;

import com.coreos.jetcd.Watch;
import com.coreos.jetcd.data.KeyValue;

import java.util.List;

public class MicroServiceTest implements WatchCallerInterface{
  @Override
  public void WatchCaller(Watch.Watcher watch){
    System.out.println("watcher response  " + watch.listen());
  }
  public static void main(String args[]) throws Exception {
    MicroService myservice = new MicroServiceImpl();
    myservice.init("http://192.168.20.251:2379",new MicroServiceTest());

    String name = "vag";
    myservice.RegisteWithHB(name,"192.168.1.1",12345,10);
    myservice.SetWatcher(name);

    String name2 = "vag2";
    myservice.RegisteWithHB(name2,"192.168.1.1",88888,10);
    myservice.SetWatcher(name2);

    String name3 = "vag3";
    myservice.RegisteWithHB(name3,"192.168.1.1",99999,10);
    myservice.SetWatcher(name3);

    List<KeyValue> a = myservice.ResolveAll("vag");
    for(int i = 0;i<a.size();i++){
      System.out.println( "key:"+a.get(i).getKey().toStringUtf8()+"value: "+a.get(i).getValue().toStringUtf8());
   }

  }

}
