package cn.marco.ffp.plugin;

import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.mvc.Controller;
import play.mvc.Http.Request;

public class FppController extends Controller {
	
	public static final String ARG_KEY = "amfArg";

	protected static void fppRender(Object... args) {
		if (FppPlugin.APPLICATION_AMF.equals(Request.current().contentType)) {
			throw new FppResult(args);
		} else {
			String templateName = null;
			if (args.length > 0 && args[0] instanceof String
					&& LocalVariablesNamesTracer.getAllLocalVariableNames(args[0]).isEmpty()) {
				templateName = args[0].toString();
			} else {
				templateName = template();
			}
			renderTemplate(templateName, args);
		}
	}

}
