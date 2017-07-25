package com.vorxsoft.ieye.logservice;

import com.vorxsoft.ieye.proto.*;

import static com.vorxsoft.ieye.proto.VSLogType.*;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;



public class SimpleLogClientStart {

    public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String numberChar = "0123456789";
    private ManagedChannel managedChannel;
    private int PORT = 8888;
    private Random random = new Random();
    private int count = 0;

    private void createChannel(){
        managedChannel = NettyChannelBuilder.forAddress("localhost",PORT).usePlaintext(true).build();
    }

    private void shutdown(){
        if(managedChannel!=null){
            try {
                managedChannel.shutdown().awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private  String getDateTimeNs(){
        String msg="";
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss.SSS");
        msg += sdf.format(date);
        System.out.println(msg);
        return msg;
    }
    String[] HostNameIpArray={"192.168.1.1","192.168.2.2","cms1","vag1","192.168.78.2","rrjest"};
    String[] UserNameArray = {"admin","liming","wangjing","zhangna"};
    String[] NickNameArray= {"ad","lm","wj","zn"};
    String[] ResourceNameArray = {"ipc1","ixvr1","nvr2","dec3"};
    String[] ServerNameArray = {"cms1","cms2","vag1","ems1"};
    String[] GroupNameArray = {"g1","g2","g3","g4"};

    VSLogLevel[] LogLevelArray = {VSLogLevel.VSLogLevelTrace,VSLogLevel.VSLogLevelDebug,VSLogLevel.VSLogLevelInfo,
                                  VSLogLevel.VSLogLevelWarning,VSLogLevel.VSLogLevelError,VSLogLevel.VSLogLevelFatal};
    //VSLogType[]  LogTypeArray = {0,1,2,3,4,5,6,7,701,702,703,704,705,706,8,801,802,803,804,805,806,807,808,809,810,811,812,813,9,901,902,903,10,1001,1002,1003,11,1101,1102,1103,12};
    VSLogType[]  LogTypeArray = { VSLogTypeEvent,VSLogTypeMonitorSrcEvent,VSLogTypeDigitalIOSrcEvent,VSLogTypeDeviceSrcEvent,
            VSLogTypeServerSrcEvent,VSLogTypeLinkage,VSLogTypeUserLogin,VSLogTypeManage,VSLogTypeManageAdd,VSLogTypeManageMod,
            VSLogTypeManageDel,VSLogTypeManageReboot,VSLogTypeManageStart,VSLogTypeManageStop,VSLogTypeOperator,VSLogTypeOperatorLiveVideoView,
            VSLogTypeOperatorVwallSwitch, VSLogTypeOperatorPtz,VSLogTypeOperatorRecSearch,VSLogTypeOperatorRecPlay,VSLogTypeOperatorRecDownload,
            VSLogTypeOperatorTalkBack,VSLogTypeOperatorUserLogSearch,VSLogTypeOperatorMgrSearch,VSLogTypeOperatorSearch,VSLogTypeOperatorSvrStateSearch,
            VSLogTypeOperatorDevStateSearch,VSLogTypeOperatorResStateSearch,VSLogTypeServerStat,VSLogTypeServerStatOnline,
            VSLogTypeServerStatOffline,VSLogTypeServerStatNull,VSLogTypeDeviceStat,VSLogTypeDeviceStatOnline,VSLogTypeDeviceStatOffline,
            VSLogTypeDeviceStatNull,VSLogTypeResourceStat,VSLogTypeResourceStatOnline,VSLogTypeResourceStatOffline,VSLogTypeResourceStatNull,
            VSLogTypeOther};

    private String getHostNameIp()
    {
        int num =  HostNameIpArray.length;
        int pos = random.nextInt(num);
        return HostNameIpArray[pos];
    }
    private VSLogType getLogType()
    {
        int num = LogTypeArray.length;
        int pos = random.nextInt(num);
        return LogTypeArray[pos];
    }

    private VSLogLevel getLogLevel()
    {
        int num=6;
        int pos = random.nextInt(num);
        return LogLevelArray[pos];
    }

    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                .intValue();
    }
    private String getBusinessId()
    {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateNowStr = sdf.format(d);
        String bid = dateNowStr + String.format("%8d", count).replace(" ", "0");
        count++;
        return bid;
    }
    private  String generateMixString(int length) {
        StringBuffer sb = new StringBuffer();
        //Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(letterChar.length())));
        }
        return sb.toString();
    }

    private VSLogOperatorContent getAOPContent()
    {
        int num = UserNameArray.length;
        int pos = random.nextInt(num);

        VSLogOperatorContent logOpContent =  VSLogOperatorContent.newBuilder().setUserName(UserNameArray[pos]).
                                              setNickName(NickNameArray[pos]).setResoureId(pos).
                                              setResourceName(ResourceNameArray[pos]).
                                              setLogDescription(generateMixString(25)).build();
        return logOpContent;
    }
    private VSLogServerStatContent getAServerContent()
    {
        int num = ServerNameArray.length;
        int pos = random.nextInt(num);

        VSLogServerStatContent logServerContent = VSLogServerStatContent.newBuilder().setServerId(pos).
                                                   setServerName(ServerNameArray[pos]).setGroupId(pos).
                                                   setGroupName(GroupNameArray[pos]).setStartTime(getDateTimeNs()).
                                                   setEndTime(getDateTimeNs()).build();
        return logServerContent;
    }

    public static void main(String[] args) {

        SimpleLogClientStart simpleLogClient  = new SimpleLogClientStart();
        simpleLogClient.createChannel();

        VSLogServiceGrpc.VSLogServiceBlockingStub simpleLogServiceStub = VSLogServiceGrpc.newBlockingStub(simpleLogClient.managedChannel);

        do {
            VSLogResponse vsLogResponse = simpleLogServiceStub.sentVSLog(VSLogRequest.newBuilder().
                    setDateTimeMs(simpleLogClient.getDateTimeNs()).
                    setHostNameIp(simpleLogClient.getHostNameIp()).
                    setPId(getProcessID()).
                    setPName(SimpleLogClientStart.class.getName()).
                    setLogLevel(simpleLogClient.getLogLevel()).
                    setLogType(simpleLogClient.getLogType()).
                    setBusinessId(simpleLogClient.getBusinessId()).
                    setLogContent(simpleLogClient.generateMixString(25)).
                    build());
            System.out.println("response:" + vsLogResponse.getResult());


            VSLogResponse vsLogResponse2 = simpleLogServiceStub.sentVSLog(VSLogRequest.newBuilder().
                    setDateTimeMs(simpleLogClient.getDateTimeNs()).
                    setHostNameIp(simpleLogClient.getHostNameIp()).
                    setPId(getProcessID()).
                    setPName(SimpleLogClientStart.class.getName()).
                    setLogLevel(simpleLogClient.getLogLevel()).
                    setLogType(simpleLogClient.getLogType()).
                    setBusinessId(simpleLogClient.getBusinessId()).
                    setOpContent(simpleLogClient.getAOPContent()).
                    build());
            System.out.println("response:" + vsLogResponse2.getResult());


            VSLogResponse vsLogResponse3 = simpleLogServiceStub.sentVSLog(VSLogRequest.newBuilder().
                    setDateTimeMs(simpleLogClient.getDateTimeNs()).
                    setHostNameIp(simpleLogClient.getHostNameIp()).
                    setPId(getProcessID()).
                    setPName(SimpleLogClientStart.class.getName()).
                    setLogLevel(simpleLogClient.getLogLevel()).
                    setLogType(simpleLogClient.getLogType()).
                    setBusinessId(simpleLogClient.getBusinessId()).
                    setSerContent(simpleLogClient.getAServerContent()).
                    build());
            System.out.println("response:" + vsLogResponse3.getResult());
            try {
                TimeUnit.MILLISECONDS.sleep(10);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }while(true);
        //simpleLogClient.managedChannel.shutdownNow();
        //simpleClientStart.shutdown();
    }
}