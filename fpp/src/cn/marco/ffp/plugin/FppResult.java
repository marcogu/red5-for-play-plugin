package cn.marco.ffp.plugin;

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.red5.compatibility.flex.messaging.messages.AcknowledgeMessage;
import org.red5.server.net.remoting.message.RemotingCall;
import org.red5.server.net.remoting.message.RemotingPacket;

import play.Logger;
import play.mvc.Http.Request;
import play.mvc.Http.Response;
import play.mvc.results.Result;

public class FppResult extends Result {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6692565730417334514L;
	
	private Object[] results=null;

	/**
	 * TODO :  请实现多个返回值 eg:  FPPController.fppRender(list1, list2, obj3, int4...);
	 */
	@Override
	public void apply(Request request, Response response) {
		try{
			RemotingPacket pack = (RemotingPacket)request.args.get(FppPlugin.PACK_KEY);
			if(request.args.containsKey(FppPlugin.PREPARED_RESULT)){
				AcknowledgeMessage msg = (AcknowledgeMessage)request.args.get(FppPlugin.PREPARED_RESULT);
				msg.body = getResult();
			}else{
				RemotingCall remotingCall = pack.getCalls().get(0);
				remotingCall.setResult(getResult());
			}
			IoBuffer bufferEncode = FppPlugin.encoder.encode(null, pack);
			response.out.write(bufferEncode.array());
			bufferEncode.free();
			bufferEncode = null;
		}catch (Exception e) {
			Logger.error(e, e.getMessage());
		}finally{
			try {
				response.out.flush();
				response.out.close();
				request.args.remove(FppPlugin.PREPARED_RESULT);
				request.args.remove(FppPlugin.PACK_KEY);
				request.args.remove(FppController.ARG_KEY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public FppResult(Object... args){
		this.results = args;
	}
	
	public Object getResult(){
		if(results != null){
			if(results.length > 1){
				return results;
			}else if(results.length == 1){
				return results[0];
			}else {
				return null;
			}
		}
		return null;
	}
	

	
}
