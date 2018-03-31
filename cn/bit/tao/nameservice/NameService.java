package cn.bit.tao.nameservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *@author  Tao wenjun
 *��������
 *
 */

public class NameService {
	//������ӡ��־
	public static final Logger LOG = LoggerFactory.getLogger(NameService.class);
	
	//����ʱ��
	private static final int SESSION_TIME = 2000;
	
	//ZooKeeper Hosts
	private static final String Hosts = "10.108.21.2:2181";
	
	//ZK�ͻ���
	protected ZooKeeper zk = null;
	
	//ZK���ڵ���
	private String nameRoot = "/NameService1";
	
	//ZK���ڵ�ֵ
	private String nameRootValue = "IsNameService";
	
	public NameService() {
		try {
			zk = new ZooKeeper(Hosts,SESSION_TIME,new Watcher(){

				@Override
				public void process(WatchedEvent event) {
					LOG.info(event.getType()+"-->"+event.getPath());
				}
				
			});
			if(zk!=null){
				try {
					if(zk.exists(nameRoot, false)==null){
						zk.create(nameRoot, nameRootValue.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				} catch (KeeperException e) {
					LOG.debug(String.valueOf(e));
					e.printStackTrace();
				} catch (InterruptedException e) {
					LOG.debug(String.valueOf(e));
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			LOG.debug(String.valueOf(e));
			e.printStackTrace();
		}	
	}
	
	/**
	 * ��ע��������ַ���name����ZK�д���һ��/NameService/name��znode·��
	 * @param name  ��ע��������ַ���
	 * @return �Ƿ񴴽��ɹ�
	 */
	public boolean registerService(String name){
		String path = nameRoot + "/" + name;
		boolean result = false;
		try {
			if(zk.exists(path, false)==null){
				zk.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				LOG.info(name + " registered successfully");
				result = true;
			}
		} catch (KeeperException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ɾ������
	 * @param name ��Ҫɾ����������
	 * @return �Ƿ�ɾ���ɹ�
	 */
	public boolean deleteService(String name){
		String path = nameRoot + "/" + name;
		boolean result = false;
		try {
			if(zk.exists(path, false)!=null){
				zk.delete(path, 0);
				LOG.info(name + " deleted successfully");
				result = true;
			}
		} catch (KeeperException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * ��ȡ����ע��ķ���
	 * @return ע��ķ����б�
	 */
	public List<String> readAllService(){
		List<String> nameList = new ArrayList<>();
		try {
			nameList = zk.getChildren(nameRoot, false);
		} catch (KeeperException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		}
		return nameList;
	}
	
	public String createTempNode(String name,Object value){
		String path = nameRoot + "/temp/" + name;
		try {
			if(zk.exists(nameRoot+"/temp", false)==null){
				zk.create(nameRoot+"/temp", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			String pathString=null;
			if(zk.exists(path, false)==null){
				pathString = zk.create(path, value.toString().getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			}
		    LOG.info(name + " createTempNode successfully! path = "+pathString);
		    return pathString;
		} catch (KeeperException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		} catch (InterruptedException e) {
			LOG.error(String.valueOf(e));
			e.printStackTrace();
		}
		return "--------------------";
	}
	
	/**
	 * �ر�ZK����
	 */
	public void closeZk(){
		if(zk!=null){
			try {
				zk.close();
			} catch (InterruptedException e) {
				LOG.error(String.valueOf(e));
				e.printStackTrace();
			}	
			LOG.info("Zookeeper close success");
		}
	}
	
	public static void main(String[] args) {
		NameService nameService=new NameService();
		nameService.registerService("service_1");
		nameService.registerService("service_2");
		nameService.registerService("service_1");
		nameService.registerService("service_3");
		List<String> list=nameService.readAllService();
		System.out.println(list.toString());
		nameService.deleteService("service_1");
		
		for (int i = 0; i < 10; i++) {
			System.out.println(nameService.createTempNode("service_"+i,"123456_"+i));
		}
	}
}
