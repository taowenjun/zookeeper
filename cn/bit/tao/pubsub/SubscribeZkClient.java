package cn.bit.tao.pubsub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 *@author  Tao wenjun
 *≤‚ ‘
 */

public class SubscribeZkClient {
	private static final int CLIENT_QTY = 10;

	private static final String CONFIG_PATH="/config";
	private static final String COMMAND_PATH="/command";
	private static final String SERVERS_PATH="/servers";
	
	public static void main(String[] args) {
        List<ZkClient> clients = new ArrayList<ZkClient>(); 
        List<WorkerServer> workServers = new ArrayList<WorkerServer>();
        ManageServer manageServer = null;
        
        try{
        	ServerConfig initConfig = new ServerConfig();
        	initConfig.setDbPwd("123456");
        	initConfig.setDbUrl("jdbc:mysql://localhost:3306/mydb");
        	initConfig.setDbUser("root");
        	
        	ZkClient clientManage = new ZkClient("10.108.21.2:2181", 5000, 5000, new BytesPushThroughSerializer());
        	manageServer = new ManageServer(SERVERS_PATH,COMMAND_PATH,CONFIG_PATH, clientManage,initConfig);
        	manageServer.start();
        	for(int i=0;i<CLIENT_QTY;i++){
        		ZkClient client = new ZkClient("10.108.21.2:2181",5000,5000,new BytesPushThroughSerializer());
        		clients.add(client);
        		ServerData serverData = new ServerData();
        		serverData.setId(i);
        		serverData.setName("WorkServer#"+i);
        		serverData.setAddress("192.168.1."+i);
        		WorkerServer workServer = new WorkerServer(CONFIG_PATH,SERVERS_PATH,serverData,client,initConfig);
        		workServers.add(workServer);
        		workServer.start();
        	}
        	System.out.println("«√ªÿ≥µº¸ÕÀ≥ˆ£°\n");
        	try {
				new BufferedReader(new InputStreamReader(System.in)).readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }finally{
        	System.out.println("Shutting down...");
        	
        	for(WorkerServer workerServer:workServers){
        		try{
        			workerServer.stop();
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}
        	
        	for(ZkClient client:clients){
        		try{
        			client.close();
        		}catch(Exception e){
        			e.printStackTrace();
        		}
        	}
        }
	}

}
