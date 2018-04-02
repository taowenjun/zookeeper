package cn.bit.tao.pubsub;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.apache.zookeeper.ZooKeeper;

import com.alibaba.fastjson.JSON;

/**
 *@author  Tao wenjun
 *WorkerServer:服务器
 */

public class WorkerServer {
	private ZkClient zk;
	private String configPath;
	private String serversPath;
	private ServerData serverData;
	private ServerConfig serverConfig;
	private IZkDataListener dataListener;
	
	public WorkerServer(String configPath,String serversPath,ServerData serverData,ZkClient zkClient,ServerConfig initConfig){
		this.zk=zkClient;
		this.configPath=configPath;
		this.serversPath=serversPath;
		this.serverData=serverData;
		this.serverConfig=initConfig;
		this.dataListener=new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String arg0) throws Exception {
				 
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
			     String retJson = new String((byte[])data);
			     ServerConfig serverConfigLocal = (ServerConfig)JSON.parseObject(retJson,ServerConfig.class);
			     updateConfig(serverConfigLocal);
			     System.out.println("new Work server config is:"+serverConfig.toString());
			}
		};
		
	}
	
    //启动
	public void start(){
		System.out.println("work server start...");
		initRunning();
	}
	
	//停止
	public void stop(){
		System.out.println("work server stop...");
		zk.unsubscribeDataChanges(configPath, dataListener);
	}
	
	//初始化
	private void initRunning(){
		registMe();
		zk.subscribeDataChanges(configPath, dataListener);
	}
	
	//注册
	private void registMe(){
		String mePath = serversPath.concat("/").concat(serverData.getAddress());
		try{
			zk.createEphemeral(mePath, JSON.toJSONString(serverData).getBytes());			
		}catch(ZkNoNodeException e){
			zk.createPersistent(serversPath,true);
			registMe();
		}
	}
	
	//更新
	private void updateConfig(ServerConfig serverConfig){
		this.serverConfig = serverConfig;
	}	
}
