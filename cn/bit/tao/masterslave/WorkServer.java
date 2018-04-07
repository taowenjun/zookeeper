package cn.bit.tao.masterslave;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

/**
 *@author  tao wenjun
 *WorkServer：运行的服务器，具有选举功能
 */

public class WorkServer {
	
	//客户端状态
	private volatile boolean running = false;
	
	private ZkClient zkClient;
	
	//zk主节点路径
	public static final String MASTER_PATH = "/master";
	
	//监听（用于监听主节点删除事件）
	private IZkDataListener dataListener;
	
	//服务器基本信息
	private RunningData serverData;
	
	//主节点基本信息
	private RunningData masterData;
	
	//调度器
	private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
	
	//延迟时间5s
	private int delayTime = 5;
	
	public WorkServer(RunningData runningData){
		this.serverData = runningData;
		this.dataListener = new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String arg0) throws Exception {
				
			}
			
			@Override
			public void handleDataChange(String arg0, Object arg1) throws Exception {
				if(masterData!=null&&masterData.getName().equals(serverData.getName())){
					takeMaster();
				}else{
					delayExector.schedule(new Runnable(){

						@Override
						public void run() {
							System.out.println(serverData.getCid()+" 竞选");
							takeMaster();
						}
						
					}, delayTime, TimeUnit.SECONDS);
				}
			}
		};
	}
	
	//启动
	public void start() throws Exception{
		if(running){
			throw new Exception("server has startup...");
		}
		running = true;
		zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
        takeMaster();
	}
	
	//停止
	public void stop() throws Exception{
		if(!running){
			throw new Exception("server has stopped...");
		}
		running = false;
		delayExector.shutdown();
		zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
		releaseMaster();
	}
	
	//抢注主节点
	private void takeMaster(){
		if(!running) 
			return;
		try{
			zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
			masterData = serverData;
			System.out.println(serverData.getName()+" is master");
			delayExector.schedule(new Runnable(){

				@Override
				public void run() {
	            	if(checkMaster()){
	            		releaseMaster();
	            	}
				}
				}, 5, TimeUnit.SECONDS);   //测试抢注用，每5s释放一次主节点
		}catch(ZkNodeExistsException e){ //
			RunningData runningData = zkClient.readData(MASTER_PATH,true);
			if(runningData == null){
				takeMaster();
			}else{
				masterData = runningData;
			}
		}catch(Exception e){
			//ignore
		}
	}
	
	//释放主节点
	private void releaseMaster(){
		if(checkMaster()){
			zkClient.delete(MASTER_PATH);
		}
	}
	
	//检查自己是否是主节点
	private boolean checkMaster(){
		try{
		    RunningData runningData = zkClient.readData(MASTER_PATH);
		    masterData = runningData;
		    if(masterData.getName().equals(serverData.getName())){
			    return true;
		    }
		    return false;
		}catch(ZkNoNodeException e){
			return false;
		}catch(ZkInterruptedException e){
			return checkMaster();
		}catch(Exception e){
			return false;
		}
	}
	
	//设置客户端
	public void setZkClient(ZkClient zkClient){
		this.zkClient=zkClient;
	}
	
	//获取客户端
	public ZkClient getZkClinet(){
		return zkClient;
	}	
}
