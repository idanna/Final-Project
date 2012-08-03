package clock.outsources.dependencies;

public interface IWeatherReader 
{
	public void process();
	public String getLocation();
	public void setLocation(String location);
	public WeatherModel getWeatherModel();
	public void setWeatherModel(WeatherModel wm);
}
