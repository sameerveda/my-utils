package sam.logging;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;

import java.time.LocalDateTime;
import java.util.logging.LogManager;
import java.util.logging.Logger;

//import sam.logging.LogFilter;

public final class MyLoggerFactory {
	private static final boolean loggernameSimple;

	static {
		/*
		 		String key = "java.util.logging.config.file";
		String file = System.getProperty(key);

		if(file == null) {
			Predicate<String> t = k -> {
				String s = lookup(k);
				if(s != null)
					System.setProperty(key, s);
				return s != null;
			};
			boolean b = t.test(key);
			b = b || t.test("logging.config.file");
			b = b || t.test("sam.logging.file");
		}
		 */

		String s2 = LogManager.getLogManager().getProperty("sam.logging.loggerName.format");
		boolean  result = false;
		if(s2 != null) {
			switch (s2.toLowerCase().trim()) {
				case "simple":
					result = true;
					break;
				case "full":
					result = false;
					break;
				default:
					Logger.getLogger(MyLoggerFactory.class.getCanonicalName()).severe("unknown value for \"sam.logging.loggerName.format\": "+s2+", settting to: full");
					result = false;
					break;
			}
		}
		loggernameSimple = result;

		/*
		 		if(file == null)
			logger(MyLoggerFactory.class).fine("java.util.logging.config.file="+System.getProperty(key));
		 */


		logger(MyLoggerFactory.class).config(() -> "----------- DateTime: "+LocalDateTime.now()+" ----------");
	}
	@SuppressWarnings("unused")
	private  static String lookup(String key) {
		String s = getProperty(key);
		if(s != null) return s;

		s = getenv(key);
		return s;
	}

	MyLoggerFactory() {}

	public static Logger logger(@SuppressWarnings("rawtypes") Class cls) {
		return Logger.getLogger(loggernameSimple ? cls.getSimpleName() : cls.getCanonicalName()); 
	}
	public static Logger logger(String loggerName) {
		return Logger.getLogger(loggerName); 
	}
}
