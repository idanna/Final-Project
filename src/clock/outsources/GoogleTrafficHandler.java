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

public class GoogleTrafficHandler 
{
	public class TrafficData
	{
		public TrafficData()
		{
			duration = -1l;
			distance = -1f;
		}
		
		private long duration;
		private float distance;
		
		/**
		 * 
		 * @return Duration in seconds. If error occurred or places not found then -1 is returned.
		 */
		public long getDuration() {
			return duration;
		}
		public void setDuration(long duration) {
			this.duration = duration;
		}

		/**
		 * 
		 * @return Distance in meters
		 */
		public float getDistance() {
			return distance;
		}
		public void setDistance(float distance) {
			this.distance = distance;
		}
	}
	
	private URL googleUrl;
	private XPathFactory xpf;
	private XPath xpath;
	
	public GoogleTrafficHandler()
	{
		xpf = XPathFactory.newInstance();
		xpath = xpf.newXPath();
	}
	
	/***
	 * Method connect to google maps with the query provided ("from", "to") and returns the duration in Seconds.
	 * @param from - origin place (e.g. "רבנו ירוחם 2, תל אביב")
	 * @param to - destination place with same format as origin
	 */
	public TrafficData calculateTrafficInfo(String from, String to) throws Exception
	{
		TrafficData trafficData = new TrafficData();
		
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
		
		//Calculate duration
		String xPathDurationExpression = "DirectionsResponse/route/leg/step/duration/value";
		NodeList durationNodeList = this.parseXml(is, xPathDurationExpression);		
		trafficData.setDuration(this.getDurationFromNodeList(durationNodeList));
		
		//Calculate distance
		String xPathDistanceExpression = "DirectionsResponse/route/leg/step/distance/value";
		NodeList distanceNodeList = this.parseXml(is, xPathDistanceExpression);
		trafficData.setDistance(this.getDistanceFromNodeList(distanceNodeList));

		return trafficData;	
	}
	
	public boolean checkAddress(String address) throws Exception
	{
		address = URLEncoder.encode(address, "UTF-8");
		String query = "http://maps.googleapis.com/maps/api/geocode/xml?"
				+ "address=" + address
				+ "&sensor=false"; 
		
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
		
		//Check address status
		String xPathDurationExpression = "GeocodeResponse/status";
		NodeList statusNodeList = this.parseXml(is, xPathDurationExpression);		

		return getStatusFromNodeList(statusNodeList).equalsIgnoreCase("ok")? true : false;
				
	}

	private String getStatusFromNodeList(NodeList statusNodeList) {
				
		return statusNodeList.item(0).getFirstChild().getNodeValue();
	}

	private long getDurationFromNodeList(NodeList durationNodeList) {
		String valueStr;
		long value = 0;
		
		for (int i=0 ; i < durationNodeList.getLength() ; ++i)
		{
			valueStr = durationNodeList.item(i).getFirstChild().getNodeValue();
			if (valueStr != null)
			{
				value += Long.parseLong(valueStr);
			}
		}
		
		if (durationNodeList.getLength() == 0)
		{
			value = -1l;
		}
		
		return value;
	}
	
	private float getDistanceFromNodeList(NodeList distanceNodeList) {
		String valueStr;
		float value = 0;
		
		for (int i=0 ; i < distanceNodeList.getLength() ; ++i)
		{
			valueStr = distanceNodeList.item(i).getFirstChild().getNodeValue();
			if (valueStr != null)
			{
				value += Float.parseFloat(valueStr);
			}
		}
		
		if (distanceNodeList.getLength() == 0)
		{
			value = -1f;
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

