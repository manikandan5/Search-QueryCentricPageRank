import java.util.Comparator;
import java.util.HashMap;

class checker<Key, Value extends Comparable<Value>> implements Comparator<Key>
{	 
	HashMap<Key, Value> hashMap = new HashMap<Key, Value>();

	public checker(HashMap<Key, Value> map)
	{
		this.hashMap.putAll(map);
	}

	@Override
	public int compare(Key s1, Key s2) 
	{
		return -hashMap.get(s1).compareTo(hashMap.get(s2));
	}
}