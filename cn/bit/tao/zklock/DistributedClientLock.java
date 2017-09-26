package cn.bit.tao.zklock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class DistributedClientLock {
	
	//会话超时
	private static final int SESSION_TIMEOUT=5000;
	//zookeeper集群地址
	private static final String hosts="10.108.21.2:2181,10.108.21.29:2181,10.108.21.236:2181";
	private String groupNode="locks";
	private String subNode="sub";
	private boolean haveLock=false;
	
	private ZooKeeper zk;
	// 记录自己创建的子节点路径
	private volatile String thisPath;
	
	/*
	 * 连接ZooKeeper
	 */
	public void connectZookeeper() throws IOException, Exception, InterruptedException{
		zk=new ZooKeeper(hosts,SESSION_TIMEOUT,new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				try{
					//判断事件类型，此处只处理子节点变化事件
					if(event.getType()==EventType.NodeChildrenChanged&&event.getPath().equals("/"+groupNode)){
						//获取子节点，并对父节点进行监听
						List<String> childrenNodes=zk.getChildren("/"+groupNode, true);
						String thisNode=thisPath.substring(("/"+groupNode+"/").length());
					    Collections.sort(childrenNodes);
					    if(childrenNodes.indexOf(thisNode)==0){
					    	doSomething();
					    	//重新注册一把锁
					    	thisPath=zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					    }
					}
				}catch(Exception e){
					
				}
			}		
		});
		
		//程序一进来就先注册一把锁到zk上
		thisPath=zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		Thread.sleep(new Random().nextInt(1000));
		// 记录自己创建的子节点路径
		List<String> childrenNodes=zk.getChildren("/"+groupNode, true);
		
		//如果争抢资源的程序就只有自己，则可以直接去访问共享资源 
		if(childrenNodes.size()==1){
			doSomething();
			thisPath=zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		}
	}
	
	public void doSomething() throws Exception{
		try{
			System.out.println("gain lock: "+thisPath);
			Thread.sleep(2000);
		}finally{
			System.out.println("finished: "+thisPath);
			zk.delete(this.thisPath, -1);
		}
	}
	
	public static void main(String[] args) throws Exception {
		DistributedClientLock dl1=new DistributedClientLock();
		dl1.connectZookeeper();
		
		Thread.sleep(Long.MAX_VALUE);
	}

}
