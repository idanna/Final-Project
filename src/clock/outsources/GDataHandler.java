package clock.outsources;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class GDataHandler 
{
	private URL googleUrl;
	private XPathFactory xpf;
	private XPath xpath;
	
	public GDataHandler()
	{
		xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
	}
	
	/***
	 * Method connect to google maps with the query provided ("from", "to") and returns the duration in Seconds.
	 * @param from - origin place (e.g. "רבנו ירוחם 2, תל אביב")
	 * @param to - destination place with same format as origin
	 * @return Duration in seconds. If error occurred or places not found then -1 is returned.
	 */
	public int getDuration(String from, String to)
	{
		int res = -1;
		
		try{
			from = URLEncoder.encode(from, "UTF-8");
			to = URLEncoder.encode(to, "UTF-8");
			
			String query = "http://maps.googleapis.com/maps/api/directions/xml?"
					+ "origin=" + from
					+ "&destination=" + to
					+ "&sensor=false"; 
			//NOTICE: in order to use device location as origin, sensor should be set as true
			
			googleUrl = new URL(query);
			
			URLConnection uc = googleUrl.openConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			
			String xmlStr = ""; 
			String inputLine;
			
			in.readLine();

	        while ((inputLine = in.readLine()) != null) 
			{
				xmlStr = xmlStr.concat(inputLine);
				xmlStr = xmlStr.concat("\n");
			}
        
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlStr));
			
			String xPathExpression = "DirectionsResponse/route/leg/step/duration/value";
			NodeList nl = this.parseXml(is, xPathExpression);		
			res = this.getDurationFromNodeList(nl);
		  
		}
		catch (Exception ex)
		{
			res = -1;
		}

		return res;	
	}

	private int getDurationFromNodeList(NodeList nl) {
		String valueStr;
		int value = 0;
		
		for (int i=0 ; i < nl.getLength() ; ++i)
		{
			valueStr = nl.item(i).getFirstChild().getNodeValue();
			if (valueStr != null)
			{
				value += Integer.parseInt(valueStr);
			}
		}
		
		if (nl.getLength() == 0)
		{
			value = -1;
		}
		
		return value;
	}
	
	private NodeList parseXml (InputSource xmlSrc, String xPathExpression) throws Exception
	{	
		XPathExpression expr = xpath.compile(xPathExpression);
		Object result = expr.evaluate(xmlSrc, XPathConstants.NODESET);
		return (NodeList) result;
	}


}

