/**
 * 
 */
package org.sunbird.es;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.sunbird.common.Platform;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author pradyumna
 *
 */
public class ElasticSearchUtil {

	static {
		System.setProperty("es.set.netty.runtime.available.processors", "false");
		registerShutdownHook();
	}

	private static Map<String, RestHighLevelClient> esClient = new HashMap<String, RestHighLevelClient>();

	public static int defaultResultLimit = 10000;
	private static final int resultLimit = 100;
	public int defaultResultOffset = 0;
	private static int BATCH_SIZE = (Platform.config.hasPath("search.batch.size"))
			? Platform.config.getInt("search.batch.size")
			: 1000;
	private static ObjectMapper mapper = new ObjectMapper();

	public static void initialiseESClient(String indexName, String connectionInfo) {
		createClient(indexName, connectionInfo);
	}

	/**
	 * 
	 */
	private static void createClient(String indexName, String connectionInfo) {
		if (!esClient.containsKey(indexName)) {
			Map<String, Integer> hostPort = new HashMap<String, Integer>();
			for (String info : connectionInfo.split(",")) {
				hostPort.put(info.split(":")[0], Integer.valueOf(info.split(":")[1]));
			}
			List<HttpHost> httpHosts = new ArrayList<>();
			for (String host : hostPort.keySet()) {
				httpHosts.add(new HttpHost(host, hostPort.get(host)));
			}
			RestHighLevelClient client = new RestHighLevelClient(
					RestClient.builder(httpHosts.toArray(new HttpHost[httpHosts.size()])));
			if (null != client)
				esClient.put(indexName, client);
		}
	}

	private static RestHighLevelClient getClient(String indexName) {
		return esClient.get(indexName);
	}

	public void finalize() {
		cleanESClient();
	}

	public static boolean isIndexExists(String indexName) {
		Response response;
		try {
			response = getClient(indexName).getLowLevelClient().performRequest("HEAD", "/" + indexName);
			return (200 == response.getStatusLine().getStatusCode());
		} catch (IOException e) {
			return false;
		}

	}

	private void actionListener() {
		CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
		new ActionListener<IndexResponse>() {
			@Override
			public void onResponse(IndexResponse indexResponse) {
				indexResponse.status().getStatus();
				future.complete(new HashMap<String, Object>(){{
					put("status", indexResponse.status().getStatus());
				}});
			}

			@Override
			public void onFailure(Exception e) {
				future.completeExceptionally(e);
			}
		};
	}

	public static void addDocument(String indexName, String documentType, Map<String, Object> doc, String documentId) {
		try {
			IndexRequest indexRequest = (StringUtils.isNotBlank(documentId)) ?
					new IndexRequest(indexName, documentType, documentId) : new IndexRequest(indexName, documentType);
			IndexResponse response = getClient(indexName).index(indexRequest.source(doc));
			System.out.println("Added " + response.getId() + " to index " + response.getIndex() + " response: " + response.status().getStatus());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error while adding document to index :" + indexName);
		}
	}

	public static void updateDocument(String indexName, String documentType, String document, String documentId) {
		try {
			Map<String, Object> doc = mapper.readValue(document, new TypeReference<Map<String, Object>>() {
			});
			IndexRequest indexRequest = new IndexRequest(indexName, documentType, documentId).source(doc);
			UpdateRequest request = new UpdateRequest().index(indexName).type(documentType).id(documentId).doc(doc)
					.upsert(indexRequest);
			UpdateResponse response = getClient(indexName).update(request);
			System.out.println("Updated " + response.getId() + " to index " + response.getIndex());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error while updating document to index :" + indexName);
		}

	}

	public static void deleteDocument(String indexName, String documentType, String documentId)
			throws IOException {
		DeleteResponse response = getClient(indexName).delete(new DeleteRequest(indexName, documentType, documentId));
		System.out.println("Deleted " + response.getId() + " to index " + response.getIndex());
	}

	public static Map<String, Object> getDocument(String indexName, String documentType, String documentId)
			throws IOException {
		GetResponse response = getClient(indexName).get(new GetRequest(indexName, documentType, documentId));
		return response.getSource();
	}

