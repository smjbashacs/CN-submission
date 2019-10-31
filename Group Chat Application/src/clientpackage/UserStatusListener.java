package clientpackage;

public interface UserStatusListener {
	public void online(String uname);
	public void offline(String uname);
}
