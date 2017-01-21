import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.FactoryUtils;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

public class AuthorRank 
{
    @SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException 
    {
        @SuppressWarnings("rawtypes")
		PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
        Graph<Integer, String> graph = new UndirectedSparseGraph<Integer, String>();
        pnr.load("/Users/murugesm/Documents/EclipseWorkspace/Search-A3/author.net", graph);

        HashMap<String, Double> score= new HashMap<String, Double>();
        PageRank<Integer, String> pageRank = new PageRank<Integer, String>(graph, 0.85);
        pageRank.setMaxIterations(30);
        pageRank.evaluate();
        
        for (Object node : graph.getVertices()) 
        {
    		String key=node.toString();
    		Double value= pageRank.getVertexScore(Integer.parseInt(node.toString()));
    		score.put(key, value);
        }
        
		Comparator<String> checker = new checker<String, Double>(score);
		TreeMap<String, Double> output = new TreeMap<String, Double>(checker);
		output.putAll(score);
		
		printResults(output);
    }
    
    static void printResults(TreeMap<String, Double> output)
    {
    	int count=0;
    
    	for (Map.Entry<String, Double> val: output.entrySet()) 
		{
			System.out.println(val.getKey()+" "+ val.getValue());
			
			if(count>10)
			{
				  break;
			}
			count++;
		}
    }
}