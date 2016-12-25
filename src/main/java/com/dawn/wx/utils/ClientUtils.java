package com.dawn.wx.utils;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by DawnHeaven on 16-12-25.
 */
public class ClientUtils implements ApplicationContextAware
{
    private static final Logger LOGGER = Logger.getLogger(ClientUtils.class);

    private static ApplicationContext appContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        appContext = applicationContext;
    }

    /**
     * 获取Web应用程序上下文环境（ApplicationContext）
     *
     * @return ApplicationContext实例
     */
    public static ApplicationContext getApplicationContext()
    {
        return appContext;
    }

    /**
     * 获取web根目录实际系统路径
     *
     * @return 根目录实际路径
     */
    public static String getRootRealPath()
    {
        return getRealPath("");
    }

    /**
     * 获取指定web url目录的实际系统路径
     *
     * @param url web url，如果为空则默认为“/”
     * @return 根目录实际路径
     */
    public static String getRealPath(String url)
    {
        if (StringUtils.isEmpty(url)) url = "/";
        return ((XmlWebApplicationContext) appContext).getServletContext().getRealPath(url);
    }

    /**
     * 获取ContextPath
     *
     * @return ContextPath
     */
    public static String getContextPath()
    {
        /*try
        {
            return RequestContext.getRequest().getContextPath();
        }
        catch (Exception e)
        {
            return LocalSetting.getSetting("app.js.contextpath");
        }*/

        return null;
    }

    /**
     * 将异常中的堆栈信息转换成字符串
     *
     * @param ex 异常实例
     * @return 堆栈信息字符串
     */
    public static String stringStackTrace(Throwable ex)
    {
        if (ex == null) return "";
        StringWriter _sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(_sw));
        return _sw.toString();
    }

    /**
     * 根据class名称来获取Class（使用规则扩展的类加载器，可以载入规则扩展路径中的Class）
     *
     * @param className Class name
     * @return Class
     * @throws ClassNotFoundException
     */
    public static Class<?> getClass(String className) throws ClassNotFoundException
    {
        return Class.forName(className.trim());
    }

    /**
     * 将指定的class（className）实例化为指定的类型(targetClass)实例
     *
     * @param targetClass 实例的目标类型
     * @param className   要实例化的类名，默认为targetClass.getName()
     * @param <T>         实例，泛型
     * @return 按照目标类型实例化后的实例对象
     */
    public static <T> T getNewInstance(Class<T> targetClass, String className)
    {
        Class _cls = null;
        String _className = StringUtils.isBlank(className) ? targetClass.getName() : className;
        try
        {
            _cls = getClass(_className);
        }
        catch (ClassNotFoundException cnfex)
        {
            LOGGER.error("未找到Class[" + _className + "]");
        }
        if (_cls == null) return null;
        try
        {
            return targetClass.cast(_cls.newInstance());
        }
        catch (Exception ex)
        {
            LOGGER.warn("使用默认无参构造函数创建实例时出错！[" + ex.getMessage() + "]");
        }
        return null;
    }

    /**
     * 将指定的class实例化
     *
     * @param targetClass 实例的目标类型
     * @param <T>         实例，泛型
     * @return 实例对象
     */
    public static <T> T getNewInstance(Class<T> targetClass)
    {
        return getNewInstance(targetClass, "");
    }

    /**
     * 将原始数据类型class转换为封装数据类型class（非原始数据类型不转换）
     *
     * @param cls 要转换的class
     * @return 转换后的class
     */
    public static Class convPrimitiveClass(Class cls)
    {
        if (cls == null || !cls.isPrimitive()) return cls;
        Class _rtCls = null;
        if (cls.equals(int.class)) _rtCls = Integer.class;
        else if (cls.equals(float.class)) _rtCls = Float.class;
        else if (cls.equals(double.class)) _rtCls = Double.class;
        else if (cls.equals(short.class)) _rtCls = Short.class;
        else if (cls.equals(long.class)) _rtCls = Long.class;
        else if (cls.equals(boolean.class)) _rtCls = Boolean.class;
        else if (cls.equals(byte.class)) _rtCls = Byte.class;
        else if (cls.equals(char.class)) _rtCls = Character.class;
        else if (cls.equals(void.class)) _rtCls = Void.class;
        return _rtCls;
    }


    /**
     * 获取指定class中所有字段（包括父类、私有字段）
     *
     * @param clazz 指定的class
     * @return Field数组
     */
    public static Field[] getAllFieldsInClass(Class clazz)
    {
        if (clazz.isInterface() || clazz.getName().equals(Object.class.getName())) return new Field[0];
        List<Field> _fields = new ArrayList<Field>();
        _fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null)
            appendAllToCollectionNoRepeat(_fields, Arrays.asList(getAllFieldsInClass(clazz.getSuperclass())), new Comparator<Field>()
            {
                public int compare(Field o1, Field o2)
                {
                    try
                    {
                        return o1.getName().equals(o2.getName()) ? 0 : 1;
                    }
                    catch (Exception ex)
                    {
                        return 1;
                    }
                }
            });
        return _fields.toArray(new Field[_fields.size()]);
    }

    /**
     * 获取指定class中所有的public方法（包括父类）
     *
     * @param clazz    指定的class
     * @param prefixes 方法前缀字符串（多个前缀用逗号分隔），如果非空则只返回方法名中含这些前缀的方法
     *                 （如prefixes="get,is"，则只返回该Class中所有的get和is开头的方法）
     * @return Method数组
     */
    public static Method[] getAllMethodsInClass(Class clazz, String prefixes)
    {
        if (clazz == null || clazz.getName().equals(Object.class.getName())) return new Method[0];
        List<Method> _methods = new ArrayList<Method>();
        Method[] _ms = clazz.getMethods(); //获取该方法所有的public方法
        if (StringUtils.isBlank(prefixes)) _methods.addAll(Arrays.asList(_ms));
        else //如果指定的方法前缀非空，则对methods进行过滤
        {
            List<String> _prefixes = Arrays.asList(prefixes.trim().split(","));
            for (Method _m : _ms)
                for (String _prefix : _prefixes)
                {
                    if (StringUtils.isBlank(_prefix) || !_m.getName().startsWith(_prefix.trim())) continue;
                    _methods.add(_m);
                    break;
                }
        }
        if (clazz.getSuperclass() != null)
            appendAllToCollectionNoRepeat(_methods, Arrays.asList(getAllMethodsInClass(clazz.getSuperclass(), prefixes)), new Comparator<Method>()
            {
                public int compare(Method o1, Method o2)
                {
                    try
                    {
                        return o1.getName().equals(o2.getName()) ? 0 : 1;
                    }
                    catch (Exception ex)
                    {
                        return 1;
                    }
                }
            });
        return _methods.toArray(new Method[_methods.size()]);
    }

    /**
     * 根据Method及指定的方法前缀生成对应的字段名
     *
     * @param method   Method对象
     * @param prefixes 方法前缀字符串（多个前缀用逗号分隔），
     *                 如果为空，则尝试将Method.getName()中首个大写字母开始的字符串作为字段名，
     *                 否则，则根据指定的方法前缀截取获得字段名
     * @return 生成的对应字段名
     */
    public static String genFieldNameFromMethod(Method method, String prefixes)
    {
        if (method == null) return "";
        String _methodName = method.getName();
        String _fieldName = "";
        if (StringUtils.isBlank(prefixes))  //前缀为空
        {
            int _upperIdx = 0;  //首个大写字符位置
            for (; _upperIdx < _methodName.length(); _upperIdx++)
                if (Character.isUpperCase(_methodName.charAt(_upperIdx))) break;
            if (_upperIdx == _methodName.length()) return _methodName;  //未找到大写字母，将methodname返回
            _fieldName = _methodName.substring(_upperIdx);  //首个大写字母开始的字符串作为字段名
        }
        else    //前缀非空
        {
            for (String _prefix : Arrays.asList(prefixes.trim().split(",")))
            {
                if (StringUtils.isBlank(_prefix) || !_methodName.startsWith(_prefix)) continue;
                _fieldName = _methodName.substring(_prefix.length());   //截掉前缀之后的字符串作为字段名
                break;
            }
        }
        if (StringUtils.isBlank(_fieldName)) return "";
        return Character.toLowerCase(_fieldName.charAt(0)) + (_fieldName.length() == 1 ? "" : _fieldName.substring(1));   //将字段名的第一个字符转换为小写
    }


    /**
     * 将对象追加到目标Collection中，保证Collection中无重复对象（使用Comparable接口进行比较）
     *
     * @param tarCol 目标集合
     * @param ele    要添加的对象
     * @param <T>    必须为实现了Comparable接口的类型，调用compareTo来判断对象是否相同
     * @return 追加是否成功
     */
    public static <T extends Comparable<T>> boolean appendToCollectionNoRepeat(Collection<T> tarCol, T ele)
    {
        if (tarCol == null || ele == null) return false;
        for (T _t : tarCol)
            if (_t.compareTo(ele) == 0) return false;
        return tarCol.add(ele);
    }

    /**
     * 将对象追加到目标Collection中，保证Collection中无重复对象（使用Comparator对象进行比较）
     *
     * @param tarCol     目标集合
     * @param ele        要添加的对象
     * @param comparator Comparator实例
     * @param <T>        类型变量
     * @return 追加是否成功
     */
    public static <T> boolean appendToCollectionNoRepeat(Collection<T> tarCol, T ele, Comparator<T> comparator)
    {
        if (tarCol == null || ele == null || comparator == null) return false;
        for (T _t : tarCol)
            if (comparator.compare(_t, ele) == 0) return false;
        return tarCol.add(ele);
    }

    /**
     * 将一个Collection中对象全部追加到目标Collection中，保证目标Collection中无重复对象（使用Comparable接口进行比较）
     *
     * @param tarCol 目标集合
     * @param eleCol 要追加的集合
     * @param <T>    必须为实现了Comparable接口的类型，调用compareTo来判断对象是否相同
     * @return 追加成功对象的数目
     */
    public static <T extends Comparable<T>> int appendAllToCollectionNoRepeat(Collection<T> tarCol, Collection<T> eleCol)
    {
        int _count = 0;
        if (tarCol == null || eleCol == null) return _count;
        for (T _ele : eleCol)
            if (appendToCollectionNoRepeat(tarCol, _ele))
                _count++;
        return _count;
    }

    /**
     * 将一个Collection中对象全部追加到目标Collection中，保证目标Collection中无重复对象（使用Comparator对象进行比较）
     *
     * @param tarCol     目标集合
     * @param eleCol     要追加的集合
     * @param comparator Comparator实例
     * @param <T>        必须为实现了Comparable接口的类型，调用compareTo来判断对象是否相同
     * @return 追加成功对象的数目
     */
    public static <T> int appendAllToCollectionNoRepeat(Collection<T> tarCol, Collection<T> eleCol, Comparator<T> comparator)
    {
        int _count = 0;
        if (tarCol == null || eleCol == null || comparator == null) return _count;
        for (T _ele : eleCol)
            if (appendToCollectionNoRepeat(tarCol, _ele, comparator))
                _count++;
        return _count;
    }

    /**
     * 用指定的分隔符拼接任意多个字符串，如两个字符串之间的分隔符重复则仅保留一个分隔符
     *
     * @param separator 分隔符，默认为“,”
     * @param strs      待拼接的任意多个字符串
     * @return 拼接后的字符串
     */
    public static String jointStrings(String separator, String... strs)
    {
        if (strs == null || strs.length <= 1) return strs == null || strs.length == 0 ? "" : strs[0];
        if (StringUtils.isBlank(separator)) separator = ",";
        StringBuilder _sb = new StringBuilder("");
        for (String _str : strs)
        {
            if (StringUtils.isBlank(_str)) continue;
            String _s = _str;
            if (_s.startsWith(separator)) _s = _s.substring(separator.length());
            if (_s.endsWith(separator)) _s = _s.substring(0, _s.length() - separator.length());
            _sb.append(_s).append(separator);
        }
        return _sb.length() == 0 ? "" : _sb.substring(0, _sb.length() - separator.length());
    }

    /**
     * 将文件路径标准化，便于跨平台正常运行
     * Windows中路径分隔符为“\”，而在Linux/Unix中为“/”
     *
     * @param filePath 原始路径
     * @return 标准化后的路径
     */
    public static String normalizeFilePath(String filePath)
    {
        if (StringUtils.isBlank(filePath)) return "";
        return filePath.trim().replace("/", File.separator).replace("\\", File.separator);
    }

    /**
     * 注册https协议
     */
    public static void registerHttps()
    {
        Protocol.registerProtocol("https", new Protocol("https", new MySSLSocketFactory(), 443));
    }

    /**
     * 装配完整的url请求
     *
     * @param url    url请求
     * @param params 请求参数
     * @return 完整的url
     */
    public static String assembleUrl(String url, String params)
    {
        if (StringUtils.isBlank(url)) return "";
        String _url = url.trim();
        if (StringUtils.isBlank(params)) return _url;
        String _params = params.trim();
        if (_params.startsWith("?") || _params.startsWith("&")) _params = _params.substring(1);
        return _url.contains("?") ? _url + "&" + _params : _url + "?" + _params;
    }

    public static String changeInputStreamToString(InputStream in)
    {
        StringBuffer out = new StringBuffer();
        char[] b = new char[4096];
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            for (int n; (n = reader.read(b)) != -1; )
            {
                out.append(new String(b, 0, n));
            }
        }
        catch (Exception ex)
        {
            return "";
        }

        return out.toString();
    }

    /**
     * 发送GET请求
     *
     * @param url         请求地址url
     * @param params      请求参数
     * @param connTimeout 连接超时时间（毫秒），如果为null则默认为30s
     * @param readTimeout 读取超时时间（毫秒），如果为null则默认为60s
     * @return 请求响应内容，如果为null表示请求发生异常
     */
    public static String doGet(String url, String params, Integer connTimeout, Integer readTimeout)
    {
        String _url = assembleUrl(url, params);
        if (StringUtils.isBlank(_url)) return "";
        if (_url.startsWith("https://")) registerHttps();
        HttpClient _httpClient = new HttpClient();
        _httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connTimeout == null ? ClientConsts.URLCONN_CONNTIMEOUT_MS : connTimeout);
        _httpClient.getHttpConnectionManager().getParams().setSoTimeout(readTimeout == null ? ClientConsts.URLCONN_READTIMEOUT_MS : readTimeout);
        GetMethod _httpGet = new GetMethod(_url);
        try
        {
            _httpClient.executeMethod(_httpGet);
            return new String(_httpGet.getResponseBodyAsString().getBytes());
        }
        catch (Exception ex)
        {
            LOGGER.error("Error when send GET request to [" + _url + "]!\n", ex);
            return null;
        }
        finally
        {
            _httpGet.releaseConnection();
        }
    }

    /**
     * 发送GET请求
     *
     * @param url    请求地址url
     * @param params 请求参数
     * @return 请求响应内容，如果为null表示请求发生异常
     */
    public static String doGet(String url, String params)
    {
        return doGet(url, params, null, null);
    }

    /**
     * 发送POST请求
     *
     * @param url         请求地址url
     * @param params      需要发送的请求参数字符串
     * @param connTimeout 连接超时时间（毫秒），如果为null则默认为30s
     * @param readTimeout 读取超时时间（毫秒），如果为null则默认为60s
     * @return 请求响应内容，如果为null表示请求发生异常
     */
    public static String doPost(String url, String params, Integer connTimeout, Integer readTimeout)
    {
        String _url = assembleUrl(url, "");
        if (StringUtils.isBlank(_url)) return "";
        if (_url.startsWith("https://")) registerHttps();
        HttpClient _httpClient = new HttpClient();
        _httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(connTimeout == null ? ClientConsts.URLCONN_CONNTIMEOUT_MS : connTimeout);
        _httpClient.getHttpConnectionManager().getParams().setSoTimeout(readTimeout == null ? ClientConsts.URLCONN_READTIMEOUT_MS : readTimeout);
        PostMethod _httpPost = new PostMethod(_url);
        try
        {
            _httpPost.setRequestEntity(new StringRequestEntity(params, "application/json", "UTF-8"));
            _httpClient.executeMethod(_httpPost);
            return changeInputStreamToString(_httpPost.getResponseBodyAsStream());
        }
        catch (Exception ex)
        {
            LOGGER.error("Error when send POST request to [" + _url + "]\n", ex);
            return null;
        }
        finally
        {
            _httpPost.releaseConnection();
        }
    }

    /**
     * 发送POST请求
     *
     * @param url    请求地址url
     * @param params 需要发送的请求参数字符串
     * @return 请求响应内容，如果为null表示请求发生异常
     */
    public static String doPost(String url, String params)
    {
        return doPost(url, params, null, null);
    }

    /**
     * 获取“精确的”当前时间戳（精确到毫秒）
     * <p>
     * 在JAVA中Date.getTime()和System. currentTimeMillis()方法“看似”能获得精确到毫秒级别的时间戳，
     * 但是其实际返回的值却受限于不同的操作系统实现，并不一定能提供1ms的计时粒度，比如在Windows系统上它的粒度为15~16ms。
     * 所以如果客户端在“非常短”的时间内发出两条指令，则可能会被误以为是“同一条指令”而被排重！
     * 值的一提的是，虽然JDK1.5以上提供了System.nanoTime()方法，可以支持精确到纳秒（ns）级的计时，但此方法仅可用于计时，无法用于计算当前时间！
     * 因为nanoTime的返回值是从一个确定的值算起的，但是这个值是任意的，可能是一个未来的时间，所以返回值有可能是负数。
     * 所以就目前而言，只能使用System. currentTimeMillis()方法来获取“看似精确”到毫秒级的当前时间戳！
     * </p>
     *
     * @return “精确的”当前时间戳（毫秒）
     */
    public static long getCurrentTimeMillis()
    {
        //System.nanoTime()虽能提供纳秒级计时，但不能用于表示当前时间！
        //return System.nanoTime() / 1000000L;
        return System.currentTimeMillis();
    }

    /**
     * 获得本机所有有效的（非虚拟、回环、点对点、MAC为空的）网络接口（网卡）信息
     *
     * @param onlyUp 是否只获取开启并运行的网卡的MAC地址
     * @return 网络接口信息（NetworkInterface）实例
     */
    public static List<NetworkInterface> getLocalNetworkInterfaces(boolean onlyUp)
    {
        List<NetworkInterface> _rtList = new ArrayList<NetworkInterface>();
        try
        {
            Enumeration<NetworkInterface> _inters = NetworkInterface.getNetworkInterfaces();
            while (_inters.hasMoreElements())
            {
                NetworkInterface _inter = _inters.nextElement();
                if (_inter.isVirtual() || _inter.isLoopback() || _inter.isPointToPoint() || _inter.getHardwareAddress() == null || (onlyUp && !_inter.isUp()))
                    continue;
                _rtList.add(_inter);
            }
        }
        catch (Exception ex)
        {
            LOGGER.error("Error when get local network interfaces!", ex);
        }
        return _rtList;
    }

    /**
     * 从网络接口信息中提取MAC地址
     *
     * @param networkInterface 网络接口信息实体
     * @param separator        MAC地址分隔符，null则默认为“-”
     * @return MAC地址
     */
    public static String getMacAddress(NetworkInterface networkInterface, String separator)
    {
        if (networkInterface == null) return "";
        String _separator = separator == null ? "-" : separator;
        try
        {
            byte[] _macBytes = networkInterface.getHardwareAddress();
            if (_macBytes == null) return "";
            StringBuffer _sbf = new StringBuffer("");
            for (int i = 0; i < _macBytes.length; i++)
            {
                if (i > 0) _sbf.append(_separator);
                String _str = Integer.toHexString(_macBytes[i] & 0xff);
                _sbf.append(_str.length() == 1 ? "0" + _str : _str);
            }
            return _sbf.toString();
        }
        catch (SocketException ex)
        {
            LOGGER.info("Error when get MAC address from NetworkInterface!", ex);
        }
        return "";
    }

    /**
     * 读取指定文件的内容以字节数组形式返回
     *
     * @param file 要读取的File实例
     * @return 字节数组，如果为null则表示读取失败
     */
    public static byte[] readBytesFromFile(File file)
    {
        FileInputStream _fis = null;
        BufferedInputStream _bis = null;
        ByteArrayOutputStream _bos = null;
        byte[] _bytes = null;
        try
        {
            _fis = new FileInputStream(file);
            _bis = new BufferedInputStream(_fis);
            _bos = new ByteArrayOutputStream(1024);
            byte[] _buf = new byte[1024];
            int _len;
            while ((_len = _bis.read(_buf)) != -1)
                _bos.write(_buf, 0, _len);
            _bytes = _bos.toByteArray();
        }
        catch (IOException ex)
        {
            LOGGER.error("读取文件[" + file.getPath() + "]时发生异常！", ex);
        }
        finally
        {
            try
            {
                if (_bos != null) _bos.close();
            }
            catch (Exception ex)
            {
            }
            try
            {
                if (_bis != null) _bis.close();
            }
            catch (Exception ex)
            {
            }
            try
            {
                if (_fis != null) _fis.close();
            }
            catch (Exception ex)
            {
            }
        }
        return _bytes;
    }
}