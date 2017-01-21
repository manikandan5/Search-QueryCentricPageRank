import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

public class AuthorRankwithQuery 
{
	public static void main(String args[]) throws Exception
	{	
		String indexpath="/Users/murugesm/Documents/EclipseWorkspace/Search-A3/author_index/";
		
		print(indexpath, "Data Mining");
		print(indexpath, "Information Retrieval");
	}

	@SuppressWarnings("unchecked")
	public static void print(String indexpath, String queryString ) throws IOException, ParseException
	{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexpath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("content", analyzer);
		
		HashMap<String, Double> priors= new HashMap<String, Double>();
		Query query = parser.parse(QueryParser.escape(queryString));
		TopDocs collector = searcher.search(query, 300);
		ScoreDoc[] docs = collector.scoreDocs;
		
		for (int iter = 0; iter < docs.length; iter++)
		{
			Document doc = searcher.doc(docs[iter].doc); 
			String key=doc.get("authorid");
			Double value=(double) docs[iter].score;
			if(priors.containsKey(key))
			{
				Double old=(double)priors.get(key);
				value+=old;
			}
			priors.put(key, value);
		}

		HashMap<String, Double> normalized= new HashMap<String, Double>();
		for (Map.Entry<String, Double> val: priors.entrySet()) 
		{
			Double temp = val.getValue();
			String key = val.getKey();
			normalized.put(key, temp);
		}

		for(Integer i = 0; i < 2000; i++)
		{
			if(!(normalized.containsKey(i.toString())))
			{
				normalized.put(i.toString(),(double)0);
			}
		}

		@SuppressWarnings({ "rawtypes" })
		PajekNetReader pnr = new PajekNetReader(FactoryUtils.instantiateFactory(Object.class));
		Graph<Integer, String> graph = new UndirectedSparseGraph<Integer,String>();
    
		pnr.load("/Users/murugesm/Documents/EclipseWorkspace/Search-A3/author.net", graph);
			
		Transformer<Integer, Double> prior=new Transformer<Integer, Double>()
		{
			@Override
			public Double transform(Integer val)
			{
				return (double) normalized.get(val.toString());
			}
		
		};
	
		HashMap<String, Double> results= new HashMap<String, Double>();
		PageRankWithPriors<Integer, String> pageRank = new PageRankWithPriors<Integer, String>(graph, prior, 0.85);
		pageRank.evaluate();
		
		for (Object node : graph.getVertices()) 
		{
			results.put(node.toString(), pageRank.getVertexScore(Integer.parseInt(node.toString())));
		}

		Comparator<String> comparator = new checker<String, Double>(results);
		TreeMap<String, Double> output = new TreeMap<String, Double>(comparator);
		output.putAll(results);

		printResults(output, queryString);
	}

	public static void printResults(TreeMap<String, Double> results, String queryString)
	{
		int count=0;
		
		System.out.println("Results for the query term : "+ queryString);
		
		for (Map.Entry<String, Double> val: results.entrySet() ) 
		{
			System.out.println(val.getKey()+" "+ val.getValue());
			if(count > 10)
			{
				break;
			}
			count++;
		}
		System.out.println();
	}
}