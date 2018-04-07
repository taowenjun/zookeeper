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
 *Masterѡ�ٽ��в���
 */

public class LeaderSelectorZkClient {
	
	//�����ķ�����
	private static final int CLIENT_QTY = 10;

	//Zookeeper�������ĵ�ַ
	private static final String ZOOKEEPER_SERVER = "10.108.21.2:2181";
	
	public static void main(String[] args) throws Exception {
		//��������ZkClient���б�
		List<ZkClient> clients = new ArrayList<>();
		//�������з�����б�
		List<WorkServer> workServers = new ArrayList<>();
		
		try{
		    for(int i=0;i<CLIENT_QTY;i++){
		    	//����zkClient
			    ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
			    clients.add(client);
			    
			    //����ServerData
			    RunningData runningData = new RunningData();
			    runningData.setCid(Long.valueOf(i));
			    runningData.setName("Client #"+i);
			    
			    //��������
			    WorkServer workServer = new WorkServer(runningData);
			    workServer.setZkClient(client);
			    
			    workServers.add(workServer);
			   
			    workServer.start();
				
		    }
		    System.out.println("�ûس����˳���\n");
		    
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
