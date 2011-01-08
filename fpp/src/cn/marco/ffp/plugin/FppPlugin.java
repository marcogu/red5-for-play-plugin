package cn.marco.ffp.plugin;

import static cn.marco.ffp.plugin.FppController.ARG_KEY;
import static cn.marco.ffp.utils.PluginUtil.getListFromArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.compatibility.flex.messaging.messages.AcknowledgeMessage;
import org.red5.compatibility.flex.messaging.messages.AsyncMessage;
import org.red5.compatibility.flex.messaging.messages.CommandMessage;
import org.red5.compatibility.flex.messaging.messages.RemotingMessage;
import org.red5.io.object.Deserializer;
import org.red5.io.object.Serializer;
import org.red5.server.net.remoting.FlexMessagingService;
import org.red5.server.net.remoting.codec.RemotingProtocolDecoder;
import org.red5.server.net.remoting.codec.RemotingProtocolEncoder;
import org.red5.server.net.remoting.message.RemotingCall;
import org.red5.server.net.remoting.message.RemotingPacket;
import org.red5.server.service.Call;
import org.red5.server.service.ConversionUtils;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;
import play.utils.Java;
import cn.marco.ffp.annotations.Destination;

/**
 * @author Marco
 * 基于red5的play plugin
 * 0.1版 实现flex remoting 和 as3的基于netconnection的remoting调用
 * 
 * 期待功能：
 * TODO:  1：实现简单rtmp server
 * 		  2：实现rtmp server 的动态, 参考：japid的动态,Plugin.onClassesChange
 * 			 Plugin.compileAll,Plugin.detectChange,Plugin.enhance方法
 */
public class FppPlugin extends PlayPlugin {
	
	public static final String PACK_KEY = "fppPack";
	public static final String MSG_METHOD_NAME = "handleRequest";
	public static final String APPLICATION_AMF = "application/x-amf";
	
	public static final Deserializer deserializer = new Deserializer();
	public static final Serializer    serializer  = new Serializer();
	
	public static final String PREPARED_RESULT = "AcknowledgeMessage";
	
	public static RemotingProtocolDecoder decoder = new RemotingProtocolDecoder();
	public static RemotingProtocolEncoder encoder = new RemotingProtocolEncoder();
	
	private static FlexMessagingService messageService = new FlexMessagingService();
	
	public static class FPPState extends Http.StatusCode{
		public static final int SC_EXPECTATION_FAILED = 417;
	}
	
    @Override
    public void onLoad() {
    	decoder.setDeserializer(deserializer);
    	encoder.setSerializer(serializer);
        Logger.debug("mdemo start up.");
    }
    
	@Override
    public void onApplicationReady(){
    	List<Class> clss = Play.classloader.getAnnotatedClasses(Destination.class);
    	List<Class> belongToDestination;
    	for(Class clazz : clss){
    		Destination destination = (Destination)clazz.getAnnotation(Destination.class);
    		String dName = destination.value();
    		if(!Red5PlugIn.remotingDestination.containsKey(dName)){
    			belongToDestination = new ArrayList<Class>();
				Red5PlugIn.remotingDestination.put(dName, belongToDestination);
			}else{
				belongToDestination = Red5PlugIn.remotingDestination.get(dName);
			}
    		belongToDestination.add(clazz);
    		Logger.info("find destination=\"" + destination.value()+"\"");
    	}
    }
    
	@Override
    public boolean rawInvocation(Request request, Response response) throws Exception {
		try {
			if (APPLICATION_AMF.equals(request.contentType)) {
				System.out.println("flash request." + APPLICATION_AMF);

				IoBuffer reqBuffer = IoBuffer.allocate(request.body.available());
				copy(request.body, reqBuffer.asOutputStream());
				reqBuffer.flip();

				RemotingPacket packet = (RemotingPacket) decoder.decode(null, reqBuffer);
				reqBuffer.free();
				reqBuffer = null;
				request.args.put(PACK_KEY, packet);
				
				boolean cmdMsgComplete = autoAcknowledge(packet, request);
				if(cmdMsgComplete){
					response.status = Http.StatusCode.OK;
					response.contentType = APPLICATION_AMF;
					
					IoBuffer responseBuffer = encoder.encode(null, packet);
					copy(responseBuffer.asInputStream(), response.out);
					responseBuffer.free();
					responseBuffer = null;
				}
				return cmdMsgComplete;
			} else {
				return false;
			}
		} catch (Exception e) {
			Logger.error(e, e.getMessage());
			return false;
		}
    }
    

