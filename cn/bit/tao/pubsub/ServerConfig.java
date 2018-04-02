package cn.bit.tao.pubsub;
/**
 *@author  Tao wenjun
 *ServerConfig:·þÎñÆ÷ÅäÖÃ
 */

public class ServerConfig {
	private String dbUrl;
	private String dbUser;
	private String dbPwd;
	
	/**
	 * @return the dbUrl
	 */
	public String getDbUrl() {
		return dbUrl;
	}
	
	/**
	 * @param dbUrl the dbUrl to set
	 */
	public void setDbUrl(String dbUrl) {
		this.dbUrl = dbUrl;
	}
	
	/**
	 * @return the dbUser
	 */
	public String getDbUser() {
		return dbUser;
	}
	
	/**
	 * @param dbUser the dbUser to set
	 */
	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}
	
	/**
	 * @return the dbPwd
	 */
	public String getDbPwd() {
		return dbPwd;
	}
	
	/**
	 * @param dbPwd the dbPwd to set
	 */
	public void setDbPwd(String dbPwd) {
		this.dbPwd = dbPwd;
	}
	
	@Override
	public String toString() {
		return "ServerConfig [dbUrl=" + dbUrl + ",dbUser=" + dbUser + ",dbPwd=" + dbPwd +"]";
	}
}
