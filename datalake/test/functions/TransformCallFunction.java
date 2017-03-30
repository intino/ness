package functions;

import io.intino.ness.datalake.NessFunction;
import io.intino.ness.inl.Message;

import java.util.HashMap;
import java.util.Map;

public class TransformCallFunction implements NessFunction {

    private final Map<String,String> types = new HashMap<>();

    public TransformCallFunction() {
        types.put("call-request","CallRequest");
        types.put("inbound-start","InboundStart");
        types.put("inbound-connect","InboundConnect");
        types.put("inbound-disconnect","InboundDisconnect");
        types.put("outbound-start","OutboundStart");
        types.put("outbound-connect","OutboundConnect");
        types.put("outbound-disconnect","OutboundDisconnect");
        types.put("route","Route");
    }

    @Override
    public Message cast(Message message) {
        String type = types.get(message.type());
        if (type == null) System.out.println(message.type());
        message.type(type);
        if (message.contains("call-id")) message.rename("call-id", "callId");
        if (message.contains("sip-dc")) message.rename("sip-dc", "sipDC");
        if (message.contains("call-dc")) message.rename("call-dc", "callDC");
        message.topic("feed.genie."+ type + ".1");
        return message;
    }
}
