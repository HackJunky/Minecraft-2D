import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Util implements Serializable {
	private static final long serialVersionUID = 3402846080434434909L;
	
	private ArrayList<String> logs;
	
	public Util() {
		logs = new ArrayList<String>();
	}
		
	
	private static final int CLIENT_CODE_STACK_INDEX;
	static {
		int i = 0;
		for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			i++;
			if (ste.getClassName().equals(Util.class.getName())) {
				break;
			}
		}
		CLIENT_CODE_STACK_INDEX = i;
	}

	
	private static SimpleDateFormat timeFormatter= new SimpleDateFormat("hh:mm:ss a");
	void Log(String message) {
		Date date = new Date();
		String sender = Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
		String time = timeFormatter.format(date);

		String log = "[" + sender + "@" + time +"]: " + message;
		
		System.out.println(log);
		logs.add(log);
	}
}

