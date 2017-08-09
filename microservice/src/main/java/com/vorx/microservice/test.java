package com.vorx.microservice;

//import com.coreos.jetcd.*;

import com.coreos.jetcd.*;
import com.coreos.jetcd.Watch.Watcher;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.lease.LeaseKeepAliveResponse;
import com.coreos.jetcd.lease.LeaseRevokeResponse;
import com.coreos.jetcd.lease.LeaseTimeToLiveResponse;
import com.coreos.jetcd.options.DeleteOption;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.LeaseOption;
import com.coreos.jetcd.options.PutOption;

import java.util.List;
import java.util.concurrent.*;

public class test {
    private KV kvClient;
    private Client client;
    private Lease leaseClient;
    private Watch watchClient;
    private String endpoint;
    private ScheduledExecutorService executor;
    public ScheduledExecutorService getExecutor(){
        return executor;
    }

    test(){
        client = null;
        kvClient = null;
        leaseClient =  null;
        watchClient =  null;
        endpoint = "http://192.168.20.251:2379";
        executor = Executors.newScheduledThreadPool(3);
    }
    public void setUp() {
        if(!endpoint.isEmpty()){
            client = ClientBuilder.newBuilder().setEndpoints(endpoint).build();
        }else{
            client = ClientBuilder.newBuilder().setEndpoints("http://localhost:2379").build();
        }
        watchClient = client.getWatchClient();
        kvClient = client.getKVClient();
        leaseClient = client.getLeaseClient();
    }

    public void store(String key,String value) throws Exception{
        CompletableFuture<PutResponse> feature = kvClient.put(ByteSequence.fromString(key), ByteSequence.fromString(value));
        PutResponse response = feature.get();
        System.out.println("store response is" + response);
    }

    public long store(String key,String value,int ttl) throws Exception{
        long leaseID = leaseGrant(ttl);
        CompletableFuture<PutResponse> feature = kvClient.put(ByteSequence.fromString(key),
                                                              ByteSequence.fromString(value),
                                              PutOption.newBuilder().withLeaseId(leaseID).build());
        PutResponse response = feature.get();
        System.out.println("store response is" + response);
        return leaseID;
    }
    public KeyValue getKeyValue(String key) throws Exception{
        CompletableFuture<GetResponse> getFeature = kvClient.get(ByteSequence.fromString(key));
        GetResponse response = getFeature.get();
        return response.getKvs().get(0);
    }
    public String get(String key) throws Exception{
        CompletableFuture<GetResponse> getFeature = kvClient.get(ByteSequence.fromString(key));
        GetResponse response = getFeature.get();
        return response.getKvs().get(0).getValue().toString();
    }

    public List<KeyValue> getSortedPrefix(String key) throws Exception{
        GetOption option = GetOption.newBuilder()
                .withSortField(GetOption.SortTarget.KEY)
                .withSortOrder(GetOption.SortOrder.DESCEND)
                .withPrefix(ByteSequence.fromString(key))
                .build();
        GetResponse response =  kvClient.get(ByteSequence.fromString(key), option).get();
        return response.getKvs();
    }

    public void delete(String key) throws Exception {
        DeleteResponse delResp = kvClient.delete(ByteSequence.fromString(key)).get();
        System.out.println("delete  response is" + delResp);;
    }
    public void deleteWithPrefix(String key) throws Exception {
        DeleteOption deleteOpt = DeleteOption.newBuilder()
                .withPrefix(ByteSequence.fromString(key)).build();
        DeleteResponse delResp = kvClient.delete(ByteSequence.fromString(key),deleteOpt).get();
        System.out.println("delete  response is" + delResp);;
    }

    public void tearDown() {
        kvClient.close();
        leaseClient.close();
        watchClient.close();
        this.client.close();
        executor.shutdown();
    }

    public long leaseGrant(int ttl) throws Exception {
        long leaseID = leaseClient.grant(ttl).get().getID();
        return leaseID;
    }
    public void leaseRevoke(long leaseId) throws Exception {
        LeaseRevokeResponse reply = leaseClient.revoke(leaseId).get();
    }

    public void leaseKeepAliveOnce(long leaseId) throws ExecutionException, InterruptedException  {
        LeaseKeepAliveResponse rp = leaseClient.keepAliveOnce(leaseId).get();
        System.out.println("LeaseKeepAliveResponse is "+rp);
    }
    public void leaseKeepAlive(long leaseId) throws ExecutionException, InterruptedException  {
        int ttl = getGrantedTTL(leaseId);
        System.out.println("ttl is "+ttl);
        getExecutor().scheduleAtFixedRate(()->{
            Lease.KeepAliveListener kal = leaseClient.keepAlive(leaseId);
            try {
                System.out.println("keepAlive response  " + kal.listen());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },1l,ttl, TimeUnit.SECONDS);

    }



    public int getLeaseTTl(long leaseId)throws ExecutionException, InterruptedException {
        LeaseTimeToLiveResponse resp = leaseClient.timeToLive(leaseId, LeaseOption.DEFAULT).get();
        return (int) resp.getTTl();
    }

    public int getGrantedTTL(long leaseId)throws ExecutionException, InterruptedException {
        LeaseTimeToLiveResponse resp = leaseClient.timeToLive(leaseId, LeaseOption.DEFAULT).get();
        return (int)resp.getGrantedTTL();
    }

    public void setWatch(String key) throws Exception{
        Watcher mywatch  =  watchClient.watch(ByteSequence.fromString(key));
        getExecutor().scheduleAtFixedRate(()->{
            //WatchResponse wreply  = mywatch.listen();
            System.out.println("watcher response  " + mywatch.listen());
        },1l,4l, TimeUnit.SECONDS);
    }

    public static void main(String[] args) throws Exception {
        test mytest = new test();
        mytest.setUp();
        String key =  "/mytest";
        int ttl=10;
        long leaseId = mytest.store(key, "mytest123",ttl);

        mytest.leaseKeepAlive(leaseId);
        mytest.setWatch(key);

//        mytest.getExecutor().scheduleAtFixedRate(()->{
//            try {
//                mytest.leaseKeepAliveOnce(leaseid);
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            try {
//                System.out.println("getLeaseTTl:" + mytest.getLeaseTTl(leaseid));
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            try {
//                System.out.println("getGrantedTTL:" + mytest.getGrantedTTL(leaseid));
//            } catch (ExecutionException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        },1l,ttl-1, TimeUnit.SECONDS);
//        while(true){
//
//        }
        //mytest.tearDown();
    }
}
