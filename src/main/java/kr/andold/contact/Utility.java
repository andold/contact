package kr.andold.contact;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utility {
	public static final String BLANK = "";
	public static final String HR = repeat("─", 64);

	public static final String OFFSET_ID_KST = "+09:00";
	public static final ZoneId ZONE_ID_KST = ZoneId.of("Asia/Seoul");
	public static final ZoneOffset ZONE_OFFSET_KST = ZoneOffset.of(OFFSET_ID_KST);
	public static final long MILLI_SECONDS_A_SECOND = 1000L;
	public static final long MILLI_SECONDS_A_MINUTE = MILLI_SECONDS_A_SECOND * 60L;
	public static final long MILLI_SECONDS_AN_HOUR = MILLI_SECONDS_A_MINUTE * 60L;
	public static final long MILLI_SECONDS_A_DAY = MILLI_SECONDS_AN_HOUR * 24L;
	public static final long MILLI_SECONDS_AN_YEAR = MILLI_SECONDS_A_DAY * 365L + MILLI_SECONDS_A_DAY / 4L;

	public static final double ONE_SOLAR_YEAR = 31556926.08d;
	public static final double UNIVERSE_AGE = 13799000000.0d * ONE_SOLAR_YEAR;

	private static final String LIST_DATE_FORMAT[] = {
		"EEE, d MMM yyyy HH:mm:ss z"	//	Fri, 26 Jan 2018 06:37:57 GMT
		, "yyyy-MM-dd'T'HH:mm:ss.SSSZ"	//	2012-04-23T04:43:40.000+0000
		, "yyyy-MM-ddHH:mm:ss"			//	2022-03-0419:13:52
		, "yyyy-MM-dd HH:mm:ss.SSS"
		, "yyyy-MM-dd(E) HH:mm:ss"		//	2003-08-26(화) 10:35:28
		, "yyyy/MM/dd HH:mm:ss[E]"		//	2021/08/16 11:00:53[월]
		, "yyyy-MM-dd (E) HH:mm"		//	2022-03-11 (금) 18:26 
		, "yyyy-M-d a h:m:s"			//	2009-10-19 오전 11:30:00
		, "yy-MM-dd HH:mm:ss"
		, "yyyy.MM.dd'T'HH:mm:ss"
		, "yyyy.MM.dd (HH:mm:ss)"		//	2023.03.13 (14:02:18)
		, "yyyy.MM.dd HH:mm:ss"
		, "yyyy.MM.dd a h:mm"			//	『2023.04.05』, 『오후』, 『5:57』
		, "yyyy년 MM월 dd일 (E) a h:m"		//	2022년 11월 13일 (일) 오후 2:54  
		, "yyyy년 M월 d일 (E) a h:m"		//	2023년 4월 5일 (수) 오후 5:57
		, "yyyy년 MM월 dd일(E) HH시 mm분"	//	2022년 3월 16일(수) 17시 32분
		, "yyyy-MM-dd HH:mm"
		, "yyyy년 MM월 dd일 HH:mm"
		, "yyyy년MM월dd일 HH:mm"
		, "yyyy.MM.dd HH:mm"
		, "yyyy.MM.dd"
		, "yyyy/MM/dd HH:mm"			//	2021/02/14 16:31
		, "yyyy/MM/dd"					//	2021/02/14
		, "yyyy-MM-dd"
		, "yyyyMMdd'T'HHmmssZ"			//	20190419T134908Z
		, "yyyyMMdd'T'HHmmss"			//	20190419T134908
		, "yyyyMMdd"					//	20110320
		, "yyyy년MM월분 dd일"			//  2020년6월분
		, "yyyy년MM월dd일"
		, "yy/MM/dd HH:mm"
		, "yy.MM"						//	22.12
		, "yyyy년 MM월"					//	20년 1월
	};
	private static final int SKIP_COUNT_LINE_LOGGING_IN_FILE_READ = 1024 * 64;
	private static final String INDENT_DEFAULT = "  ";
	private static final String[] ARRAY_INDENT = {"", INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT,
		INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT,
		INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT, INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT,
		INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT + INDENT_DEFAULT};
	private static final String[] ARRAY_INDENT_PREFIX = {"", "0000", "1111", "2222", "3333"};
	public static final String ARRAY_INDENT_THREAD = "▒■□▣▤▥▦▧▨▩▲△▶▷▼▽◀◁◆◇◈○◎●◐◑★☆☎☏☜☞♀♂♠♡♣♤♥♧♨♩♪♬♭";
	//	블록요소, 도형								  ▒■□▣▤▥▦▧▨▩▱▲△▵▶▷▹▼▽▿◀◁◃◆◇◈◉◊○◌◎●◐◑◦◯
	//	여러가지기호, 딩뱃기호						  ★☆☎☏☜☝☞☟☯♀♂♠♡♣♤♥♧♨♩♪♬♭♯✖✚✽❖❶❷❸❹❺❻❼❽❾❿➊➋➌➍➎➏➐➑➒➓
	//	result										  ▒■□▣▤▥▦▧▨▩?▲△?▶▷?▼▽?◀◁?◆◇◈??○?◎●◐◑??★☆☎☏☜?☞??♀♂♠♡♣♤♥♧♨♩♪♬♭?????????????????????????

	@SuppressWarnings("unused")
	private static final String METHOD_LABEL = "┍┕├    「」『』【】    []‘’‛“”‟‹›";
	
	public static int size(List<?> list) {
		return (list == null) ? -1 : list.size();
	}

	public static int size(Map<?, ?> map) {
		return (map == null) ? -1 : map.size();
	}

	public static int size(MultipartFile file) {
		return (int)((file == null) ? -1 : file.getSize());
	}

	public static int size(Page<?> page) {
		return (page == null) ? -1 : size(page.getContent());
	}

	public static int size(Object[] types) {
		return (types == null) ? -1 : types.length;
	}

	public static Date parseDateTime(String string) {
		if (string == null) {
			return null;
		}

		for (int cx = 0; cx < LIST_DATE_FORMAT.length; cx++) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LIST_DATE_FORMAT[cx], Locale.KOREA);
				Date date = simpleDateFormat.parse(string);
				if (date != null) {
					return date;
				}
			} catch (Exception e) {
			}
		} //	for (int cx = 0; cx < listDateFormat.length; cx++) {

		//	if time-milli-seconds
		Long timestamp = Utility.parseLong(string, -1L);
		if (timestamp >= 0) {
			return new Date(timestamp);
		}

		return null;
	}

	public static Date parseDateTime(String string, long defaultValue) {
		if (string == null) {
			return new Date(defaultValue);
		}

		for (int cx = 0; cx < LIST_DATE_FORMAT.length; cx++) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LIST_DATE_FORMAT[cx], Locale.KOREA);
				Date date = simpleDateFormat.parse(string);
				if (date != null) {
					return date;
				}
			} catch (Exception e) {
			}
		} //	for (int cx = 0; cx < listDateFormat.length; cx++) {

		//	if time-milli-seconds
		Long timestamp = Utility.parseLong(string, defaultValue);
		return new Date(timestamp);
	}

	public static Date parseDateTime(String string, Date defaultDateStart) {
		if (string == null) {
			return defaultDateStart;
		}

		for (int cx = 0; cx < LIST_DATE_FORMAT.length; cx++) {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(LIST_DATE_FORMAT[cx], Locale.KOREA);
				Date date = simpleDateFormat.parse(string);
				if (date != null) {
					return date;
				}
			} catch (Exception e) {
			}
		} //	for (int cx = 0; cx < listDateFormat.length; cx++) {

		//	if time-milli-seconds
		Long timestamp = Utility.parseLong(string, null);
		if (timestamp == null) {
			return defaultDateStart;
		}

		return new Date(timestamp);
	}

	public static Date parseDateTime(String[] args) {
		if (args == null) {
			return null;
		}

		String string = "";
		for (int cx = 0; cx < args.length; cx++) {
			if (args[cx] == null) {
				continue;
			}

			string += (args[cx].replaceAll("[ \t\r\n\",]+", " ") + " ");
		}

		return parseDateTime(string.replaceAll("[ ]+  ", " ").trim());
	}

	public static Integer parseInteger(String string) {
		return parseInteger(string, null);
	}

	public static Integer parseInteger(String string, Integer defaultInteger) {
		if (string == null) {
			return defaultInteger;
		}

		try {
			return Integer.parseInt(string.trim().replaceAll("((,)|(\\.[0-9]*))", ""));
		} catch (Exception e) {
		}

		return defaultInteger;
	}

	public static Long parseLong(String string, Long defaultLong) {
		if (string == null) {
			return defaultLong;
		}

		try {
			return Long.parseLong(string.trim().replaceAll("((,)|(\\.[0-9]*))", ""));
		} catch (Exception e) {
		}

		return defaultLong;
	}
	public static Long parseLong(String string) {
		return parseLong(string, null);
	}

	public static String concat(String string, String... args) {
		StringBuffer stringBuffer = new StringBuffer("");
		if(string != null) {
			stringBuffer.append(string.replaceAll("[　\n\"]+", "").trim());
		}
		
		if(args == null || args.length == 0) {
			return new String(stringBuffer).trim();
		}
		
		for (String arg : args) {
			if(arg == null || arg.trim().isEmpty()) {
				continue;
			}
			
			stringBuffer.append(arg.replaceAll("[　]+", "").trim());
		}

		return new String(stringBuffer).trim();
	}
	public static Double parseDouble(String string, String... args) {
		if (string == null) {
			return 0.0;
		}

		String concat = concat(string, args);
		try {
			return Double.parseDouble(concat);
		} catch (Exception e) {
		}

		return 0.0;
	}
	public static <T> List<T> parseJsonLines(String filename, Class<T> classParameter, int max) {
		long started = System.currentTimeMillis();
		List<T> list = new ArrayList<T>();
		String fullPath = scanClassPath(filename);
		if (fullPath == null) {
			return null;
		}

		try {
			File file = new File(fullPath);

			if (!file.exists()) {
				log.warn("{} {} 존재하지 않음", indentMiddle(), fullPath);
				return null;
			}

			try {
				BufferedReader inFile = new BufferedReader(new FileReader(file));
				String line;
				int lineNumber = 0;
				ObjectMapper objectMapper = new ObjectMapper();
				while ((line = inFile.readLine()) != null) {
					lineNumber++;
					if (lineNumber > max) {
						log.info("{} {} - line number: {} - lines limit :: {}", indentMiddle(), filename, lineNumber, max);
						break;
					}

					if (lineNumber % SKIP_COUNT_LINE_LOGGING_IN_FILE_READ == 0) {
						log.info("{} {} - line number: {} - {}", indentMiddle(), filename, lineNumber, line);
					}
					objectMapper.setSerializationInclusion(Include.NON_NULL);
					objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
					objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
					T the = objectMapper.readValue(line, classParameter);
					list.add(the);
				}

				inFile.close();
				log.info("{} found {} - #{} data - {} seconds", indentEnd(), filename, list.size(), (System.currentTimeMillis() - started) / 1000);
				return list;
			} catch (Exception e) {
				e.printStackTrace();
				log.error("{} Exception:: {} - parseJsonLines({}, {})", indentEnd(), e.getMessage(), filename, classParameter);
			}
		} catch (Exception e) {
			log.error("{} Exception:: {} - parseJsonLines({}, {})", indentEnd(), e.getMessage(), filename, classParameter);
			return null;
		}

		return list;
	}

	private static String scanClassPath(String filename) {
		String javaClassPath = System.getProperty("java.class.path");
		String[] listJavaClassPath = javaClassPath.split("[;]");

		for (int cx = 0; cx < listJavaClassPath.length; cx++) {
			String fullPath = String.format("%s/%s", listJavaClassPath[cx], filename);
			File file = new File(fullPath);
			if (!file.exists()) {
				log.trace("{} 존재하지 않음", fullPath);
				continue;
			}

			return fullPath;
		}

		return null;
	}

	public static String toStringTimeReadable(long time) {
		if (time > (MILLI_SECONDS_A_DAY * 10L)) {
			//	10일 이상인 경우에는 일단위로
			return String.format("%d days", time / MILLI_SECONDS_A_DAY);
		} else if (time > (MILLI_SECONDS_AN_HOUR * 10L)) {
			//	10시간 이상인 경우에는 시간단위로
			return String.format("%d hours", time / MILLI_SECONDS_AN_HOUR);
		} else if (time > (MILLI_SECONDS_A_MINUTE * 10L)) {
			//	10분 이상인 경우에는 분단위로
			return String.format("%d minutes", time / MILLI_SECONDS_A_MINUTE);
		} else if (time > (MILLI_SECONDS_A_SECOND * 10L)) {
			//	10초 이상인 경우에는 초단위로
			return String.format("%d seconds", time / MILLI_SECONDS_A_SECOND);
		}

		return String.format("%d ms", time);
	}

	public static String toStringTimestamp(long millis) {
		try {
			Instant instant = Instant.ofEpochMilli(millis);
			ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZONE_ID_KST);
			if (zonedDateTime.getSecond() != 0) {
				return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss zzz", Locale.KOREAN));
			}
			if (zonedDateTime.getMinute() != 0) {
				return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzz", Locale.KOREAN));
			}
			if (zonedDateTime.getHour() != 0) {
				return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm zzz", Locale.KOREAN));
			}
			return zonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd zzz", Locale.KOREAN));
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	public static String toStringJson(Object object) {
		if (object == null) {
			return "";
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	public static String toStringJson(Object object, int max) {
		String string = toStringJson(object);
		return ellipsis(string, max);
	}

	public static String toStringJson(Object object, int left, int right) {
		String string = toStringJson(object);
		return ellipsis(string, left, right);
	}
	public static String toStringJsonPretty(Object object) {
		if (object == null) {
			return "";
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setSerializationInclusion(Include.NON_NULL);
			objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
			objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
		} catch (Exception e) {
			return e.getMessage();
		}
	}


	public static String toStringQuery(Object object, String exclude) {
		String string = "";
		if (object == null) {
			return string;
		}

		Method[] methods = object.getClass().getMethods();
		if (methods == null) {
			return string;
		}

		Map<String, Method> mapSetter = new HashMap<String, Method>();
		for (Method method : methods) {
			if (method == null || method.getParameterCount() != 1) {
				continue;
			}

			String name = method.getName();
			if (name == null || !name.startsWith("set")) {
				continue;
			}

			if (name.substring(3).equalsIgnoreCase(exclude)) {
				continue;
			}

			mapSetter.put(name.substring(3), method);
		}

		for (Method getter : methods) {
			if (getter == null || getter.getParameterCount() > 0) {
				continue;
			}

			String name = getter.getName();
			if (name == null || !name.startsWith("get")) {
				continue;
			}

			String key = name.substring(3);
			Method setter = mapSetter.get(key);
			if (setter == null) {
				continue;
			}

			try {
				Object value = getter.invoke(object);
				if (value == null) {
					//	value is null
					continue;
				}

				//	value is not null
				string += (key.substring(0, 1).toLowerCase() + key.substring(1) + "=" + value + "&");
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		int length = string.length();
		if (length > 1) {
			//	last character & remove
			string = string.substring(0, length - 1);
		}

		return string;
	}

	public static String toStringPastTimeReadable(long started) {
		return toStringTimeReadable(System.currentTimeMillis() - started);
	}

	public static String toStringPastTimeReadable(ZonedDateTime zonedDateTime) {
		ZonedDateTime now = ZonedDateTime.now();

		if (ChronoUnit.DAYS.between(zonedDateTime, now) > 10L) {
			//	10일 이상인 경우에는 일단위로
			return String.format("%d days", ChronoUnit.DAYS.between(zonedDateTime, now));
		} else if (ChronoUnit.HOURS.between(zonedDateTime, now) > 10L) {
			//	10시간 이상인 경우에는 시간단위로
			return String.format("%d hours", ChronoUnit.HOURS.between(zonedDateTime, now));
		} else if (ChronoUnit.MINUTES.between(zonedDateTime, now) > 10) {
			//	10분 이상인 경우에는 분단위로
			return String.format("%d minutes", ChronoUnit.MINUTES.between(zonedDateTime, now));
		} else if (ChronoUnit.SECONDS.between(zonedDateTime, now) > 10) {
			//	10초 이상인 경우에는 초단위로
			return String.format("%d seconds", ChronoUnit.SECONDS.between(zonedDateTime, now));
		}

		return String.format("%d ms", ChronoUnit.MILLIS.between(zonedDateTime, now));
	}

	private static String extractStringFromHtmlElement(Element element) {
		if (element == null) {
			return null;
		}

		String string = "";
		Elements elements = element.children();
		string += extractStringFromHtmlElement(elements);
		if (element.hasText() && elements.isEmpty()) {
			string += element.text();
		}

		switch (element.tag().getName().toLowerCase()) {
			case "td":
			case "th":
				string += "\t";
				break;
			case "tr":
				string += "\n";
				break;
			default:
				break;
		}

		return string;
	}

	private static String extractStringFromHtmlElement(Elements elements) {
		if (elements == null || elements.isEmpty()) {
			return "";
		}

		String string = "";
		for (int cx = 0, sizex = elements.size(); cx < sizex; cx++) {
			string += extractStringFromHtmlElement(elements.get(cx));
		}

		return string;
	}

	public static String extractStringFromText(File file) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			return extractStringFromText(bufferedReader);
		} catch (Exception e) {
			log.error("Exception:: {}", e.getMessage(), e);
		}

		return null;
	}

	public static String extractStringFromText(InputStream inputStream) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
			return extractStringFromText(bufferedReader);
		} catch (UnsupportedEncodingException e) {
			log.error("UnsupportedEncodingException:: {}", e.getLocalizedMessage(), e);
		}

		return "";
	}

	private static String extractStringFromText(BufferedReader bufferedReader) {
		try {
			String line = null;
			StringBuffer stringBuffer = new StringBuffer();
			int lineno = 0;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
				stringBuffer.append("\n");
				if (lineno++ % 1024 == 0) {
					log.debug("{} {}:	{}", Utility.indentMiddle(), lineno, line);
				}
			}
			bufferedReader.close();

			return new String(stringBuffer);
		} catch (Exception e) {
			log.warn("{} Exception:: {}", Utility.indentMiddle(), e.getMessage(), e);
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public static int compare(Object left, Object right) {
		if (left == null && right == null) {
			return 0;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}

		if (left instanceof Comparable<?> && right instanceof Comparable<?>) {
			return ((Comparable<Comparable<?>>)left).compareTo((Comparable<?>)right);
		}

		return 0;
	}

	public static int compareDate(Date left, Date right) {
		if (left == null && right == null) {
			return 0;
		}
		if (left == null) {
			return -1;
		}
		if (right == null) {
			return 1;
		}

		return left.compareTo(right);
	}

	public static boolean equals(Date left, Date right, long tolerance) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null) {
			return false;
		}
		if (right == null) {
			return false;
		}

		return Math.abs(left.getTime() - right.getTime()) < tolerance;
	}

	public static boolean equals(Integer left, Integer right, double tolerance) {
		if (left == null && right == null) {
			return true;
		}
		if (left == null) {
			return false;
		}
		if (right == null) {
			return false;
		}

		return Math.abs(left - right) < ((Math.abs(left) + Math.abs(right)) / 2.f * tolerance);
	}

	public static int intValue(Integer value, int defaultValue) {
		return (value == null) ? defaultValue : value.intValue();
	}

	public static int length(String string) {
		if (string == null) {
			return -1;
		}
		return string.length();
	}

	public static boolean copyPropertiesNotNull(Object source, Object target) {
		if (source == null || target == null) {
			return false;
		}

		Method[] sourceMethods = source.getClass().getMethods();
		Method[] targetMethods = target.getClass().getMethods();
		if (sourceMethods == null || targetMethods == null) {
			return false;
		}

		Map<String, Method> mapTargetSetter = new HashMap<String, Method>();
		Map<String, Method> mapTargetGetter = new HashMap<String, Method>();
		for (Method targetMethod : targetMethods) {
			if (targetMethod == null) {
				continue;
			}

			String name = targetMethod.getName();
			if (name == null) {
				continue;
			}

			if (name.startsWith("set") && targetMethod.getParameterCount() == 1) {
				mapTargetSetter.put(name.substring(3), targetMethod);
				continue;
			}

			if (name.startsWith("get") && targetMethod.getParameterCount() == 0) {
				mapTargetGetter.put(name.substring(3), targetMethod);
				continue;
			}

		}

		boolean dirty = false;
		for (Method getter : sourceMethods) {
			if (getter == null || getter.getParameterCount() > 0) {
				continue;
			}

			String name = getter.getName();
			if (name == null || !name.startsWith("get")) {
				continue;
			}

			String key = name.substring(3);
			Method targetSetter = mapTargetSetter.get(key);
			Method targetGetter = mapTargetGetter.get(key);
			if (targetSetter == null || targetGetter == null) {
				continue;
			}

			try {
				Object sourceValue = getter.invoke(source);
				if (sourceValue == null) {
					//	value is null
					continue;
				}

				Object targetValue = targetGetter.invoke(target);
				if (Utility.compare(sourceValue, targetValue) == 0) {
					//	value is equal
					continue;
				}

				//	value is not null and not equal

				targetSetter.invoke(target, sourceValue);
				dirty = true;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		return dirty;
	}
	public static boolean copyPropertiesTargetNull(Object source, Object target) {
		if (source == null || target == null) {
			return false;
		}

		Method[] sourceMethods = source.getClass().getMethods();
		Method[] targetMethods = target.getClass().getMethods();
		if (sourceMethods == null || targetMethods == null) {
			return false;
		}

		Map<String, Method> mapTargetSetter = new HashMap<String, Method>();
		Map<String, Method> mapTargetGetter = new HashMap<String, Method>();
		for (Method targetMethod : targetMethods) {
			if (targetMethod == null) {
				continue;
			}

			String name = targetMethod.getName();
			if (name == null) {
				continue;
			}

			if (name.startsWith("set") && targetMethod.getParameterCount() == 1) {
				mapTargetSetter.put(name.substring(3), targetMethod);
				continue;
			}

			if (name.startsWith("get") && targetMethod.getParameterCount() == 0) {
				mapTargetGetter.put(name.substring(3), targetMethod);
				continue;
			}

		}

		boolean dirty = false;
		for (Method getter : sourceMethods) {
			if (getter == null || getter.getParameterCount() > 0) {
				continue;
			}

			String name = getter.getName();
			if (name == null || !name.startsWith("get")) {
				continue;
			}

			String key = name.substring(3);
			Method targetSetter = mapTargetSetter.get(key);
			Method targetGetter = mapTargetGetter.get(key);
			if (targetSetter == null || targetGetter == null) {
				continue;
			}

			try {
				Object sourceValue = getter.invoke(source);
				Object targetValue = targetGetter.invoke(target);
				if (targetValue != null) {
					//	target value is not null
					continue;
				}

				if (Utility.compare(sourceValue, targetValue) == 0) {
					//	value is equal
					continue;
				}

				targetSetter.invoke(target, sourceValue);
				dirty = true;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		return dirty;
	}

	public static String readClassPathFile(String filename) {
		log.info("{} readClassPathFile(『{}』)", Utility.indentStart(), filename);
		long started = System.currentTimeMillis();

		String javaClassPath = System.getProperty("java.class.path");
		String[] listJavaClassPath = javaClassPath.split("[;]");

		for (int cx = 0; cx < listJavaClassPath.length; cx++) {
			String fullPath = String.format("%s/%s", listJavaClassPath[cx], filename);
			File file = new File(fullPath);
			if (!file.exists()) {
				continue;
			}

			String string = extractStringFromText(file);

			log.info("{} 『{}』 readClassPathFile(『{}』) - {}", Utility.indentEnd(), toStringJson(string, 32, 32), filename, Utility.toStringPastTimeReadable(started));
			return string;
		}

		log.info("{} 『{}』 readClassPathFile(『{}』) - {}", Utility.indentEnd(), null, filename, Utility.toStringPastTimeReadable(started));
		return null;
	}

	public static String repeat(String word, int count) {
		if (word == null) {
			return null;
		}

		String string = "";
		for (int cx = 0; cx < count; cx++) {
			string += word;
		}

		return string;
	}

	public static String ellipsis(String string, int size) {
		if (string == null) {
			return null;
		}

		if (string.length() < size) {
			return string;
		}

		return string.substring(0, size - 3) + "...";
	}
	public static String ellipsisEscape(String string, int size) {
		return ellipsis(escape(string), size);
	}


	public static String ellipsis(String string, int left, int right) {
		if (string == null) {
			return "";
		}

		if (string.length() < (left + right)) {
			return string;
		}

		return string.substring(0, left) + "..." + string.substring(string.length() - right);
	}
	public static String ellipsisEscape(String string, int left, int right) {
		return ellipsis(escape(string), left, right);
	}

	public static Date newDate(Long from, long delta) {
		if (from == null) {
			return null;
		}

		return new Date(from.longValue() + delta);
	}

	public static Date newDate(Long from, int calendarType, int delta) {
		if (from == null) {
			return null;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(from.longValue());
		calendar.add(calendarType, delta);
		return calendar.getTime();
	}

	public static Date min(Date... args) {
		if (args == null || args.length == 0) {
			return null;
		}

		Date min = null;
		for (Date arg : args) {
			if (arg == null) {
				continue;
			}

			if (min == null) {
				min = arg;
				continue;
			}

			if (min.after(arg)) {
				min = arg;
				continue;
			}
		}

		return min;
	}

	public static int max(int... args) {
		if (args == null || args.length == 0) {
			return -1;
		}

		int max = Integer.MIN_VALUE;
		for (int arg : args) {
			if (max < arg) {
				max = arg;
				continue;
			}
		}

		return max;
	}

	public static Long max(Long... args) {
		if (args == null || args.length == 0) {
			return null;
		}

		Long max = null;
		for (Long arg : args) {
			if (arg == null) {
				continue;
			}

			if (max == null) {
				max = arg;
				continue;
			}

			if (max.longValue() < arg.longValue()) {
				max = arg;
				continue;
			}
		}

		return max;
	}

	public static Date max(Date... args) {
		if (args == null || args.length == 0) {
			return null;
		}

		Date max = null;
		for (Date arg : args) {
			if (arg == null) {
				continue;
			}

			if (max == null) {
				max = arg;
				continue;
			}

			if (max.before(arg)) {
				max = arg;
				continue;
			}
		}

		return max;
	}

	public static String toStringJsonLine(List<?> list) {
		if (list == null) {
			return "";
		}

		StringBuffer stringBuffer = new StringBuffer();
		for (int cx = 0, sizex = list.size(); cx < sizex; cx++) {
			Object object = list.get(cx);
			if (object == null) {
				continue;
			}

			stringBuffer.append(Utility.toStringJson(object));
			stringBuffer.append("\n");
		}

		return new String(stringBuffer);
	}

	public static String substring(String string, int max) {
		if (string == null) {
			return null;
		}

		if (string.length() < max) {
			return string;
		}

		return string.substring(0, max);
	}

	public static String indent() {
		Thread thread = Thread.currentThread();
		StackTraceElement[] stackTraceElements = thread.getStackTrace();
		int depth = 0;
		for (int cx = 0; cx < stackTraceElements.length; cx++) {
			StackTraceElement stackTraceElement = stackTraceElements[cx];
			String className = stackTraceElement.getClassName();
			if (className.startsWith("kr.andold.household")) {
				depth++;
			}
		}

		depth = max(0, depth - 2);
		int ea = depth / ARRAY_INDENT.length % ARRAY_INDENT_PREFIX.length;
		
		long threadId = Thread.currentThread().getId();
		return ARRAY_INDENT_THREAD.charAt((int)(threadId % ARRAY_INDENT_THREAD.length())) + ARRAY_INDENT_PREFIX[ea] + ARRAY_INDENT[depth % ARRAY_INDENT.length];
	}
	public static String indentStart() {	return indent() + "┍";	}
	public static String indentMiddle() {	return indent() + "┝";	}
	public static String indentEnd() {		return indent() + "┕";	}

	public static String readFile(MultipartFile file) throws UnsupportedEncodingException, IOException {
		log.debug("{} readFile({})", Utility.indentStart(), file);
		long started = System.currentTimeMillis();

		if (file == null) {
			log.warn("{} {} readFile(...) - {}", Utility.indentEnd(), null, Utility.toStringPastTimeReadable(started));
			return null;
		}

		String contentType = file.getContentType();
		String contents = null;
		if (contentType == null) {
			contents = extractStringFromText(file.getInputStream());
		} else if (contentType.contains("json")) {
			contents = extractStringFromText(file.getInputStream());
		}

		return contents;
	}

	public static String readFileFromText(MultipartFile multipartFile) {
		log.debug("{} readFile({})", Utility.indentStart(), multipartFile);
		long started = System.currentTimeMillis();

		if (multipartFile == null) {
			log.warn("{} {} readFile({}) - {}", Utility.indentEnd(), null, multipartFile, Utility.toStringPastTimeReadable(started));
			return null;
		}

		try {
			String string = Utility.extractStringFromText(multipartFile.getInputStream());
			log.debug("{} #{} readFile({}) - {}", Utility.indentEnd(), Utility.length(string), multipartFile, Utility.toStringPastTimeReadable(started));
			return string;
		} catch (IOException e) {
		}

		log.warn("{} {} readFile({}) - {}", Utility.indentEnd(), null, multipartFile, Utility.toStringPastTimeReadable(started));
		return null;
	}

	public static String ifNullThen(String string, String defaultValue) {
		return string == null ? defaultValue : string;
	}

	/**
	 * ESCAPED-CHAR = ("\\" / "\;" / "\," / "\N" / "\n")
	 */
	public static String escape(String escaped) {
		if (escaped == null) {
			return null;
		}
		
		return escaped.replaceAll("\n", "ⓝ").replaceAll("\t", "ⓣ");
	}
	public static String unescape(String escaped) {
		if (escaped == null) {
			return null;
		}
		
		return escaped.replace("ⓝ", "\n").replaceAll("ⓣ", "\t");
	}

	public static String escapeDQuoted(String text) {
		if (text == null) {
			return null;
		}
		
		if (text.startsWith("\"")) {
			text = text.substring(1);
		}

		if (text.endsWith("\"")) {
			text = text.substring(0, text.length() - 1);
		}

		return text;
	}

	public static boolean overlapDateTime(long startX, long endX, long startY, long endY) {
		return startX < endY && endX > startY;
	}

	public static String append(String... args) {
		String string = "";

		if (args == null || args.length == 0) {
			return string;
		}

		for (String arg : args) {
			if (arg == null) {
				continue;
			}

			string += arg;
		}

		return string;
	}

	public static int contains(String before, String after) {
		if (before == null || after == null) {
			return 0;
		}

		return (before.strip().contains(after.strip())) ? 1 : -1;
	}

	public static boolean isSameDay(Date date1, Date date2) {
		return isSameDay(date1.toInstant().atZone(ZoneId.systemDefault()), date2.toInstant().atZone(ZoneId.systemDefault()));
	}
	public static boolean isSameDay(ZonedDateTime left, ZonedDateTime right) {
		return left.truncatedTo(ChronoUnit.DAYS).compareTo(right.truncatedTo(ChronoUnit.DAYS)) == 0;
	}

	public static String toString(ZonedDateTime lastUpdated) {
		return lastUpdated.format(DateTimeFormatter.ISO_DATE_TIME);
	}

	public static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
