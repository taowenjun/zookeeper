package cn.bit.tao.zkclient;
import java.io.IOException;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

/*
 * @author Tao wenjun
 * ZooKeeper�ͻ��˵�ʹ��
 */
public class SimpleZkClient {

	private static final String connectString="10.108.21.2:2181,10.108.21.29:2181,10.108.21.236:2181";
	private static final int sessionTimeout=2000;
	static ZooKeeper zkClient =null;
	
	@Before
	public void init() throws IOException{
		zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher(){

			@Override
			public void process(WatchedEvent event) {
				//�յ�ʱ��֪ͨ��Ļص������������Լ����¼������߼���
				System.out.println(event.getType()+"---"+event.getPath());
			}
		});
	}
	
	/*
	 * �����ڵ�
	 */
	@Test
	public void testCreate() throws IOException, KeeperException, InterruptedException {
		String nodeCreated = zkClient.create("/eclipse", "haha".getBytes(),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	    System.out.println(nodeCreated);
	}
	
	/*
	 * ��ȡ�ӽڵ�
	 */
	@Test
	public void getChildren() throws KeeperException, InterruptedException{
		List<String> children=zkClient.getChildren("/", true);
		for(String child:children){
			System.out.println(child);
		}
	}

	/*
	 * �ж��Ƿ����
	 */
	@Test
	public void testExist() throws Exception{
		Stat stat=zkClient.exists("/eclipse", false);
		System.out.println(stat==null?"not exist":"exist");
	}
	
	/*
	 * ��ȡznode������
	 */
	@Test
	public void getData() throws KeeperException, InterruptedException{
		byte[] data = zkClient.getData("/eclipse", false, null);
		System.out.println(new String(data));
	}
	
	/*
	 * ɾ���ڵ�
	 */
	@Test
	public void deleteNode() throws InterruptedException, KeeperException{
		//��������ָ��Ҫɾ���İ汾��-1��ʾɾ�����а汾
		zkClient.delete("/test0000000001", -1);
	}
	
	/*
	 * �޸�����
	 */
	@Test
	public void setData() throws KeeperException, InterruptedException{
		zkClient.setData("/eclipse", "xixi".getBytes(), -1);
	}
}
