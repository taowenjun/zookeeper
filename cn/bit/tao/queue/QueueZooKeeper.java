package cn.bit.tao.queue;

import java.io.IOException;

import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 *@author  Tao wenjun
 *ZooKeeper实现队列
 */

public class QueueZooKeeper {
	
	protected static ZooKeeper zkClient = null;
	
	protected static Integer mutex;
	int sessionTimeout = 10000;
	
	protected static String root = "/queue";
	
	public QueueZooKeeper(String connectString) throws Exception {
		if(zkClient==null){
			zkClient = new ZooKeeper(connectString,sessionTimeout,new Watcher(){

				@Override
				public void process(WatchedEvent event) {
					System.out.println("receive watch event:"+event);
					if(event.getType()==EventType.NodeChildrenChanged){
						try{
							System.out.println("ReGet Child:"+zkClient.getChildren(event.getPath(), true));
						}catch(Exception e){
							
						}
					}
				}
			});
			mutex = new Integer(-1);
		}
		zkClient.exists("/queue/start", true);
		if(zkClient.exists(root, false)==null){
			System.out.println("create /queue");
			zkClient.create(root, "task-queue-fifo".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}else{
			System.out.println("/queue is exist!");
		}
	}
	
	public boolean addQueue(int i) throws Exception, InterruptedException{
		System.out.println("create /queue/x"+i+" x"+i);
		zkClient.create(root+"/x"+i, ("x"+i).getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		isCompleted();
		return true;
	}
	
	public void isCompleted() throws Exception, InterruptedException{
		int size=3;
		int length = zkClient.getChildren(root, true).size();
		System.out.println("Queue Complete:"+length+"/"+size);
		if(length>=size){
			System.out.println("create /queue/start start");
		}
	}
	
	public static void main(String[] main) throws Exception{
		ZkClient zkClient = new ZkClient("10.108.21.2:2181",5000);
		zkClient.deleteRecursive(root);
		zkClient.close();
		QueueZooKeeper keeper = new QueueZooKeeper("10.108.21.2:2181");
		for(int i=0;i<8;i++){
			keeper.addQueue(i);
		}
	}
}
