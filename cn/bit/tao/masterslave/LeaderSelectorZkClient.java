package cn.bit.tao.masterslave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 *@author  Tao wenjun
 *Master选举进行测试
 */

public class LeaderSelectorZkClient {
	
	//启动的服务器
	private static final int CLIENT_QTY = 10;

	//Zookeeper服务器的地址
	private static final String ZOOKEEPER_SERVER = "10.108.21.2:2181";
	
	public static void main(String[] args) throws Exception {
		//保留所有ZkClient的列表
		List<ZkClient> clients = new ArrayList<>();
		//保留所有服务的列表
		List<WorkServer> workServers = new ArrayList<>();
		
		try{
		    for(int i=0;i<CLIENT_QTY;i++){
		    	//创建zkClient
			    ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
			    clients.add(client);
			    
			    //创建ServerData
			    RunningData runningData = new RunningData();
			    runningData.setCid(Long.valueOf(i));
			    runningData.setName("Client #"+i);
			    
			    //创建服务
			    WorkServer workServer = new WorkServer(runningData);
			    workServer.setZkClient(client);
			    
			    workServers.add(workServer);
			   
			    workServer.start();
				
		    }
		    System.out.println("敲回车键退出！\n");
		    
			new BufferedReader(new InputStreamReader(System.in)).readLine();
			
		}finally{
			System.out.println("Shutting down...");
			for(WorkServer workServer:workServers){
				try {
					workServer.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			for(ZkClient client:clients){
				client.close();
			}
		}
	}
}
