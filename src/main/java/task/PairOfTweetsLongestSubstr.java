package task;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.log4j.Logger;

import semanticdiscoverytoolkit.GeneralizedSuffixTree;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * This class accesses the Twitter account (hardcoded in TWIT_BASE_URL), reads
 * tweets from it and finds the longest substring.
 * 
 * Solved problem: http://en.wikipedia.org/wiki/Longest_common_substring_problem
 * 
 * @author den.konakov@gmail.com
 */
public class PairOfTweetsLongestSubstr {

	Logger log = Logger.getLogger(PairOfTweetsLongestSubstr.class);

	public static final String TWIT_URL_BASE = "https://twitter.com/statuses/user_timeline.xml?id=cnnbrk";
	public static int TWIT_COUNT = 200;

	/**
	 * @param - if useMax is TRUE, then use TWIT_COUNT in URL
	 * 
	 * @return constructed
	 */
	private String getTwitUrl(boolean useMax) {
		return TWIT_URL_BASE + (useMax ? "&count=" + TWIT_COUNT : "");
	}

	/**
	 * 
	 * @return
	 */
	public Collection<String> getLongestSubstrPair() {
		ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);

		String url = getTwitUrl(true);
		log.info("Getting tweets from URL:" + url);
		WebResource service = client.resource(url);

		// Going to use Stream parser, to find all tweets
		final List<String> tweets = new LinkedList<>();
		int maxTweetLength = 0; // int since tweets can not be so long...
		ByteArrayInputStream inputStream = new ByteArrayInputStream(service
				.accept(MediaType.TEXT_XML).get(String.class).getBytes());
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		try {
			XMLEventReader reader = inputFactory
					.createXMLEventReader(inputStream);
			log.info("XML STAX Reader is created. Continue...");

			while (reader.hasNext()) {
				XMLEvent event = (XMLEvent) reader.next();
				if (event.isStartElement()) {

					StartElement element = event.asStartElement();
					log.info("Start EL event. Name:" + element.getName().getLocalPart());
					if (element.getName().getLocalPart().equals("text")) {
						
						event = (XMLEvent) reader.next();
						if (event.isCharacters()) {
							String txt = event.asCharacters().getData();
							log.info("Tweet text found:'"+txt+"'");
							tweets.add(txt);
							
							// Find the longest tweet
							if (txt.length() > maxTweetLength) {
								maxTweetLength = txt.length();
							}
						}
					}
				}
			}
		} catch (XMLStreamException e) {
			log.error("Cannon create XML reader", e);
		}

		if (tweets.size() < 1) {
			log.warn("Somehow we didn't get any tweets. Please check!");
		}

		// Build GST for future quick searches
		GeneralizedSuffixTree st = new GeneralizedSuffixTree(tweets.toArray(new String[] {}));
		log.info("GST is created for our tweet list. Start search ...");

		// Now we will start to look for common substring. We start from max length tweet
		while (maxTweetLength >= 1) {
			
			Collection<String> substrList = st.longestSubstrs(maxTweetLength);
			// We sort out all whole tweets from our suffix list (they are suffix for them)
			substrList = Collections2.filter(substrList, new Predicate<String>() {
				public boolean apply(String input) {
					return !tweets.contains(input);
				}
			});
			
			// If there is no suffix in the result filtered list 
			// - then look again with smaller min. length
			maxTweetLength --;
			if (substrList.size() == 0) {
				continue;
			}
			
			if (substrList.size() > 1) {
				log.warn("There is more than one pair of tweets with such long substr. We will use first pair..");
			}
			
			// TODO: If have time, modify method to return also string index from nodes...
			// it will allow us avoid this filtration and speed up process a little bit more...
			log.info("Filtering out the tweets with longest substr. And get two first of them");
			final String substr = substrList.toArray(new String[] {})[0];
			Collection<String> res = Collections2.filter(tweets, new Predicate<String>() {
				public boolean apply(String input) {
					return input.contains(substr);
				}
			});
			return res;
		}
		
		// we didn't find somehow the common substrings at all 
		// this should not happen, because at least one letter 
		// should be in common usually
		log.warn("Somehow we didn't get any common letters. Please check!");
		return null;
	}

	/**
	 * Just default run point for this class for running from console
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		PairOfTweetsLongestSubstr task = new PairOfTweetsLongestSubstr();
		Collection<String> tweets = task.getLongestSubstrPair();
		
		if (tweets != null) {
			System.out.println("The tweets have longest substr, are:");
			for (String res : tweets) {
				System.out.println(res);
			}
		} else {
			System.out.println("The tweets with longest substr are not found :(");
		}
	}
}