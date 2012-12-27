package pagingTests.supportFiles;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import java.io.IOException;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import java.util.Calendar;

/**
 *
 * @author t0neg0d
 */
public class SkyDome implements Control {
	private ViewPort viewPort;
	private Spatial spatial;
	private AssetManager assetManager;
	private Node skyNight;
	private Camera cam;
    private boolean enabled = true;
	private FogFilter fog = null;
	private DirectionalLight sun = null;
	private ColorRGBA sunDayLight  = new ColorRGBA(.92f, .92f, .92f, 1f);
	private ColorRGBA sunNightLight  = new ColorRGBA(.15f, .15f, .15f, .25f);
	private ColorRGBA sunRiseColor  = new ColorRGBA(.9843f, .5254f, .8823f, 1f);
	private ColorRGBA sunSetColor  = new ColorRGBA(.8509f, .5254f, .9843f, 1f);
	private boolean controlFog = false;
	private boolean controlSun = false;
	private String model, nightSkyMap, sunMap, moonMap, cloudsMap, fogAlphaMap;
	private boolean cycleCI = false, cycleCO = false;
	private float cloudMaxAlpha = 1f, cloudMinAlpha = 0f, cloudsAlpha = 1;
	private float cloudCycleSpeed = .125f;
	private float cloud1Rotation = FastMath.HALF_PI+0.02f;
	private float cloud1Speed = .025f;
	private float cloud2Rotation = FastMath.HALF_PI+0.023f;
	private float cloud2Speed = .05f;
	private float moonRotation = 180;
	private float sunMoonSpeed = .0185f;
	private float horizonAlpha = 1.0f;
	
	private boolean isDay = false;
	private boolean cycleN2D = false, cycleD2N = false;
	private float dayAlpha = 0;
	private float cycleSpeed = .125f;
	private float sunRotation = 0f;
	private Quaternion sunQ = new Quaternion();
	private Vector3f sunDirection = new Vector3f(-.2f, -1f, -.2f);
	private float sunOffsetX = 0.5f;
	private float sunOffsetXInc = 0f;
	private float sunSpeed = 0.0125f;
	private boolean sunDay = true;
	private ColorRGBA fogColor = new ColorRGBA(0.7f, 0.7f, 0.7f, 0.6f);
	private ColorRGBA fogNightColor = new ColorRGBA(0.3f, 0.3f, 0.3f, 0.6f);
	private ColorRGBA dayColor = new ColorRGBA(.7f,.7f,1.0f,1.0f);
	private ColorRGBA nightColor = new ColorRGBA(.4f,.3f,.6f,1.0f);
	private Texture tex_Sky, tex_Sun, tex_Moon, tex_FogAlpha, tex_Clouds;
	private Material mat_Sky;
	private boolean useCalendar = false;
	
	// Calendar
	long serverDate, clientDate, dif;
	private String[] months = new String[] {
		"January", "February", "March",
		"April", "May", "June",
		"July", "August", "September",
		"October", "November", "December"
	};
	private int yearOffset = 0;
	
	private int secondsPerMinute = 10;
	private int minutesPerHour = 60;
	private int hoursPerDay = 24;
	private int daysPerWeek = 7;
	private int weeksPerMonth = 4;
	private int monthsPerYear = 12;
	private int daysPerYear = 365;
	
	private long secondsPerHour = 600;
	private long secondsPerDay = 14400;
	private long secondsPerWeek = 100800;
	private long secondsPerMonth = 403200;
	private long secondsPerYear = 4838400;
	
