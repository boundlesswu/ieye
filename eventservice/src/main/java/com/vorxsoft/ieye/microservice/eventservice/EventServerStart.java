package com.vorxsoft.ieye.microservice.eventservice;

import com.coreos.jetcd.Watch;
import com.vorxsoft.ieye.microservice.MicroService;
import com.vorxsoft.ieye.microservice.MicroServiceImpl;
import com.vorxsoft.ieye.microservice.WatchCallerInterface;
import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2017/8/3 0003.
 */
public class EventServerStart implements WatchCallerInterface {
  @Override
  public void WatchCaller(Watch.Watcher watch) {
    System.out.println("watcher response  " + watch.listen());
  }
  private ScheduledExecutorService executor_;
  public long count = 0;
  public Connection conn=null;
  //public Statement st = null;
  private static int PORT = 9999;
  private Server server;
  private static String hostip;
  private static int ttl = 10;
  private static String dbname;
  private static String dbAddress;
  private static String dbUrl=null;
  private static String dbUser=null;
  private static String dbPasswd=null;
  private static String driverClassName=null;
  private static String serviceName;
  private static String registerCenterName;
  private static String registerCenterAddress = "http://192.168.20.251:2379";
  private static String mqName;
  private static String mqIP;
  private static int mqPort;
  private Jedis jedis;
  private InputStream cfgFile = this.getClass().getClassLoader().getResourceAsStream("event_service.xml");
  private ScheduledExecutorService getExecutor(){
    return  executor_;
  }

  public void cfgInit() {
    // 解析books.xml文件
    // 创建SAXReader的对象reader
    SAXReader reader = new SAXReader();
    try {
      System.out.println("cfg file is:"+cfgFile);
      // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
           //Document document = reader.read(new File(cfgFile));
      Document document = reader.read(cfgFile);
      // 通过document对象获取根节点bookstore
      Element bookStore = document.getRootElement();
      // 通过element对象的elementIterator方法获取迭代器
      Iterator it = bookStore.elementIterator();
      // 遍历迭代器，获取根节点中的信息（书籍）
      while (it.hasNext()) {
        //System.out.println("=====开始遍历某一本书=====");
        Element cfg = (Element) it.next();
        // 获取book的属性名以及 属性值
        List<Attribute> bookAttrs = cfg.attributes();
        System.out.println("cfgname :" + cfg.getName());
        for (Attribute attr : bookAttrs) {
          //System.out.println("属性名：" + attr.getName() + "--属性值：" + attr.getValue());
        }
        String tname = cfg.getName();
        //解析子节点的信息
        Iterator itt = cfg.elementIterator();
        while (itt.hasNext()) {
          Element bookChild = (Element) itt.next();
          String lname = bookChild.getName();
          String lvalue = bookChild.getStringValue();
          //System.out.println("节点名：" + bookChild.getName() + "--节点值：" + bookChild.getStringValue());
          if (tname.equals("info")) {
            if (lname.equals("hostip"))
              hostip = lvalue;
            else if (lname.equals("port"))
              PORT = Integer.parseInt(lvalue);
            else if (lname.equals("name"))
              serviceName = lvalue;
            else if (lname.equals("ttl"))
              ttl = Integer.parseInt(lvalue);
          }
          if (tname.equals("database")) {
            if (lname.equals("name"))
              dbname = lvalue;
            else if (lname.equals("address"))
              dbAddress = lvalue;
            else if (lname.equals("user"))
              dbUser = lvalue;
            else if (lname.equals("passwd"))
              dbPasswd = lvalue;
            else if (lname.equals("driverClassName"))
              driverClassName = lvalue;
          }
          if (tname.equals("registerCenter")) {
            if (lname.equals("name"))
              registerCenterName = lvalue;
            else if (lname.equals("address"))
              registerCenterAddress = lvalue;
          }
          if (tname.equals("mq")) {
            if (lname.equals("name"))
              mqName = lvalue;
            else if (lname.equals("ip"))
              mqIP = lvalue;
            else if (lname.equals("port"))
              mqPort = Integer.parseInt(lvalue);
          }
        }
        //System.out.println("=====结束遍历某一本书=====");
      }

    } catch (DocumentException e) {
      e.printStackTrace();
    }
  }
  public void dbInit() throws SQLException, ClassNotFoundException {
    dbUrl = "jdbc:"+dbname+"://"+dbAddress;
    System.out.println("db url :" + dbUrl);
    Class.forName(driverClassName);
    conn = DriverManager.getConnection(dbUrl,dbUser,dbPasswd);
    //st = conn.createStatement();
  }
  public void mqInit(){
    jedis  = new  Jedis(mqIP, mqPort);
  }

  public void alarm2event() throws SQLException {
    Set<String> set = jedis.keys("alarm_" +"*");
    Iterator<String> it = set.iterator();
    while(it.hasNext()){
      String keyStr = it.next();
      System.out.println(keyStr);
      Map<String, String> map = jedis.hgetAll(keyStr);
      Map<String, String> emap = alrrmMap2Eventmap(map);
      jedis.del(keyStr);
      count++;
      String key = emap.get("event_type") + count;
      jedis.hmset(key,map);
    }
  }

