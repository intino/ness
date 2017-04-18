public class Model {
	public static int version(io.intino.ness.Channel self) {
		try {
			String[] names = self.qualifiedName().split("\\.");
			return Integer.parseInt(names[names.length]);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
