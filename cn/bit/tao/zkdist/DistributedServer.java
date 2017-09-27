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
	
	//������ZK�Ŀͻ�������
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
	
	//��ZK��Ⱥע���������Ϣ
	public void registerServer(String hostname) throws KeeperException, InterruptedException{
		if(zk.exists(parentNode, false)==null){
			zk.create(parentNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}
		String create = zk.create(parentNode+"/server", hostname.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
	    System.out.println(hostname + " is online..."+create);
	}
	
	/*
	 * ҵ����
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
		 * ��ȡzk����
		 * ����zk����ע���������Ϣ
		 * ����ҵ����
		 */
		DistributedServer server=new DistributedServer();
		server.getConnect();
		
		server.registerServer(args[0]);
		
		server.handleBusiness(args[0]);
	}
}
