package cn.bit.tao.zkdist;

import java.io.IOException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class DistributedServer {
	private static final String connectString="10.108.21.2:2181,10.108.21.29:2181,10.108.21.236:2181";
	private static final int sessionTimeout=2000;
	private static final String parentNode="/servers";
	
	private ZooKeeper zk=null;
	
	//创建到ZK的客户端连接
	public void getConnect() throws IOException{
		zk=new ZooKeeper(connectString,sessionTimeout,new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				System.out.println(event.getType()+"---"+event.getPath());
				try {
					zk.getChildren("/", true);
				} catch (KeeperException | InterruptedException e) {
					e.printStackTrace();
				}
			}			
		});
	}
	
	//向ZK集群注册服务器信息
	public void registerServer(String hostname) throws KeeperException, InterruptedException{
		if(zk.exists(parentNode, false)==null){
			zk.create(parentNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		String create = zk.create(parentNode+"/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	    System.out.println(hostname + " is online..."+create);
	}
	
	/*
	 * 业务功能
	 */
	public void handleBusiness(String hostname){
		System.out.println(hostname + " is working..");
		try {
			Thread.sleep(Long.MAX_VALUE);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
		/*
		 * 获取zk连接
		 * 利用zk连接注册服务器信息
		 * 启动业务功能
		 */
		DistributedServer server=new DistributedServer();
		server.getConnect();
		
		server.registerServer(args[0]);
		
		server.handleBusiness(args[0]);
	}
}
