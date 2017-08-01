package com.vorxsoft.ieye.logservice;


import com.vorxsoft.ieye.proto.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;


public class SimpleLogServer extends VSLogServiceGrpc.VSLogServiceImplBase
{
    private static Logger logger = LogManager.getLogger(SimpleLogServerStart.class.getName());

    public static Logger getLogger() {
        return logger;
    }
    final Level VERBOSE = Level.forName("VERBOSE", 550);
    final Level DIAG = Level.forName("DIAG", 350);
    private String logLevel2string(VSLogLevel level){
        String tmp = "";
        switch(level){
            case VSLogLevelTrace:
                tmp =  "T";
                break;
            case VSLogLevelDebug:
                tmp = "D";
                break;
            case VSLogLevelInfo:
                tmp = "I";
                break;
            case VSLogLevelWarning:
                tmp = "W";
                break;
            case VSLogLevelError:
                tmp = "E";
                break;
            case VSLogLevelFatal:
                tmp = "F";
                break;
            case UNRECOGNIZED:
                tmp =  "";
                break;
        }
        return tmp;
    }
    private String logType2string(VSLogType type){
        String tmp = "";
        switch (type){
            case VSLogTypeEvent:
                tmp = "log_event";
                break;
            case VSLogTypeMonitorSrcEvent:
                tmp = "log_event_src_monitor";
                break;
            case VSLogTypeDigitalIOSrcEvent:
                tmp = "log_ event_src_sio";
                break;
            case VSLogTypeDeviceSrcEvent:
                tmp = "log_ event_src_dev";
                break;
            case VSLogTypeServerSrcEvent:
                tmp = "log_ event_src_machine";
                break;
            case VSLogTypeLinkage:
                tmp = "log_linkage";
                break;
            case VSLogTypeUserLogin:
                tmp = "log_user_login";
                break;
            case VSLogTypeManage:
                tmp = "log_manage";
                break;
            case VSLogTypeManageAdd:
                tmp = "log_mgr_add";
                break;
            case VSLogTypeManageMod:
                tmp = "log_mgr_mod";
                break;
            case VSLogTypeManageDel:
                tmp = "log_mgr_del";
                break;
            case VSLogTypeManageReboot:
                tmp = "log_mgr_reboot";
                break;
            case VSLogTypeManageStart:
                tmp = "log_mgr_start";
                break;
            case VSLogTypeManageStop:
                tmp = "log_mgr_stop";
                break;
            case VSLogTypeOperator:
                tmp = "log_operator";
                break;
            case VSLogTypeOperatorLiveVideoView:
                tmp = "log_livevideo_view";
                break;
            case VSLogTypeOperatorVwallSwitch:
                tmp = "log_vwall_switch";
                break;
            case VSLogTypeOperatorPtz:
                tmp = "log_ptz";
                break;
            case VSLogTypeOperatorRecSearch:
                tmp = "log_rec_search";
                break;
            case VSLogTypeOperatorRecPlay:
                tmp = "log_rec_play";
                break;
            case VSLogTypeOperatorRecDownload:
                tmp = "log_rec_download";
                break;
            case VSLogTypeOperatorTalkBack:
                tmp = "log_talkback";
                break;
            case VSLogTypeOperatorUserLogSearch:
                tmp = "log_userlog_search";
                break;
            case VSLogTypeOperatorMgrSearch:
                tmp = "log_mgr_search";
                break;
            case VSLogTypeOperatorSearch:
                tmp = "log_operator_search";
                break;
            case VSLogTypeOperatorSvrStateSearch:
                tmp = "log_svrstate_search";
                break;
            case VSLogTypeOperatorDevStateSearch:
                tmp = "log_devstate_search";
                break;
            case VSLogTypeOperatorResStateSearch:
                tmp = "log_resstate_search";
                break;
             case VSLogTypeServerStat:
                tmp = "log_server_state";
                break;
            case VSLogTypeServerStatOnline:
                tmp = "log_server_online";
                break;
            case VSLogTypeServerStatOffline:
                tmp = "log_server_offline";
                break;
            case VSLogTypeServerStatNull:
                tmp = "log_server_null";
                break;
            case VSLogTypeDeviceStat:
                tmp = "log_device_state";
                break;
            case VSLogTypeDeviceStatOnline:
                tmp = "log_device_online";
                break;
            case VSLogTypeDeviceStatOffline:
                tmp = "log_device_offline";
                break;
            case VSLogTypeDeviceStatNull:
                tmp = "log_device_null";
                break;
            case VSLogTypeResourceStat:
                tmp = "log_res_state";
                break;
            case VSLogTypeResourceStatOnline:
                tmp = "log_res_online";
                break;
            case VSLogTypeResourceStatOffline:
                tmp = "log_res_offline";
                break;
            case VSLogTypeResourceStatNull:
                tmp = "log_res_null";
                break;
            case VSLogTypeOther:
                tmp = "log_other";
                break;
            case UNRECOGNIZED:
                tmp = "";
                break;
        }
        return tmp;
    }
    private String VSLogRequest2string(VSLogRequest request){
        String tmpstring="";
        tmpstring = request.getDateTimeMs()+"|";
        tmpstring += request.getHostNameIp()+"|";
        tmpstring += request.getPId()+"|";
        tmpstring += logLevel2string(request.getLogLevel())+"|";
        tmpstring += logType2string(request.getLogType())+"|";
        tmpstring += request.getBusinessId() + "|";
        switch(request.getLogContentofCase()){
            case LOGCONTENT:
                //tmpstring += request.getLogContent();
                //tmpstring += request.getLogContentBytes().toStringUtf8();
                tmpstring += request.getLogContentBytes().toStringUtf8();;
                break;
            case OPCONTENT:
                tmpstring += request.getOpContent().getUserName()+","
                          + request.getOpContent().getNickName()+ ","
                          + request.getOpContent().getLogDescription()+","
                          + request.getOpContent().getResoureId()+","
                          + request.getOpContent().getResourceName();
                break;
            case SERCONTENT:
                tmpstring += request.getSerContent().getServerId() + ","
                          + request.getSerContent().getServerName() + ","
                          + request.getSerContent().getGroupId() + ","
                          + request.getSerContent().getGroupName() + ","
                          + request.getSerContent().getStartTime() + ","
                          + request.getSerContent().getEndTime();
                break;
            case DEVCONTENT:
                tmpstring += request.getDevContent().getDeviceId() + ","
                        + request.getDevContent().getDeviceName() + ","
                        + request.getDevContent().getGroupId() + ","
                        + request.getDevContent().getGroupName() + ","
                        + request.getDevContent().getStartTime() + ","
                        + request.getDevContent().getEndTime();
                break;
            case RESCONTENT:
                tmpstring += request.getResContent().getResourceId() + ","
                        + request.getResContent().getResourceName() + ","
                        + request.getResContent().getGroupId() + ","
                        + request.getResContent().getGroupName() + ","
                        + request.getResContent().getStartTime() + ","
                        + request.getResContent().getEndTime();
                break;
            case LOGCONTENTOF_NOT_SET:
                break;
        }
        return tmpstring;
    }
    @Override
    public void sentVSLog(VSLogRequest request,io.grpc.stub.StreamObserver<com.vorxsoft.ieye.proto.VSLogResponse> response)
    {
        String logstring  =  VSLogRequest2string(request);
        System.out.println("receive:::::" + logstring);
        //logger.log(Level.getLevel("vsall"),logstring);
        logger.trace(logstring);
        if ((request.getLogLevel() == VSLogLevel.VSLogLevelError ) ||
                (request.getLogLevel() == VSLogLevel.VSLogLevelFatal) ||
                (request.getLogLevel() ==VSLogLevel.VSLogLevelWarning)  ){
            //logger.log(Level.getLevel("vserror"),logstring);
            logger.error(logstring);

        }
        //logger.error(logstring);
        VSLogResponse sentlLogResponse = VSLogResponse.newBuilder().setBusinessId(request.getBusinessId()).setResult(0).build();
        response.onNext(sentlLogResponse);
        response.onCompleted();
        //logger.exit();
    }

}