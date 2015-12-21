package cc.Proj2_3;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache2
{
    private int _maxSize = 0;
    private LinkedHashMap< String, String > _cache = null;

    public LRUCache2( int maxSize )
    {
        _maxSize = maxSize;
        _cache = new LinkedHashMap<String, String>(_maxSize, 0.75F, true ) {
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > _maxSize;
            }
        };
    }

    public String access( String targetID )
    {
        String result = _cache.get( targetID );

        return result;
    }

    public void update( String key, String value )
    {
        _cache.put(key, value);
    }
}