  public void travel(){
    getExecutor().scheduleAtFixedRate(()->{
      try {
        alarm2event();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    },1l,1L, TimeUnit.SECONDS);
    getExecutor().scheduleAtFixedRate(()->{
      try {
        event2db();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    },1l,2L, TimeUnit.SECONDS);
  }
  public void event2db() throws SQLException {
    event2db("event_monitor");
    event2db("event_sio");
  }

  public void event2db(String evenType) throws SQLException {
    Set<String> set = jedis.keys(evenType + "*");
    Iterator<String> it = set.iterator();
    while (it.hasNext()) {
      String keyStr = it.next();
      System.out.println(keyStr);
      Map<String, String> map = jedis.hgetAll(keyStr);
      String res_id = map.get("res_id");
      String res_name = map.get("res_name");
      String happen_time = map.get("happen_time");
      String sql=null;
      if(evenType.equals("event_monitor")){
        sql = "insert into tl_event_src_monitor(event_type,res_id,res_name,happen_time) values('event_monitor',?,?,?)";
      }else if(evenType.equals("event_sio")){
        sql = "insert into tl_event_src_sio(event_type,res_id,res_name,happen_time) values('event_monitor',?,?,?)";
      }
      PreparedStatement pstmt = conn.prepareStatement(sql);
      pstmt.setString(1, res_id);
      pstmt.setString(2, res_name);
      pstmt.setString(3, happen_time);
      if (pstmt.executeUpdate() > 0) {
        jedis.del(keyStr);
      }
      pstmt.close();
    }
  }

  public String evenType2string(int type){
    if( (type == 1) ||((type > 100)&&(type < 200))) { //VSEventTypeMonitor
      return "event_monitor";
    }else if ((type == 2) ||((type > 200)&&(type < 300))) { //VSEventTypeDigitalIO
      return "event_sio";
    }else {
      return null;
    }
  }
  public List<String> getResIdResNo(String dev_no,String res_uid) throws SQLException {
    PreparedStatement pstmt = conn.prepareStatement("select b.res_id,b.res_no,b.res_name from ti_device a join ti_resource b on a.dev_id=b.dev_id where a.dev_no = ? and b.res_uid = ?");
    pstmt.setString(1, dev_no);
    pstmt.setString(2, res_uid);
    ResultSet rs = pstmt.executeQuery();
    //ResultSet rs = st.executeQuery("select b.res_id,b.res_no from ti_device a join ti_resource b on a.dev_id=b.dev_id where a.dev_no='102' and b.res_uid='1'");
    while (rs.next()) {
      System.out.print(rs.getString(1));
      System.out.print("  ");
      System.out.println(rs.getString(2));
      System.out.print("  ");
      System.out.println(rs.getString(3));
    }
    List<String> a = new LinkedList<String>();
    a.add(rs.getString(1));
    a.add(rs.getString(2));
    a.add(rs.getString(3));
    rs.close();
    pstmt.close();
    return a;
  }

  public Map<String, String> alrrmMap2Eventmap(Map<String, String> map ) throws SQLException {
    String dev_no = map.get("deviceNo");
    String res_uid = map.get("ResourceUid");
    List<String> b = getResIdResNo(dev_no,res_uid);
    int res_id =  Integer.parseInt(b.get(1));
    int res_no = Integer.parseInt(b.get(2));
    String res_name = b.get(3);
    int type = Integer.parseInt(map.get("evenType"));
    Map<String, String> mymap =  new HashMap<String, String>();
    if( (type == 1) ||((type > 100)&&(type < 200))){ //VSEventTypeMonitor
      mymap.put("event_type",evenType2string(type));
      mymap.put("res_id",String.valueOf(res_id));
      mymap.put("res_name",res_name);
      mymap.put("group_id",null);
      mymap.put("group_name",null);
      mymap.put("happen_time",map.get("happen_time"));
      mymap.put("pic_path1",null);
      mymap.put("pic_path1",null);
      mymap.put("pic_path1",null);
    }else if ((type == 2) ||((type > 200)&&(type < 300))){ //VSEventTypeDigitalIO
      mymap.put("event_type",evenType2string(type));
      mymap.put("res_id",String.valueOf(res_id));
      mymap.put("res_name",res_name);
      mymap.put("group_id",null);
      mymap.put("group_name",null);
      mymap.put("happen_time",map.get("happen_time"));
    }else{
      System.out.println("wrong evenType");
      mymap = null;
    }
    return  mymap;
  }

  private void start() throws Exception {
    //server = NettyServerBuilder.forPort(PORT).addService(new EventServer().bindService()).build();
    server = NettyServerBuilder.forPort(PORT).addService(new EventServer(mqIP,mqPort).bindService()).build();
    server.start();

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        EventServerStart.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    try {
      server.awaitTermination(2, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    final EventServerStart simpleServerStart = new EventServerStart();
    simpleServerStart.cfgInit();
    MicroService myservice = new MicroServiceImpl();
    myservice.init(registerCenterAddress, simpleServerStart);
    simpleServerStart.start();
    simpleServerStart.dbInit();
    myservice.RegisteWithHB(serviceName, hostip, PORT, ttl);
    TimeUnit.DAYS.sleep(365 * 2000);
  }
}
