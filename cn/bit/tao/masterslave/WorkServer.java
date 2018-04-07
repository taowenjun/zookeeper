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
 *WorkServer�����еķ�����������ѡ�ٹ���
 */

public class WorkServer {
	
	//�ͻ���״̬
	private volatile boolean running = false;
	
	private ZkClient zkClient;
	
	//zk���ڵ�·��
	public static final String MASTER_PATH = "/master";
	
	//���������ڼ������ڵ�ɾ���¼���
	private IZkDataListener dataListener;
	
	//������������Ϣ
	private RunningData serverData;
	
	//���ڵ������Ϣ
	private RunningData masterData;
	
	//������
	private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);
	
	//�ӳ�ʱ��5s
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
							System.out.println(serverData.getCid()+" ��ѡ");
							takeMaster();
						}
						
					}, delayTime, TimeUnit.SECONDS);
				}
			}
		};
	}
	
	//����
	public void start() throws Exception{
		if(running){
			throw new Exception("server has startup...");
		}
		running = true;
		zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
        takeMaster();
	}
	
	//ֹͣ
	public void stop() throws Exception{
		if(!running){
			throw new Exception("server has stopped...");
		}
		running = false;
		delayExector.shutdown();
		zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
		releaseMaster();
	}
	
	//��ע���ڵ�
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
				}, 5, TimeUnit.SECONDS);   //������ע�ã�ÿ5s�ͷ�һ�����ڵ�
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
	
	//�ͷ����ڵ�
	private void releaseMaster(){
		if(checkMaster()){
			zkClient.delete(MASTER_PATH);
		}
	}
	
	//����Լ��Ƿ������ڵ�
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
	
	//���ÿͻ���
	public void setZkClient(ZkClient zkClient){
		this.zkClient=zkClient;
	}
	
	//��ȡ�ͻ���
	public ZkClient getZkClinet(){
		return zkClient;
	}	
}