	@SuppressWarnings("rawtypes")
	public static List<Object> getDocumentsFromSearchResult(SearchResponse result, Class objectClass) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHits(hits);
	}

	public static List<Object> getDocumentsFromHits(SearchHits hits) {
		List<Object> documents = new ArrayList<Object>();
		for (SearchHit hit : hits) {
			documents.add(hit.getSourceAsMap());
		}
		return documents;
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromSearchResultWithScore(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithScore(hits);
	}

	@SuppressWarnings("rawtypes")
	public static List<Map> getDocumentsFromHitsWithScore(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = hit.getSourceAsMap();
			hitDocument.put("score", hit.getScore());
			documents.add(hitDocument);
		}
		return documents;
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Map> textSearchReturningId(Map<String, Object> matchCriterias, String indexName,
			String indexType)
			throws Exception {
		SearchResponse result = search(matchCriterias, null, indexName, indexType, null, false, 100);
		return getDocumentsFromSearchResultWithId(result);
	}

	@SuppressWarnings({ "rawtypes" })
	public static List<Map> getDocumentsFromSearchResultWithId(SearchResponse result) {
		SearchHits hits = result.getHits();
		return getDocumentsFromHitsWithId(hits);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<Map> getDocumentsFromHitsWithId(SearchHits hits) {
		List<Map> documents = new ArrayList<Map>();
		for (SearchHit hit : hits) {
			Map<String, Object> hitDocument = (Map) hit.getSourceAsMap();
			hitDocument.put("id", hit.getId());
			documents.add(hitDocument);
		}
		return documents;
	}

	public static SearchResponse search(Map<String, Object> matchCriterias, Map<String, Object> textFiltersMap,
			String indexName, String indexType, List<Map<String, Object>> groupBy, boolean isDistinct, int limit)
			throws Exception {
		SearchSourceBuilder query = buildJsonForQuery(matchCriterias, textFiltersMap, groupBy, isDistinct, indexName);
		query.size(limit);
		return search(indexName, indexType, query);
	}

	public static SearchResponse search(String indexName, String indexType, SearchSourceBuilder query)
			throws Exception {
		return getClient(indexName).search(new SearchRequest(indexName).source(query));
	}

	public static SearchResponse search(String indexName, SearchSourceBuilder searchSourceBuilder)
			throws IOException {
		return getClient(indexName).search(new SearchRequest().indices(indexName).source(searchSourceBuilder));
	}

	public static int count(String indexName, SearchSourceBuilder searchSourceBuilder) throws IOException {
		SearchResponse response = getClient(indexName)
				.search(new SearchRequest().indices(indexName).source(searchSourceBuilder));
		return (int) response.getHits().getTotalHits();

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map<String, Object> getCountFromAggregation(Aggregations aggregations,
			List<Map<String, Object>> groupByList) {
		Map<String, Object> countMap = new HashMap<String, Object>();
		if (aggregations != null) {
			for (Map<String, Object> aggregationsMap : groupByList) {
				Map<String, Object> parentCountMap = new HashMap<String, Object>();
				String groupByParent = (String) aggregationsMap.get("groupByParent");
				Map aggKeyMap = (Map) aggregations.get(groupByParent);
				List<Map<String, Double>> aggKeyList = (List<Map<String, Double>>) aggKeyMap.get("buckets");
				List<Map<String, Object>> parentGroupList = new ArrayList<Map<String, Object>>();
				for (Map aggKeyListMap : aggKeyList) {
					Map<String, Object> parentCountObject = new HashMap<String, Object>();
					parentCountObject.put("count", ((Double) aggKeyListMap.get("doc_count")).longValue());
					List<String> groupByChildList = (List<String>) aggregationsMap.get("groupByChildList");
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						Map<String, Object> groupByChildMap = new HashMap<String, Object>();
						for (String groupByChild : groupByChildList) {
							List<Map<String, Long>> childGroupsList = new ArrayList<Map<String, Long>>();
							Map aggChildKeyMap = (Map) aggKeyListMap.get(groupByChild);
							List<Map<String, Double>> aggChildKeyList = (List<Map<String, Double>>) aggChildKeyMap
									.get("buckets");
							Map<String, Long> childCountMap = new HashMap<String, Long>();
							for (Map aggChildKeyListMap : aggChildKeyList) {
								childCountMap.put((String) aggChildKeyListMap.get("key"),
										((Double) aggChildKeyListMap.get("doc_count")).longValue());
								childGroupsList.add(childCountMap);
								groupByChildMap.put(groupByChild, childCountMap);
							}
						}
						parentCountObject.putAll(groupByChildMap);
					}
					parentCountMap.put((String) aggKeyListMap.get("key"), parentCountObject);
					parentGroupList.add(parentCountMap);
				}
				countMap.put(groupByParent, parentCountMap);
			}
		}
		return countMap;
	}


	@SuppressWarnings("unchecked")
	public static SearchSourceBuilder buildJsonForQuery(Map<String, Object> matchCriterias,
			Map<String, Object> textFiltersMap, List<Map<String, Object>> groupByList, boolean isDistinct,
			String indexName) {

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
		if (matchCriterias != null) {

			for (Map.Entry<String, Object> entry : matchCriterias.entrySet()) {
				if (entry.getValue() instanceof List) {
					for (String matchText : (ArrayList<String>) entry.getValue()) {
						queryBuilder.should(QueryBuilders.matchQuery(entry.getKey(), matchText));
					}
				}
			}
		}

		if (textFiltersMap != null && !textFiltersMap.isEmpty()) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			for (Map.Entry<String, Object> entry : textFiltersMap.entrySet()) {
				ArrayList<String> termValues = (ArrayList<String>) entry.getValue();
				for (String termValue : termValues) {
					boolQuery.must(QueryBuilders.termQuery(entry.getKey(), termValue));
				}
			}
			queryBuilder.filter(boolQuery);
		}

		searchSourceBuilder.query(QueryBuilders.boolQuery().filter(queryBuilder));

		if (groupByList != null && !groupByList.isEmpty()) {
			if (!isDistinct) {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupByParent = (String) groupByMap.get("groupByParent");
					List<String> groupByChildList = (List<String>) groupByMap.get("groupByChildList");
					TermsAggregationBuilder termBuilder = AggregationBuilders.terms(groupByParent).field(groupByParent);
					if (groupByChildList != null && !groupByChildList.isEmpty()) {
						for (String childGroupBy : groupByChildList) {
							termBuilder.subAggregation(AggregationBuilders.terms(childGroupBy).field(childGroupBy));
						}

					}
					searchSourceBuilder.aggregation(termBuilder);
				}
			} else {
				for (Map<String, Object> groupByMap : groupByList) {
					String groupBy = (String) groupByMap.get("groupBy");
					String distinctKey = (String) groupByMap.get("distinctKey");
					searchSourceBuilder.aggregation(
							AggregationBuilders.terms(groupBy).field(groupBy).subAggregation(AggregationBuilders
									.cardinality("distinct_" + distinctKey + "s").field(distinctKey)));
				}
			}
		}

		return searchSourceBuilder;
	}



	private static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					cleanESClient();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void cleanESClient() {
		if (!esClient.isEmpty())
			for (RestHighLevelClient client : esClient.values()) {
				if (null != client)
					try {
						client.close();
					} catch (IOException e) {
					}
			}
	}

	/**
	 * This method perform delete operation in bulk using document ids.
	 *
	 * @param indexName
	 * @param documentType
	 * @param identifiers
	 * @throws Exception
	 */
	public static void bulkDeleteDocumentById(String indexName, String documentType, List<String> identifiers) throws Exception {
		if (isIndexExists(indexName)) {
			if (null != identifiers && !identifiers.isEmpty()) {
				int count = 0;
				BulkRequest request = new BulkRequest();
				for (String documentId : identifiers) {
					count++;
					request.add(new DeleteRequest(indexName, documentType, documentId));
					if (count % BATCH_SIZE == 0 || (count % BATCH_SIZE < BATCH_SIZE && count == identifiers.size())) {
						BulkResponse bulkResponse = getClient(indexName).bulk(request);
						List<String> failedIds = Arrays.stream(bulkResponse.getItems()).filter(
								itemResp -> !StringUtils.equals(itemResp.getResponse().getResult().getLowercase(),"deleted")
						).map(r -> r.getResponse().getId()).collect(Collectors.toList());
						if (CollectionUtils.isNotEmpty(failedIds))
							System.out.println("Failed Id's While Deleting Elasticsearch Documents (Bulk Delete) : " + failedIds);
						if (bulkResponse.hasFailures()) {
							//TODO: Implement Retry Mechanism
							System.out.println("Error Occured While Deleting Elasticsearch Documents in Bulk : " + bulkResponse.buildFailureMessage());
						}
					}
				}
			}
		} else {
			throw new RuntimeException("ES Index Not Found With Id : " + indexName);
		}
	}

}