package com.vorxsoft.ieye.microservice.eventservice;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class test {
  private int PORT = 9999;
  private String hostip;
  private String dbname;
  private String dbAddress;
  private String serviceName;
  private int ttl;
  private String registerCenterName;
  private String registerCenterAddress="http://192.168.20.251:2379";

  //final String cfgFile = "C:\\Users\\oe\\workspace\\ieye\\eventservice\\src\\main\\resources\\event_service.xml";
  final String cfgFile = "event_service.xml";
  public void cfgInit() {
    // 解析books.xml文件
    // 创建SAXReader的对象reader
    SAXReader reader = new SAXReader();
    try {
      // 通过reader对象的read方法加载books.xml文件,获取docuemnt对象。
      Document document = reader.read(new File(cfgFile));
      // 通过document对象获取根节点bookstore
      Element bookStore = document.getRootElement();
      // 通过element对象的elementIterator方法获取迭代器
      Iterator it = bookStore.elementIterator();
      // 遍历迭代器，获取根节点中的信息（书籍）
      while (it.hasNext()) {
        System.out.println("=====开始遍历某一本书=====");
        Element cfg = (Element) it.next();
        // 获取book的属性名以及 属性值
        List<Attribute> bookAttrs = cfg.attributes();
        System.out.println("cfgname :" + cfg.getName());
        for (Attribute attr : bookAttrs) {
          System.out.println("属性名：" + attr.getName() + "--属性值：" + attr.getValue());
        }
        String tname = cfg.getName();
        //解析子节点的信息
        Iterator itt = cfg.elementIterator();
        while (itt.hasNext()) {
          Element bookChild = (Element) itt.next();
          String lname = bookChild.getName();
          String lvalue = bookChild.getStringValue();
          System.out.println("节点名：" + bookChild.getName() + "--节点值：" + bookChild.getStringValue());
          if (tname.equals("info")) {
            if (lname.equals("hostip"))
              hostip = lvalue;
            else if (lname.equals("port"))
              PORT = Integer.parseInt(lvalue);
            else if (lname.equals("name"))
              serviceName = lvalue;
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
        System.out.println("=====结束遍历某一本书=====");
      }

    } catch (DocumentException e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args) {
    test a = new test();
    a.cfgInit();

  }
}
