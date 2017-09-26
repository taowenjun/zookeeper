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
	
	//�Ự��ʱ
	private static final int SESSION_TIMEOUT=5000;
	//zookeeper��Ⱥ��ַ
	private static final String hosts="10.108.21.2:2181,10.108.21.29:2181,10.108.21.236:2181";
	private String groupNode="locks";
	private String subNode="sub";
	private boolean haveLock=false;
	
	private ZooKeeper zk;
	// ��¼�Լ��������ӽڵ�·��
	private volatile String thisPath;
	
	/*
	 * ����ZooKeeper
	 */
	public void connectZookeeper() throws IOException, Exception, InterruptedException{
		zk=new ZooKeeper(hosts,SESSION_TIMEOUT,new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				try{
					//�ж��¼����ͣ��˴�ֻ�����ӽڵ�仯�¼�
					if(event.getType()==EventType.NodeChildrenChanged&&event.getPath().equals("/"+groupNode)){
						//��ȡ�ӽڵ㣬���Ը��ڵ���м���
						List<String> childrenNodes=zk.getChildren("/"+groupNode, true);
						String thisNode=thisPath.substring(("/"+groupNode+"/").length());
					    Collections.sort(childrenNodes);
					    if(childrenNodes.indexOf(thisNode)==0){
					    	doSomething();
					    	//����ע��һ����
					    	thisPath=zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					    }
					}
				}catch(Exception e){
					
				}
			}		
		});
		
		//����һ��������ע��һ������zk��
		thisPath=zk.create("/"+groupNode+"/"+subNode, null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
		Thread.sleep(new Random().nextInt(1000));
		// ��¼�Լ��������ӽڵ�·��
		List<String> childrenNodes=zk.getChildren("/"+groupNode, true);
		
		//���������Դ�ĳ����ֻ���Լ��������ֱ��ȥ���ʹ�����Դ 
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
