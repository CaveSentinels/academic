/* ----------------------------------------------------------------------------
 *  @type: file
 *  @brief: The LRU Cache implementation.
 *  @author: Yaobin Wen
 *  @email: yaobinw@andrew.cmu.edu
 */


package cc.Proj2_3;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache
{
    private int _maxDynamicSize = 0;
    private int _maxStaticSize = 0;
    private LinkedHashMap< String, String > _cacheDyn = null;
    private HashMap< String, String > _cacheStatic = null;

    private int _hitCount = 0;
    private int _missCount = 0;

    public LRUCache( int maxDynamicSize, int maxStaticSize )
    {
        _maxDynamicSize = maxDynamicSize;
        _maxStaticSize = maxStaticSize;
        _cacheDyn = new LinkedHashMap<String, String>( _maxDynamicSize, 0.75F, true ) {
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > _maxDynamicSize;
            }
        };
        _cacheStatic = new HashMap<String, String>( _maxStaticSize );
    }

    public String access( String targetID )
    {
        String result = _cacheStatic.get( targetID );
        if ( result == null )
        {
            result = _cacheDyn.get( targetID );
        }

        return result;
    }

    public void update( String key, String value )
    {
        _cacheDyn.put( key, value );
    }

    public void updateStatic( String key, String value )
    {
        if ( _cacheStatic.size() < _maxStaticSize )
        {
            _cacheStatic.put( key, value );
        }
        else
        {
            // TODO: Throw exception?
        }
    }

    public void hit()
    {
        _hitCount++;
    }

    public void miss()
    {
        _missCount++;
    }

    public int hits()
    {
        return _hitCount;
    }

    public int misses()
    {
        return _missCount;
    }
}