	/**
     * Creates a new SkyDome control
     * @param assetManager  A pointer to the JME application AssetManager
	 * @param cam  A pointer to the default Camera of the JME application
	 * @param model  j3o to use as the Sky Dome
	 * @param nightSkyMap  The string value of the texture asset for night time sky
	 * @param moonMap  The string value of the texture asset for the moon.  This is the only param that accepts null
	 * @param cloudsMap  The string value of the texture asset for the clouds
	 * @param fogAlphaMap  The string value of the texture asset for the blending alpha map for fog coloring
     */
	public SkyDome(AssetManager assetManager, Camera cam, String model, String nightSkyMap, String sunMap, String moonMap, String cloudsMap, String fogAlphaMap) {
		this.assetManager = assetManager;
		this.cam = cam;
		
		this.model = model;
		this.nightSkyMap = nightSkyMap;
		this.sunMap = sunMap;
		this.moonMap = moonMap;
		this.cloudsMap = cloudsMap;
		this.fogAlphaMap = fogAlphaMap;
		
		tex_FogAlpha = assetManager.loadTexture(fogAlphaMap);
		tex_FogAlpha.setMinFilter(MinFilter.NearestNoMipMaps);
		tex_FogAlpha.setMagFilter(MagFilter.Nearest);
		tex_FogAlpha.setWrap(WrapMode.Repeat);
		
		tex_Sky = assetManager.loadTexture(nightSkyMap);
		tex_Sky.setMinFilter(MinFilter.BilinearNearestMipMap);
		tex_Sky.setMagFilter(MagFilter.Bilinear);
		tex_Sky.setWrap(WrapMode.Repeat);
		
		if (moonMap != null) {
			tex_Sun = assetManager.loadTexture(sunMap);
			tex_Sun.setMinFilter(MinFilter.BilinearNearestMipMap);
			tex_Sun.setMagFilter(MagFilter.Bilinear);
			tex_Sun.setWrap(WrapMode.Repeat);
			
			tex_Moon = assetManager.loadTexture(moonMap);
			tex_Moon.setMinFilter(MinFilter.BilinearNearestMipMap);
			tex_Moon.setMagFilter(MagFilter.Bilinear);
			tex_Moon.setWrap(WrapMode.Repeat);
		}
		
		tex_Clouds = assetManager.loadTexture(cloudsMap);
		tex_Clouds.setMinFilter(MinFilter.BilinearNoMipMaps);
		tex_Clouds.setMagFilter(MagFilter.Bilinear);
		tex_Clouds.setWrap(WrapMode.Repeat);
		
		mat_Sky = new Material(assetManager, "MatDefs/SkyDome.j3md");
		mat_Sky.setTexture("SkyNightMap", tex_Sky);
		if (moonMap != null) {
			mat_Sky.setTexture("SunMap", tex_Sun);
			mat_Sky.setTexture("MoonMap", tex_Moon);
		//	mat_Sky.setFloat("MoonDirection", moonRotation);
			mat_Sky.setFloat("SunMoonSpeed", sunMoonSpeed);
		}
		mat_Sky.setColor("ColorDay", dayColor);
		mat_Sky.setColor("ColorNight", nightColor);
		mat_Sky.setColor("ColorSunRise", sunRiseColor);
		mat_Sky.setColor("ColorSunSet", sunSetColor);
		mat_Sky.setFloat("Alpha", dayAlpha);
		mat_Sky.setTexture("FogAlphaMap",tex_FogAlpha);
		mat_Sky.setTexture("CloudMap1", tex_Clouds);
		mat_Sky.setFloat("CloudDirection1", cloud1Rotation);
		mat_Sky.setFloat("CloudSpeed1", cloud1Speed);
		mat_Sky.setFloat("CloudDirection2", cloud2Rotation);
		mat_Sky.setFloat("CloudSpeed2", cloud2Speed);
		mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
		mat_Sky.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		mat_Sky.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
		mat_Sky.getAdditionalRenderState().setDepthWrite(false);
		
		skyNight = (Node)assetManager.loadModel(model);
		skyNight.setCullHint(Spatial.CullHint.Never);
		skyNight.setLocalScale(7f, 5f, 7f);
		skyNight.setMaterial(mat_Sky);
	}
	
	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
		((Node)spatial).attachChild(skyNight);
	}
	/**
	 * Enable the SkyDome control
	 * @param enabled 
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * Returns if the SkyDome control is enabled
	 * @return enabled 
	 */
	public boolean isEnabled() {
		return this.enabled;
	}
	
	// Transitions
	public boolean getIsDay() {
		return this.isDay;
	}
	/**
     * Sets the speed at which the transition from night to day/day to night happens
     * @param cycleSpeed   Default value is .125f
     */
	public void setDayNightTransitionSpeed(float cycleSpeed) {
		this.cycleSpeed = cycleSpeed;
	}
	/**
     * Gets the speed at which the transition from night to day/day to night happens
     * @return cycleSpeed  Default value is .125f
     */
	public float getDayNightTransitionSpeed() {
		return this.cycleSpeed;
	}
	/**
	 * Begins cycle day to night
	 */
	public void cycleDayToNight() {
		this.cycleD2N = true;
		this.cycleN2D = false;
	}
	/**
	 * Begins cycle night to day
	 */
	public void cycleNightToDay() {
		this.cycleD2N = false;
		this.cycleN2D = true;
	}
	
	// Fog
	/**
	 * Sets a pointer to the fog filter used by the JME application that initialized the SkyDome control
	 * @param fog  The FogFilter to adjust during transitions
	 * @param viewPort  The default ViewPort for background color manipulation used for fog blending
	 */
	public void setFogFilter(FogFilter fog, ViewPort viewPort) {
		this.fog = fog;
		this.viewPort = viewPort;
	}
	/**
     * Sets the day time fog color to use
     * @param fogColor  Default value is 0.7f, 0.7f, 0.7f, 0.6f
     */
	public void setFogColor(ColorRGBA fogColor) {
		this.fogColor = fogColor;
		if (mat_Sky != null)
			mat_Sky.setColor("FogColor", fogColor);
	}
	/**
     * Gets the day time fog color
     * @return fogColor  Default value is 0.7f, 0.7f, 0.7f, 0.6f
     */
	public ColorRGBA getFogColor() {
		return this.fogColor;
	}
	/**
     * Sets the night time fog color to use
     * @param fogNightColor  Default value is 0.3f, 0.3f, 0.3f, 0.6f
     */
	public void setFogNightColor(ColorRGBA fogNightColor) {
		this.fogNightColor = fogNightColor;
		if (mat_Sky != null)
			mat_Sky.setColor("FogNightColor", fogNightColor);
	}
	/**
     * Gets the night time fog color
     * @return fogNightColor  Default value is 0.3f, 0.3f, 0.3f, 0.6f
     */
	public ColorRGBA getFogNightColor() {
		return this.fogNightColor;
	}
	/**
     * Enable SkyDome to control the JME application FogFilter
     * @param controlFog  Default value is false
     */
	public void setControlFog(boolean controlFog) { this.controlFog = controlFog; }
	/**
     * Returns if SkyDome controls the JME application FogFilter
     * @return controlFog  Default value is false
     */
	public boolean getControlFog() { return this.controlFog; }
	
	// Sun
	/**
     * Pointer to the Directional Light used by your JME application as a sun light
     * @param sun
     */
	public void setSun(DirectionalLight sun) {
		this.sun = sun;
	}
	/**
     * Sets the color used by the sun during day time
     * @param sunDayLight  Default value is 1f, 1f, 1f, 1f
     */
	public void setSunDayLight(ColorRGBA sunDayLight) {
		this.sunDayLight = sunDayLight;
	}
	/**
     * Sets the color used by the sun during night time
     * @param sunNightLight  Default value is .45f, .45f, .45f, 1f
     */
	public void setSunNightLight(ColorRGBA sunNightLight) {
		this.sunNightLight = sunNightLight;
	}
	/**
     * Gets the color used by the sun during day time
     * @return sunDayLight  Default value is 1f, 1f, 1f, 1f
     */
	public ColorRGBA getSunDayLight() {
		return this.sunDayLight;
	}
	/**
     * Gets the color used by the sun during night time
     * @return sunNightLight  Default value is .45f, .45f, .45f, 1f
     */
	public ColorRGBA getSunNightLight() {
		return this.sunNightLight;
	}
	/**
     * Enable SkyDome to control the JME application DirectionLight
     * @param controlSun  Default value is false
     */
	public void setControlSun(boolean controlSun) { this.controlSun = controlSun; }
	/**
     * Returns if SkyDome controls the JME application DirectionalLight
     * @return controlSun  Default value is false
     */
	public boolean getControlSun() { return this.controlSun; }
	
	// Day time color
	/**
     * Sets the color used for day time sky
     * @param dayColor  Default value is .7f, .7f, 1f, 1f
     */
	public void setDaySkyColor(ColorRGBA dayColor) {
		this.dayColor = dayColor;
		if (mat_Sky != null)
			mat_Sky.setColor("ColorDay", dayColor);
	}
	/**
     * Gets the color used for day time sky
     * @return dayColor  Default value is .7f, .7f, 1f, 1f
     */
	public ColorRGBA getDaySkyColor() {
		return this.dayColor;
	}
	/**
     * Sets the color blended to the day time sky for transitioning from day to night/night to day
     * @param nightColor  Default value is .4f, .3f, .6f, 1f
     */
	public void setSkyNightColor(ColorRGBA nightColor) {
		this.nightColor = nightColor;
		if (mat_Sky != null)
			mat_Sky.setColor("ColorNight", nightColor);
	}
	/**
     * Gets the color blended to the day time sky for transitioning from day to night/night to day
     * @return nightColor  Default value is .4f, .3f, .6f, 1f
     */
	public ColorRGBA getSkyNightColor() {
		return this.nightColor;
	}
	
	// Moon
	/**
     * Sets the rotation/direction the moon moves in
     * @param moonRotation  Default value 75f
     */
	public void setMoonRotation(float moonRotation) {
		this.moonRotation = moonRotation;
		if (mat_Sky != null)
			mat_Sky.setFloat("MoonRotation", moonRotation);
	}
	/**
     * Gets the rotation/direction the moon moves in
     * @return moonRotation  Default value 75f
     */
	public float getMoonRotation() {
		return this.moonRotation;
	}
	/**
     * Sets the speed the sun and moon moves
     * @param sunMoonSpeed  Default value .0185f
     */
	public void setSunMoonSpeed(float sunMoonSpeed) {
		this.sunMoonSpeed = sunMoonSpeed;
		if (mat_Sky != null)
			mat_Sky.setFloat("SunMoonSpeed", sunMoonSpeed);
	}
	/**
     * Gets the speed the sun and moon moves
     * @return sunMoonSpeed  Default value .0185f
     */
	public float getSunMoonSpeed() {
		return this.sunMoonSpeed;
	}
	
	// Clouds
	/**
     * Sets the near cloud layer movement rotation/direction
     * @param cloudRotation  Default value FastMath.HALF_PI+0.02f
     */
	public void setCloudsNearRotation(float cloudRotation) {
		this.cloud2Rotation = cloudRotation;
		if (mat_Sky != null)
			mat_Sky.setFloat("CloudDirection2", cloudRotation);
	}
	/**
     * Gets the near cloud layer movement rotation/direction
     * @return cloud2Rotation  Default value FastMath.HALF_PI+0.02f
     */
	public float getCloudsNearRotation() {
		return this.cloud2Rotation;
	}
	/**
     * Sets the near cloud layer movement speed
     * @param cloudSpeed  Default value .05f
     */
	public void setCloudsNearSpeed(float cloudSpeed) {
		this.cloud2Speed = cloudSpeed;
		if (mat_Sky != null)
			mat_Sky.setFloat("CloudSpeed2", cloudSpeed);
	}
	/**
     * Gets the near cloud layer movement speed
     * @param cloud2Speed  Default value .05f
     */
	public float getCloudsNearSpeed() {
		return this.cloud2Speed;
	}
	/**
     * Sets the far cloud layer movement rotation/direction
     * @param cloudRotation  Default value FastMath.HALF_PI+0.023f
     */
	public void setCloudsFarRotation(float cloudRotation) {
		this.cloud1Rotation = cloudRotation;
		if (mat_Sky != null)
			mat_Sky.setFloat("CloudDirection1", cloudRotation);
	}
	/**
     * Gets the near cloud layer movement rotation/direction
     * @return cloud1Rotation  Default value FastMath.HALF_PI+0.02f
     */
	public float getCloudsFarRotation() {
		return this.cloud1Rotation;
	}
	/**
     * Sets the far cloud layer movement speed
     * @param cloudSpeed  Default value .025f
     */
	public void setCloudsFarSpeed(float cloudSpeed) {
		this.cloud1Speed = cloudSpeed;
		if (mat_Sky != null)
			mat_Sky.setFloat("CloudSpeed1", cloudSpeed);
	}
	/**
     * Gets the far cloud layer movement speed
     * @return cloud1Speed  Default value .025f
     */
	public float getCloudsFarSpeed() {
		return this.cloud1Speed;
	}
	
	/**
     * Sets the near and far cloud layers maximum opacity for cycling clouds in/out
     * @param cloudMaxOpacity  Default value 1f
     */
	public void setCloudMaxOpacity(float cloudMaxOpacity) {
		this.cloudMaxAlpha = cloudMaxOpacity;
	}
	/**
     * Gets the near and far cloud layers maximum opacity for cycling clouds in/out
     * @return cloudMaxOpacity  Default value 1f
     */
	public float getCloudMaxOpacity() {
		return this.cloudMaxAlpha;
	}
	/**
     * Sets the near and far cloud layers minimum opacity for cycling clouds in/out
     * @param cloudMinOpacity  Default value 0f
     */
	public void setCloudMinOpacity(float cloudMinOpacity) {
		this.cloudMinAlpha = cloudMinOpacity;
	}
	/**
     * Gets the near and far cloud layers minimum opacity for cycling clouds in/out
     * @return cloudMinOpacity  Default value 0f
     */
	public float getCloudMinOpacity() {
		return this.cloudMinAlpha;
	}
	/**
     * Sets the speed at which the near and far cloud layers are cycled in/out
     * @param cloudCycleSpeed  Default value .125f
     */
	public void setCloudCycleSpeed(float cloudCycleSpeed) {
		this.cloudCycleSpeed = cloudCycleSpeed;
	}
	/**
     * Gets the speed at which the near and far cloud layers are cycled in/out
     * @return cloudCycleSpeed  Default value .125f
     */
	public float getCloudCycleSpeed() {
		return this.cloudCycleSpeed;
	}
	
	// Color mix function
	/**
     * Blends two ColorRGBAs by the amount passed in
     * @param c1  The color being blended into
	 * @param c2  The color to blend
	 * @param amount  The amount of c2 to blend into c1
	 * @return r  The resulting ColorRGBA
     */
	private ColorRGBA mix(ColorRGBA c1, ColorRGBA c2, float amount) {
		ColorRGBA r = new ColorRGBA();
		r.interpolate(c1, c2, amount);
		return  r;
	}
	
	// Day to night/night to day cycles
	/**
	 * Begin cycle clouds in
	 */
	public void cycleCloudsIn() {
		this.cycleCI = true;
		this.cycleCO = false;
	}
	/**
	 * Begin cycle clouds out
	 */
	public void cycleCloudsOut() {
		this.cycleCI = false;
		this.cycleCO = true;
	}
	
	/**
	 * Initialize calendar for adjusting lighting and shadow renderer direction
	 * @param secondsPerMinute  Number of actual seconds per game time minute. Default value is 10
	 * @param minutesPerHour  Number of gametime minutes per game time hour. Default value is 60
	 * @param hoursPerDay  Number of game time hours per game time day. Default value is 24
	 * @param daysPerWeek  Number of game time days per game time week. Default value is 7
	 * @param weeksPerMonth  Number of game time weeks per game time month. Default value is 4
	 * @param monthsPerYear  Number of game time months per game time year. Default value is 24
	 */
	public void initializeCalendar(
			int secondsPerMinute,
			int minutesPerHour,
			int hoursPerDay,
			int daysPerWeek,
			int weeksPerMonth,
			int monthsPerYear
			) {
		this.secondsPerMinute = secondsPerMinute;
		this.minutesPerHour = minutesPerHour;
		this.hoursPerDay = hoursPerDay;
		this.daysPerWeek = daysPerWeek;
		this.weeksPerMonth = weeksPerMonth;
		this.monthsPerYear = monthsPerYear;
		this.daysPerYear = daysPerWeek*weeksPerMonth*monthsPerYear;
		
		this.secondsPerHour = this.secondsPerMinute*this.minutesPerHour;
		this.secondsPerDay = this.secondsPerHour*this.hoursPerDay;
		this.secondsPerWeek = this.secondsPerDay*this.daysPerWeek;
		this.secondsPerMonth = this.secondsPerWeek*this.weeksPerMonth;
		this.secondsPerYear = this.secondsPerMonth*this.monthsPerYear;
		
		cycleSpeed = 1.0f/(float)minutesPerHour;
		sunSpeed = 1.0f/(float)(hoursPerDay/4)/(float)minutesPerHour/(float)secondsPerMinute;
		sunDirection = new Vector3f(-1f+sunSpeed*(getHour(getTime())+6),-1f,0f);//-1f+sunSpeed*(getHour(getTime())+6));
		sunMoonSpeed = sunSpeed;
		sunOffsetXInc = sunSpeed/2;
		sunOffsetX = 0.5f+sunSpeed*(getHour(getTime()));
		sunRotation = -FastMath.QUARTER_PI;
		sunQ = sunQ.fromAngles(sunRotation, 0f, 0f);
		sunDirection = sunQ.mult(Vector3f.UNIT_Y.negate());
		if (moonMap != null) {
			this.mat_Sky.setFloat("SunMoonSpeed", sunMoonSpeed);
		}
		if (getHour(getTime()) >= 6 && getHour(getTime()) < 18)
			sunDay = true;
		else
			sunDay = false;
		mat_Sky.setBoolean("IsDay", sunDay);
		mat_Sky.setFloat("SunMoonOffsetX", sunOffsetX);
	}
	/**
	 * Sets the names of the months to be used by the game calendar
	 * @param months String[] containing the names of the game months
	 */
	public void setCalendarMonthNames(String[] months) {
		this.months = months;
	}
	/**
	 * Sets the current game year to Julian Calendar year + offset
	 * @param yearOffset  The number of years to add to the current Julian Calendar date year (use negative number to subtract). Default value is 0;
	 */
	public void setCalenderYearOffset(int yearOffset) {
		this.yearOffset = yearOffset;
	}
	/**
	 * Returns a string containing the current time/date in game time.
	 * @return gameTime  The current time based on game time calendar 
	 */
	public String getGDateTime() {
		long time = getTime();
		
		String sHour, sMinute, sDay, sWeek;
		int hour = getHour(time);
		int minute = getMinute(time);
		if (hour < 10)	sHour = "0" + String.valueOf(hour);
		else			sHour = String.valueOf(hour);
		if (minute < 10)sMinute = "0" + String.valueOf(minute);
		else			sMinute = String.valueOf(minute);
		
		int day = getDay(time);
		sDay = String.valueOf(day) + getSuffix(day);
		int week = getWeek(time);
		sWeek = String.valueOf(week) + getSuffix(week);
		String ret = "[Time] " + sHour + ":" + sMinute + " on the " + sDay + " day of the " + sWeek + " week of " + getMonth(time) + " in the year " + String.valueOf(getYear(time)) + ".";
		return ret;
	}
	/**
	 * Enables automation of transitions based on calendar's time of day.
	 * Required for handling sunlight direction updates
	 * Required for handling shadow direction updates
	 * @param useCalendar 
	 */
	public void setUseCalendar(boolean useCalendar) {
		this.useCalendar = useCalendar;
	}
	/**
	 * Returns if transition automation based on calendar's time of day is enabled
	 * @return useCalendar
	 */
	public boolean getUseCalendar() {
		return this.useCalendar;
	}
	private long getTime() {
		Calendar cal = Calendar.getInstance();
		return (cal.getTimeInMillis()/1000);
	}
	private String getSuffix(int num) {
		String ret = "";
		switch (num) {
			case 1:
				ret += "st";
				break;
			case 2:
				ret += "nd";
				break;
			case 3:
				ret += "rd";
				break;
			default:
				ret += "th";
				break;
		}
		return ret;
	}
	private int getYear(long time) {
		return (int)((time/secondsPerYear)+yearOffset);
	}
	private String getMonth(long time) {
		long rem = (time%secondsPerYear);
		int month = (int)(rem/secondsPerMonth);
		return months[month];
	}
	private int getMonthAsInt(long time) {
		long rem = (time%secondsPerYear);
		return (int)(rem/secondsPerMonth);
	}
	private int getWeek(long time) {
		long rem = (time%secondsPerYear);
		long mRem = (rem%secondsPerMonth);
		return (int)(mRem/secondsPerWeek)+1;
	}
	private int getDay(long time) {
		long rem = (time%secondsPerYear);
		long mRem = (rem%secondsPerMonth);
		long wRem = (mRem%secondsPerWeek);
		return (int)(wRem/secondsPerDay)+1;
	}
	private int getHour(long time) {
		long rem = (time%secondsPerYear);
		long mRem = (rem%secondsPerMonth);
		long wRem = (mRem%secondsPerWeek);
		long dRem = (wRem%secondsPerDay);
		return (int)(dRem/secondsPerHour);
	}
	private int getMinute(long time) {
		long rem = (time%secondsPerYear);
		long mRem = (rem%secondsPerMonth);
		long wRem = (mRem%secondsPerWeek);
		long dRem = (wRem%secondsPerDay);
		long hRem = (dRem%secondsPerHour);
		return (int)(hRem/secondsPerMinute);
	}
	
	public void update(float tpf) {
		if (spatial != null && enabled) {
			Vector3f camLoc = cam.getLocation();
			float[] camLF = camLoc.toArray(null);
			spatial.setLocalTranslation(camLF[0], camLF[1]+.25f, camLF[2]);
			
			if (getUseCalendar()) {
				if (getHour(getTime()) == 6) {
					cycleN2D = true; cycleD2N = false; sunDay = true; //horizonAlpha = 1.0f;
					mat_Sky.setBoolean("IsDay", sunDay);
				}
				if (getHour(getTime()) == 18) {
					cycleN2D = false; cycleD2N = true; sunDay = false; //horizonAlpha = 0.0f;
					mat_Sky.setBoolean("IsDay", sunDay);
				}
				
				if (sunDay) {
					sunRotation += tpf*this.sunSpeed;
					if (sunRotation > FastMath.QUARTER_PI) sunRotation = FastMath.QUARTER_PI;
					sunOffsetX -= (tpf*sunOffsetXInc);
					if (sunOffsetX < -0.5f) sunOffsetX = -0.5f;
				} else {
					sunRotation -= tpf*this.sunSpeed;
					if (sunRotation < -FastMath.QUARTER_PI) sunRotation = -FastMath.QUARTER_PI;
					sunOffsetX += (tpf*sunOffsetXInc);
					if (sunOffsetX > 0.5f) sunOffsetX = 0.5f;
				}
				mat_Sky.setFloat("SunMoonOffsetX", sunOffsetX);
				
				sunQ = sunQ.fromAngles(0f, 0f, sunRotation);
				sunDirection = sunQ.mult(new Vector3f(.2f,-1f,.2f));
				sun.setDirection(sunDirection);
			}
			
			// Day/Night Cycle
			if (cycleN2D) {
				if (dayAlpha < 1.0f) {
					dayAlpha += tpf * cycleSpeed;
					horizonAlpha = 1.0f - dayAlpha;
					mat_Sky.setFloat("Alpha", dayAlpha);
					mat_Sky.setFloat("HorizonAlpha", horizonAlpha);
					if (fog != null && controlFog) { 
						viewPort.setBackgroundColor(mix(fogNightColor,fogColor,dayAlpha));
						fog.setFogColor(mix(fogNightColor,fogColor,dayAlpha));
					}
					if (controlSun) sun.setColor(mix(sunNightLight,sunDayLight,dayAlpha));
				} else {
					dayAlpha = 1.0f;
					horizonAlpha = 0.0f;
					mat_Sky.setFloat("Alpha", dayAlpha);
					mat_Sky.setFloat("HorizonAlpha", horizonAlpha);
					if (fog != null && controlFog) { 
						viewPort.setBackgroundColor(fogColor);
						fog.setFogColor(fogColor);
					}
					if (controlSun) sun.setColor(sunDayLight);
					cycleN2D = false;
				}
			} else if (cycleD2N) {
				if (dayAlpha > 0.0f) {
					dayAlpha -= tpf * cycleSpeed;
					horizonAlpha = 1.0f - dayAlpha;
					mat_Sky.setFloat("Alpha", dayAlpha);
					mat_Sky.setFloat("HorizonAlpha", horizonAlpha);
					if (fog != null && controlFog) { 
						viewPort.setBackgroundColor(mix(fogNightColor,fogColor,dayAlpha));
						fog.setFogColor(mix(fogNightColor,fogColor,dayAlpha));
					}
					if (controlSun) sun.setColor(mix(sunNightLight,sunDayLight,dayAlpha));
				} else {
					dayAlpha = 0.0f;
					horizonAlpha = 1.0f;
					mat_Sky.setFloat("Alpha", dayAlpha);
					mat_Sky.setFloat("HorizonAlpha", horizonAlpha);
					if (fog != null && controlFog) { 
						viewPort.setBackgroundColor(fogNightColor);
						fog.setFogColor(fogNightColor);
					}
					if (controlSun) sun.setColor(sunNightLight);
					cycleD2N = false;
				}
			}

			// Clouds Cycle
			if (cycleCI) {
				if (cloudsAlpha < cloudMaxAlpha) {
					cloudsAlpha += tpf * cloudCycleSpeed;
					mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
				} else {
					cloudsAlpha = cloudMaxAlpha;
					mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
					cycleCI = false;
				}
			} else if (cycleCO) {
				if (cloudsAlpha > cloudMinAlpha) {
					cloudsAlpha -= tpf * cloudCycleSpeed;
					mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
				} else {
					cloudsAlpha = cloudMinAlpha;
					mat_Sky.setFloat("CloudsAlpha", cloudsAlpha);
					cycleCO = false;
				}
			}
		}
	}
	
	public void render(RenderManager rm, ViewPort vp) {
		
	}
	
	public Control cloneForSpatial(Spatial spatial) {
		SkyDome control = new SkyDome(this.assetManager, this.cam,
				this.model,
				this.nightSkyMap,
				this.sunMap,
				this.moonMap,
				this.cloudsMap,
				this.fogAlphaMap);
        control.spatial.addControl(control);
        return control;
	}
	public void write(JmeExporter ex) throws IOException {
		OutputCapsule oc = ex.getCapsule(this);
		oc.write(enabled, "enabled", true);
		oc.write(spatial, "spatial", null);
	}
	public void read(JmeImporter im) throws IOException {
		InputCapsule ic = im.getCapsule(this);
		enabled = ic.readBoolean("enabled", true);
		spatial = (Spatial) ic.readSavable("spatial", null);
	}
}
