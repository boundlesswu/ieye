package com.vorxsoft.ieye.logservice;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SimpleLogServerStart implements WatchCallerInterface {
        @Override
        public void WatchCaller(Watch.Watcher watch){
            System.out.println("watcher response  " + watch.listen());
        }
    private static int PORT=8888;
    private Server server;
    private static String hostip;
    private static int ttl = 10;
    private static String dbname;
    private static String dbAddress;
    private static String serviceName;
    private static String registerCenterName;
    private static String registerCenterAddress = "http://192.168.20.251:2379";
    //final String cfgFile = "logservice/target/classes/log_service.xml";
    private final  String cfgFileName = "log_service.xml";
    private InputStream cfgFile;

    public void getConfigPath() throws FileNotFoundException {
        String tmp = String.valueOf(this.getClass().getClassLoader().getResource(cfgFileName));
        System.out.println("tmp:"+tmp);
        if(tmp.startsWith("jar"))
            cfgFile=new FileInputStream(new File(System.getProperty("user.dir")+File.separator+cfgFileName));
        else
            cfgFile = this.getClass().getClassLoader().getResourceAsStream(cfgFileName);
    }

    public void cfgInit() throws FileNotFoundException {
        getConfigPath();
        // 解析books.xml文件
        // 创建SAXReader的对象reader
        SAXReader reader = new SAXReader();
        try {
            // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
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
                    }
                    if (tname.equals("registerCenter")) {
                        if (lname.equals("name"))
                            registerCenterName = lvalue;
                        else if (lname.equals("address"))
                            registerCenterAddress = lvalue;
                    }
                }
                //System.out.println("=====结束遍历某一本书=====");
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }
    private void start() throws Exception{
        server = NettyServerBuilder.forPort(PORT).addService(new SimpleLogServer().bindService()).build();
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void  run(){
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                SimpleLogServerStart.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop(){
        try {
            server.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final SimpleLogServerStart simpleServerStart = new SimpleLogServerStart();
        SimpleLogServer.getLogger().entry();
        simpleServerStart.cfgInit();
      MicroService myservice = new MicroServiceImpl();
      myservice.init(registerCenterAddress, simpleServerStart);
      simpleServerStart.start();
      long ret = myservice.RegisteWithHB(serviceName, hostip, PORT, ttl);
      myservice.SetWatcher(serviceName,true);
      System.out.println("leasid is " + ret);
        TimeUnit.DAYS.sleep(365*2000);
        SimpleLogServer.getLogger().exit();
    }

}
