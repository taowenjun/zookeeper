package cn.bit.tao.masterslave;

import java.io.Serializable;

/**
 *@author  tao wenjun
 *RunningData：服务器数据
 */

public class RunningData implements Serializable{
	
	private static final long serialVersionUID = 4260577459043203630L;
	
	//服务器
	private long cid;
	
	//服务器名称
	private String name;

	/**
	 * @return the cid
	 */
	public long getCid() {
		return cid;
	}

	/**
	 * @param cid the cid to set
	 */
	public void setCid(long cid) {
		this.cid = cid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
