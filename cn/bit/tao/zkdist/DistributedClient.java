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
	
	//ע�⣺��volatile��������ڣ�
	private volatile List<String> serverList;
	private ZooKeeper zk=null;
	
	//��ȡZK��Ⱥ����
	public void getConnect() throws Exception{
		zk=new ZooKeeper(connectString,sessionTimeout,new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				//�յ��¼�֪ͨ��Ļص�����
				try {
					//���¸��·������б�����ע���˼���
					getServerList();
				} catch (Exception e) {
					
				}
			}	
		});
	}
	
	//��ȡ��������Ϣ�б�
	public void getServerList() throws KeeperException, InterruptedException{
		//��ȡ�������ӽڵ���Ϣ�����ҶԸ��ڵ���м���
		List<String> children = zk.getChildren(parentNode, true);
		ArrayList<String> servers = new ArrayList<String>();
		for(String child:children){
			byte[] data = zk.getData(parentNode+"/"+child, false, null);
			servers.add(new String(data));
		}
		serverList=servers;
		
		//��ӡ�������б�
		System.out.println(serverList.toString());
	}
	
	/*
	 * ҵ����
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
		 * ��ȡZK����
		 * ��ȡservers���ӽڵ���Ϣ���������������л�ȡ��������Ϣ�б�
		 * ҵ���߳�����
		 */
	    DistributedClient client=new DistributedClient();
	    client.getConnect();
	    
	    client.getServerList();
	    
	    client.handleBusiness();
	}
}
