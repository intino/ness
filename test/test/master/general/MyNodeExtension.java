package master.general;

import com.hazelcast.instance.impl.DefaultNodeExtension;
import com.hazelcast.instance.impl.Node;

public class MyNodeExtension extends DefaultNodeExtension {

	public MyNodeExtension(Node node) {
		super(node);
		System.out.println("hola");
	}
}
