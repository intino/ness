package io.intino.ness.master;

import io.intino.alexandria.Json;
import io.intino.ness.master.datamarts.SynchronizedMasterDatamart;

public interface MasterSerializer<T> {

	String serialize(T obj);
	T deserialize(String str);

	class OfDatamart implements MasterSerializer<MasterDatamart> {

		@Override
		public String serialize(MasterDatamart obj) {
			if(obj instanceof SynchronizedMasterDatamart) obj = ((SynchronizedMasterDatamart) obj).internalDatamart();
			return Json.toJson(new JsonWrap(obj.getClass(), Json.toJson(obj)));
		}

		@Override
		public MasterDatamart deserialize(String str) {
			JsonWrap wrap = Json.fromJson(str, JsonWrap.class);
			MasterDatamart datamart = (MasterDatamart) Json.fromJson(wrap.json, wrap.type);
			datamart.init();
			return datamart;
		}
	}

	class OfEntity implements MasterSerializer<Entity> {

		@Override
		public String serialize(Entity obj) {
			return Json.toJson(new JsonWrap(obj.getClass(), Json.toJson(obj)));
		}

		@Override
		public Entity deserialize(String str) {
			JsonWrap wrap = Json.fromJson(str, JsonWrap.class);
			return (Entity) Json.fromJson(wrap.json, wrap.type);
		}
	}

	class JsonWrap {
		public final Class<?> type;
		public final String json;
		public JsonWrap(Class<?> type, String json) {
			this.type = type;
			this.json = json;
		}
	}
}
