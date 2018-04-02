package cn.bit.tao.pubsub;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import com.alibaba.fastjson.JSON;

/**
 *@author  Tao wenjun
 *ManageServer�����������
 */

public class ManageServer {
	private String serversPath;
	private String commandPath;
	private String configPath;
	private ZkClient zkClient;
	private ServerConfig config;
	private IZkChildListener childListener;
	private IZkDataListener dataListener;
	private List<String> workServerList;
	
	public ManageServer(String serversPath,String commandPath,String configPath,ZkClient zkClient,ServerConfig config){
		this.serversPath=serversPath;
		this.commandPath=commandPath;
		this.zkClient=zkClient;
		this.config=config;
		this.configPath=configPath;
		this.childListener = new IZkChildListener() {
			
			@Override
			public void handleChildChange(String parentPath, List<String> currentChildren) throws Exception {
				workServerList  = currentChildren;
				System.out.println("work server list changed,newer list is ");
				execList();
			}
		};
		
		this.dataListener = new IZkDataListener() {
			
			@Override
			public void handleDataDeleted(String arg0) throws Exception {
				//ignore
			}
			
			@Override
			public void handleDataChange(String dataPath, Object data) throws Exception {
				String cmd = new String((byte[])data);
				System.out.println("cmd:"+cmd);
				exeCmd(cmd);
			}
		};
	}
	
	//��ʼ������
	private void initRunning(){
		zkClient.subscribeDataChanges(commandPath, dataListener);
		zkClient.subscribeChildChanges(serversPath, childListener);
	}
	
	//ִ������
	private void exeCmd(String cmdType){
		if("list".equals(cmdType)){
			execList();
		}else if("create".equals(cmdType)){
			execModify();
		}else{
			System.out.println("error command!"+cmdType);
		}
	}
	
	//��ӡlist
	private void execList(){
		System.out.println(workServerList.toString());
	}
	
	//ִ��create����
	private void execCreate(){
		if(!zkClient.exists(configPath)){
			try{
		    	zkClient.createPersistent(configPath, JSON.toJSONString(config).getBytes());
	    	}catch (ZkNodeExistsException e) {
			    zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
		    }catch(ZkNoNodeException e){
		    	String parentDir = configPath.substring(0, configPath.lastIndexOf('/'));
		    	zkClient.createPersistent(parentDir, true);
		    	execCreate();
		    }
		}
	}
	
	//ִ���޸�����
	private void execModify(){
		config.setDbUser(config.getDbUser()+"_modify");
		try{
			zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());			
		}catch(ZkNoNodeException e){
			execCreate();
		}
	}
	
	//����
	public void start(){
		initRunning();
	}
	
	//ֹͣ
	public void stop(){
		zkClient.unsubscribeChildChanges(serversPath, childListener);
		zkClient.unsubscribeDataChanges(commandPath, dataListener);
	}
}
