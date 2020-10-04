package de.Zorro909.CommandAPI;

import java.util.HashMap;

public interface ArgumentConverter {
  Object convert(String paramString, int paramInt, String[] paramArrayOfString, HashMap<String, Object> paramHashMap);
}
