package cn.bit.tao.zkdist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

public class DistributedClient {
	private static final String connectString="10.108.21.2:2181,10.108.21.29:2181,10.108.21.236:2181";
	private static final int sessionTimeout=2000;
	private static final String parentNode="/servers";
	
	//注意：加volatile的意义何在？
	private volatile List<String> serverList;
	private ZooKeeper zk=null;
	
	//获取ZK集群连接
	public void getConnect() throws Exception{
		zk=new ZooKeeper(connectString,sessionTimeout,new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				//收到事件通知后的回调函数
				try {
					//重新更新服务器列表，并且注册了监听
					getServerList();
				} catch (Exception e) {
					
				}
			}	
		});
	}
	
	//获取服务器信息列表
	public void getServerList() throws KeeperException, InterruptedException{
		//获取服务器子节点信息，并且对父节点进行监听
		List<String> children = zk.getChildren(parentNode, true);
		ArrayList<String> servers = new ArrayList<String>();
		for(String child:children){
			byte[] data = zk.getData(parentNode+"/"+child, false, null);
			servers.add(new String(data));
		}
		serverList=servers;
		
		//打印服务器列表
		System.out.println(serverList.toString());
	}
	
	/*
	 * 业务功能
	 */
	public void handleBusiness(){
		System.out.println("client is working..");
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		/*
		 * 获取ZK连接
		 * 获取servers的子节点信息（并监听），从中获取服务器信息列表
		 * 业务线程启动
		 */
	    DistributedClient client=new DistributedClient();
	    client.getConnect();
	    
	    client.getServerList();
	    
	    client.handleBusiness();
	}
}
