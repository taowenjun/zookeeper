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
 * ZooKeeper客户端的使用
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
				//收到时间通知后的回调函数（我们自己的事件处理逻辑）
				System.out.println(event.getType()+"---"+event.getPath());
			}
		});
	}
	
	/*
	 * 创建节点
	 */
	@Test
	public void testCreate() throws IOException, KeeperException, InterruptedException {
		String nodeCreated = zkClient.create("/eclipse", "haha".getBytes(),Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
	    System.out.println(nodeCreated);
	}
	
	/*
	 * 获取子节点
	 */
	@Test
	public void getChildren() throws KeeperException, InterruptedException{
		List<String> children=zkClient.getChildren("/", true);
		for(String child:children){
			System.out.println(child);
		}
	}

	/*
	 * 判断是否存在
	 */
	@Test
	public void testExist() throws Exception{
		Stat stat=zkClient.exists("/eclipse", false);
		System.out.println(stat==null?"not exist":"exist");
	}
	
	/*
	 * 获取znode的数据
	 */
	@Test
	public void getData() throws KeeperException, InterruptedException{
		byte[] data = zkClient.getData("/eclipse", false, null);
		System.out.println(new String(data));
	}
	
	/*
	 * 删除节点
	 */
	@Test
	public void deleteNode() throws InterruptedException, KeeperException{
		//参数二：指定要删除的版本，-1表示删除所有版本
		zkClient.delete("/test0000000001", -1);
	}
	
	/*
	 * 修改数据
	 */
	@Test
	public void setData() throws KeeperException, InterruptedException{
		zkClient.setData("/eclipse", "xixi".getBytes(), -1);
	}
}
