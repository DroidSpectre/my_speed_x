package my.speed.x;

import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
* Adapts values whose runtime type may differ from their declaration type.
* This is necessary to serialize/deserialize a heterogeneous List<LogEntry>.
*/
public final class RuntimeTypeAdapterFactory<T> implements TypeAdapterFactory {
	private final Class<?> baseType;
	private final String typeFieldName;
	private final Map<String, Class<?>> labelToSubtype;
	private final Map<Class<?>, String> subtypeToLabel;
	
	private RuntimeTypeAdapterFactory(Class<?> baseType,
	String typeFieldName) {
		this.baseType = baseType;
		this.typeFieldName = typeFieldName;
		this.labelToSubtype = new LinkedHashMap<String, Class<?>>();
		this.subtypeToLabel = new LinkedHashMap<Class<?>, String>();
	}
	
	public static <T> RuntimeTypeAdapterFactory<T> of(Class<T> baseType,
	String typeFieldName) {
		return new RuntimeTypeAdapterFactory<T>(baseType, typeFieldName);
	}
	
	public RuntimeTypeAdapterFactory<T> registerSubtype(Class<? extends T> subtype,
	String label) {
		if (subtype == null || label == null) {
			throw new NullPointerException();
		}
		if (subtypeToLabel.containsKey(subtype) || labelToSubtype.containsKey(label)) {
			throw new IllegalArgumentException("types and labels must be unique");
		}
		labelToSubtype.put(label, subtype);
		subtypeToLabel.put(subtype, label);
		return this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
		if (! baseType.isAssignableFrom(type.getRawType())) {
			return null;
		}
		
		final Map<String, TypeAdapter<?>> labelToDelegate
		= new LinkedHashMap<String, TypeAdapter<?>>();
		final Map<Class<?>, TypeAdapter<?>> subtypeToDelegate
		= new LinkedHashMap<Class<?>, TypeAdapter<?>>();
		
		for (Map.Entry<String, Class<?>> entry : labelToSubtype.entrySet()) {
			TypeAdapter<?> delegate = gson.getDelegateAdapter(this, TypeToken.get(entry.getValue()));
			labelToDelegate.put(entry.getKey(), delegate);
			subtypeToDelegate.put(entry.getValue(), delegate);
		}
		
		return new TypeAdapter<R>() {
			@Override
			public void write(JsonWriter out, R value) throws IOException {
				Class<?> srcType = value.getClass();
				String label = subtypeToLabel.get(srcType);
				@SuppressWarnings("unchecked")
				TypeAdapter<R> delegate = (TypeAdapter<R>) subtypeToDelegate.get(srcType);
				if (delegate == null) {
					throw new JsonParseException("Cannot serialize " + srcType.getName());
				}
				JsonObject jsonObject = delegate.toJsonTree(value).getAsJsonObject();
				JsonObject clone = new JsonObject();
				clone.add(typeFieldName, gson.toJsonTree(label));
				for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
					clone.add(e.getKey(), e.getValue());
				}
				gson.toJson(clone, out);
			}
			
			@Override
			public R read(JsonReader in) throws IOException {
				JsonObject jsonObject = gson.fromJson(in, JsonObject.class);
				JsonElement labelJson = jsonObject.remove(typeFieldName);
				if (labelJson == null) {
					throw new JsonParseException("Cannot deserialize missing field " + typeFieldName);
				}
				String label = labelJson.getAsString();
				@SuppressWarnings("unchecked")
				TypeAdapter<R> delegate = (TypeAdapter<R>) labelToDelegate.get(label);
				if (delegate == null) {
					throw new JsonParseException("Cannot deserialize unknown label " + label);
				}
				return delegate.fromJsonTree(jsonObject);
			}
		}.nullSafe();
	}
}