	private boolean autoAcknowledge(RemotingPacket packet, Request request) {
		if (packet == null)
			return false;
		List<RemotingCall> calls = packet.getCalls();
		if (calls.size() == 0) {
			return false;
		}
		RemotingCall rmCall = calls.get(0);
		
		if (MSG_METHOD_NAME.equals(rmCall.getServiceMethodName())
				&& FlexMessagingService.SERVICE_NAME.equals(rmCall.getServiceName())) {
			Object[] msgArgs = rmCall.getArguments();
			Object arg = null;
			if (msgArgs.length > 0) {
				arg = msgArgs[0];
				@SuppressWarnings("rawtypes")
				Class argClass = arg.getClass();

				if (CommandMessage.class.equals(argClass)) {
					AsyncMessage responseMsg = messageService.handleRequest((CommandMessage) arg);
					rmCall.setResult(responseMsg);
					return true;
				} else if (RemotingMessage.class.equals(argClass)) {
					RemotingMessage rmMsg = (RemotingMessage) arg;

					Object[] args = (Object[]) ConversionUtils.convert(rmMsg.body, Object[].class);
					List<Object> olist = getListFromArray(args);
					
					request.args.put(ARG_KEY, olist); //test ok.
					request.actionMethod = rmMsg.operation;
					request.controller = rmMsg.destination;
					
					AcknowledgeMessage acknowledegeMsg = new AcknowledgeMessage();
					acknowledegeMsg.headers = rmMsg.headers;
					acknowledegeMsg.clientId = rmMsg.clientId;
					acknowledegeMsg.correlationId = rmMsg.messageId;
					rmCall.setResult(acknowledegeMsg);
					request.args.put(PREPARED_RESULT, acknowledegeMsg);
					return false;
				} else {
					//TODO :  请处理netconnection 直接调用remoting的需求用例。
					System.out.println("can not process this type message. type:" + argClass);
					return false;
				}
			}
		} else {
			List<Object> invokeArgs = getListFromArray(rmCall.getArguments());
			request.args.put(ARG_KEY, invokeArgs); //test ok.
			Logger.debug("request is not flex message.");
		}
		System.out.println(rmCall);
		return false;
	}


	@Override
    public void onActionInvocationResult(Result result) { }
    
    
    public static void copy(InputStream input, OutputStream output) throws IOException {
		byte[] buf = new byte[2048];
		int bytesRead = input.read(buf);
		while (bytesRead != -1) {
			output.write(buf, 0, bytesRead);
			bytesRead = input.read(buf);
		}
		output.flush();
	}
    
    @Override
    public void onInvocationException(Throwable e) {
    	if(!Request.current().args.containsKey(PACK_KEY)){
    		return;
    	}
    	Logger.error(e, "marco said..onInvocationException():"+e.getMessage());
    	Response.current().status = FPPState.SC_EXPECTATION_FAILED;
    	RemotingPacket pack = (RemotingPacket)Request.current().args.get(PACK_KEY);
    	
    	RemotingCall call = pack.getCalls().get(0); 
    	call.setException((Exception)e);
		call.setStatus(Call.STATUS_GENERAL_EXCEPTION);
		//TODO:  请重写error/500.html 模板并实现encoding返回
    }
    
    /**
     * 这里可以通过调整 request.path的方法来改变路由
     */
    @Override
	public void routeRequest(Request request) {
		if (request.args.containsKey(PACK_KEY)) {
			if (request.controller != null && Red5PlugIn.remotingDestination.containsKey(request.controller)) {
				List<Class> clist = Red5PlugIn.remotingDestination.get(request.controller);
				if(clist == null || clist.size() == 0){
					RuntimeException e = new RuntimeException("destination is not exsist ->" + request.controller);
					Logger.error(e, "destination is not exsist.");
					throw e;
				}
				Method emthod = null;
				for (Class clz : clist) {
					emthod = Java.findActionMethod(request.actionMethod, clz);
					if (emthod != null){
						request.path = "/"+clz.getSimpleName()+"/"+request.actionMethod;
						System.out.println("route the path->"+request.path);
						return;
					}
				}
				throw new RuntimeException("operation is not exsist, " +
						"destination="+request.controller +", operation=" + request.actionMethod);
			}
		}
	}
    
    /**
     * 替代play默认的参数绑定机制，为controller绑定参数。
     * !!!!!注意如果返回值不为null， play和play的其他插件将没有执行绑定的机会。
     * debug 2011-1-1：起作用的是本方法 而不是复写方法 。请先实现本方法
     */
    @Override
    public Object bind(String name, @SuppressWarnings("rawtypes") Class clazz, 
    				Type type, Annotation[] annotations, Map<String, String[]> params){
    	Request crtRequest = Request.current();
    	if(crtRequest!=null && crtRequest.args!=null && crtRequest.args.containsKey(ARG_KEY)){
    		List<Object> args = (List<Object>)crtRequest.args.get(ARG_KEY);
    		if(args != null){
    			for(Object arg:args){
    				Class argClz = arg.getClass();
    				
    				if(clazz.equals(Long.class)){
    		    		if(argClz.equals(Double.class)){
    		    			args.remove(arg);
    		    			return Long.valueOf(Math.round((Double)arg));
    		    		}
    		    		
    		    		if(argClz.equals(Integer.class)){
    		    			args.remove(arg);
    		    			return Long.valueOf((long)((Integer)arg).intValue());
    		    		}
    		    	}
    				
					if(arg!= null && clazz.isAssignableFrom(argClz)){
    					args.remove(arg);
    					return arg;
    				}
    			}
    		}
    	}
    	return null;
    }

}
