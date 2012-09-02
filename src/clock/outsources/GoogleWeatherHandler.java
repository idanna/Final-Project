
package clock.outsources;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import clock.outsources.dependencies.GoogleWeatherReader;
import clock.outsources.dependencies.WeatherModel;

public class GoogleWeatherHandler
{
    private String location = null;
    private GoogleWeatherReader weatherRead = null;
    
    public WeatherModel processWeatherRequest(String inputLocation) throws UnsupportedEncodingException
    {
        String[] testArgs = new String[1];
        this.location = inputLocation;
        
        // next line may throw "UnsupportedEncodingException" exception 
        testArgs[0] = URLEncoder.encode(this.location, "UTF-8");
                
        // initialize weather object
        this.weatherRead = new GoogleWeatherReader(testArgs[0]);
        
        // make connection to Internet, and get weather 
        this.weatherRead.process();

        // no exceptions were thrown, all is good return 0
        return this.weatherRead.getWeatherModel();        
    }
    
    // easy access functions
    public String getLocation()
    {
        return this.location == null? "": this.location;
    }  
    
    public WeatherModel getWeatherModel()
    {
        return this.weatherRead == null? new WeatherModel() : this.weatherRead.getWeatherModel();
    }  
}