#import "Common/ShaderLib/Parallax.glsllib"
#import "Common/ShaderLib/Optics.glsllib"
#define ATTENUATION
//#define HQ_ATTENUATION

varying float fadeDepth;
uniform float m_FadeStartDistance;
uniform float m_FadeMaxDistance;

varying vec4 vtVertex;
varying vec3 vtNormal;

varying vec2 texCoord;
#ifdef SEPARATE_TEXCOORD
  varying vec2 texCoord2;
#endif

#ifdef HAS_DIF_1
  uniform sampler2D m_DiffuseMap1;
  uniform float m_DiffuseScale1;
#endif
#ifdef HAS_DIF_2
  uniform sampler2D m_DiffuseMap2;
  uniform float m_DiffuseScale2;
#endif

#ifdef HAS_NORM_1
  uniform sampler2D m_NormalMap1;
#endif
#ifdef HAS_NORM_2
  uniform sampler2D m_NormalMap2;
#endif

varying vec3 AmbientSum;
varying vec4 DiffuseSum;
varying vec3 SpecularSum;

#ifndef VERTEX_LIGHTING
  uniform vec4 g_LightDirection;
  //varying vec3 vPosition;
  varying vec3 vViewDir;
  varying vec4 vLightDir;
  varying vec3 lightVec;
#else
  varying vec2 vertexLightValues;
#endif

#ifdef DIFFUSEMAP
  uniform sampler2D m_DiffuseMap;
#endif

#ifdef SPECULARMAP
  uniform sampler2D m_SpecularMap;
#endif

#ifdef PARALLAXMAP
  uniform sampler2D m_ParallaxMap;  
#endif
#if (defined(PARALLAXMAP) || (defined(NORMALMAP_PARALLAX) && defined(NORMALMAP))) && !defined(VERTEX_LIGHTING) 
    uniform float m_ParallaxHeight;
#endif

#ifdef LIGHTMAP
  uniform sampler2D m_LightMap;
#endif
  
#ifdef NORMALMAP
  uniform sampler2D m_NormalMap;   
#else
  varying vec3 vNormal;
#endif

#ifdef ALPHAMAP
  uniform sampler2D m_AlphaMap;
#endif

#ifdef COLORRAMP
  uniform sampler2D m_ColorRamp;
#endif

uniform float m_AlphaDiscardThreshold;

#ifndef VERTEX_LIGHTING
uniform float m_Shininess;

#ifdef HQ_ATTENUATION
uniform vec4 g_LightPosition;
#endif

#ifdef USE_REFLECTION 
    uniform float m_ReflectionPower;
    uniform float m_ReflectionIntensity;
    varying vec4 refVec;

    uniform ENVMAP m_EnvMap;
#endif

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    #ifdef V_TANGENT
        d = 1.0 - d*d;
        return step(0.0, d) * sqrt(d);
    #else
        return d;
    #endif
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    #ifdef MINNAERT
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
        return NdotL * pow(max(NdotL * NdotV, 0.1), -1.0) * 0.5;
    #else
        return max(0.0, dot(norm, lightdir));
    #endif
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shiny){
    // NOTE: check for shiny <= 1 removed since shininess is now 
    // 1.0 by default (uses matdefs default vals)
    #ifdef LOW_QUALITY
       // Blinn-Phong
       // Note: preferably, H should be computed in the vertex shader
       vec3 H = (viewdir + lightdir) * vec3(0.5);
       return pow(max(tangDot(H, norm), 0.0), shiny);
    #elif defined(WARDISO)
        // Isotropic Ward
        vec3 halfVec = normalize(viewdir + lightdir);
        float NdotH  = max(0.001, tangDot(norm, halfVec));
        float NdotV  = max(0.001, tangDot(norm, viewdir));
        float NdotL  = max(0.001, tangDot(norm, lightdir));
        float a      = tan(acos(NdotH));
        float p      = max(shiny/128.0, 0.001);
        return NdotL * (1.0 / (4.0*3.14159265*p*p)) * (exp(-(a*a)/(p*p)) / (sqrt(NdotV * NdotL)));
    #else
       // Standard Phong
       vec3 R = reflect(-lightdir, norm);
       return pow(max(tangDot(R, viewdir), 0.0), shiny);
    #endif
}

vec2 computeLighting(in vec3 wvNorm, in vec3 wvViewDir, in vec3 wvLightDir){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir, wvViewDir);
   float specularFactor = lightComputeSpecular(wvNorm, wvViewDir, wvLightDir, m_Shininess);

   #ifdef HQ_ATTENUATION
    float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
   #else
    float att = vLightDir.w;
   #endif

   if (m_Shininess <= 1.0) {
       specularFactor = 0.0; // should be one instruction on most cards ..
   }

   specularFactor *= diffuseFactor;

   return vec2(diffuseFactor, specularFactor) * vec2(att);
}
#endif

#if defined(HAS_DIF_1) && defined(HAS_DIF_2)
vec4 compositeTerrain(in vec2 newTexCoord, in vec4 diffuseColor) {
	vec4 ret = vec4(diffuseColor);
	
	vec3 blending = abs( vtNormal );
    blending = (blending -0.2) * 0.7;
    blending = normalize(max(blending, 0.00001));      // Force weights to sum to 1.0 (very important!)
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);

    // texture coords
    vec4 coords = vtVertex;
	vec4 col1;
	vec4 col2;
	vec4 col3;
	
	col1 = texture2D( m_DiffuseMap2, coords.yz * m_DiffuseScale2 );
	col2 = texture2D( m_DiffuseMap1, coords.xz * m_DiffuseScale1 );
	col3 = texture2D( m_DiffuseMap2, coords.xy * m_DiffuseScale2 );
	
	// blend the results of the 3 planar projections.
    ret = col1 * blending.x;
	ret += col3 * blending.z;
	ret += col2 * blending.y;
	
	return ret;
}
#endif

#if defined(HAS_NORM_1) && defined(HAS_NORM_2)
vec4 compositeNormals(in vec2 newTexCoord, in vec4 normalColor) {
	vec4 ret = vec4(normalColor);
	
	vec3 blending = abs( vtNormal );
    blending = (blending -0.2) * 0.7;
    blending = normalize(max(blending, 0.00001));      // Force weights to sum to 1.0 (very important!)
    float b = (blending.x + blending.y + blending.z);
    blending /= vec3(b, b, b);

    // texture coords
    vec4 coords = vtVertex;
	vec4 col1;
	vec4 col2;
	vec4 col3;
	
	col1 = texture2D( m_NormalMap2, coords.yz * m_DiffuseScale2 );
	col2 = texture2D( m_NormalMap1, coords.xz * m_DiffuseScale1 );
	col3 = texture2D( m_NormalMap2, coords.xy * m_DiffuseScale2 );
	
	// blend the results of the 3 planar projections.
    ret = col1 * blending.x;
	ret += col3 * blending.z;
	ret += col2 * blending.y;
	
	return ret;
}
#endif

void main(){
    vec2 newTexCoord;
     
    #if (defined(PARALLAXMAP) || (defined(NORMALMAP_PARALLAX) && defined(NORMALMAP))) && !defined(VERTEX_LIGHTING) 
     
       #ifdef STEEP_PARALLAX
           #ifdef NORMALMAP_PARALLAX
               //parallax map is stored in the alpha channel of the normal map         
               newTexCoord = steepParallaxOffset(m_NormalMap, vViewDir, texCoord, m_ParallaxHeight);
           #else
               //parallax map is a texture
               newTexCoord = steepParallaxOffset(m_ParallaxMap, vViewDir, texCoord, m_ParallaxHeight);         
           #endif
       #else
           #ifdef NORMALMAP_PARALLAX
               //parallax map is stored in the alpha channel of the normal map         
               newTexCoord = classicParallaxOffset(m_NormalMap, vViewDir, texCoord, m_ParallaxHeight);
           #else
               //parallax map is a texture
               newTexCoord = classicParallaxOffset(m_ParallaxMap, vViewDir, texCoord, m_ParallaxHeight);
           #endif
       #endif
    #else
       newTexCoord = texCoord;    
    #endif
    
   #if defined(HAS_DIF_1) && defined(HAS_DIF_2)
      vec4 diffuseColor = compositeTerrain(newTexCoord, vec4(0.0));
    #else
      vec4 diffuseColor = vec4(1.0);
    #endif

    float alpha = DiffuseSum.a * diffuseColor.a;
    #ifdef ALPHAMAP
       alpha = alpha * texture2D(m_AlphaMap, newTexCoord).r;
    #endif
    if(alpha < m_AlphaDiscardThreshold){
        discard;
    }

    #ifndef VERTEX_LIGHTING
        float spotFallOff = 1.0;

        #if __VERSION__ >= 110
          // allow use of control flow
          if(g_LightDirection.w != 0.0){
        #endif

          vec3 L       = normalize(lightVec.xyz);
          vec3 spotdir = normalize(g_LightDirection.xyz);
          float curAngleCos = dot(-L, spotdir);             
          float innerAngleCos = floor(g_LightDirection.w) * 0.001;
          float outerAngleCos = fract(g_LightDirection.w);
          float innerMinusOuter = innerAngleCos - outerAngleCos;
          spotFallOff = (curAngleCos - outerAngleCos) / innerMinusOuter;

          #if __VERSION__ >= 110
              if(spotFallOff <= 0.0){
                  gl_FragColor.rgb = AmbientSum * diffuseColor.rgb;
                  gl_FragColor.a   = alpha;
                  return;
              }else{
                  spotFallOff = clamp(spotFallOff, 0.0, 1.0);
              }
             }
          #else
             spotFallOff = clamp(spotFallOff, step(g_LightDirection.w, 0.001), 1.0);
          #endif
     #endif
 
    // ***********************
    // Read from textures
    // ***********************
    #if defined(HAS_NORM_1) && defined(HAS_NORM_2) && !defined(VERTEX_LIGHTING)
		vec4 normalHeight = compositeNormals(newTexCoord, vec4(0.0));
		vec3 normal = normalize((normalHeight.xyz * vec3(2.0) - vec3(1.0)));
		#ifdef LATC
		  normal.z = sqrt(1.0 - (normal.x * normal.x) - (normal.y * normal.y));
		#endif
	//normal.y = -normal.y;
    #elif !defined(VERTEX_LIGHTING)
      vec3 normal = vNormal;
      #if !defined(LOW_QUALITY) && !defined(V_TANGENT)
         normal = normalize(normal);
      #endif
    #endif

    #ifdef SPECULARMAP
      vec4 specularColor = texture2D(m_SpecularMap, newTexCoord);
    #else
      vec4 specularColor = vec4(1.0);
    #endif

    #ifdef LIGHTMAP
       vec3 lightMapColor;
       #ifdef SEPARATE_TEXCOORD
          lightMapColor = texture2D(m_LightMap, texCoord2).rgb;
       #else
          lightMapColor = texture2D(m_LightMap, texCoord).rgb;
       #endif
       specularColor.rgb *= lightMapColor;
       diffuseColor.rgb  *= lightMapColor;
    #endif

    #ifdef VERTEX_LIGHTING
       vec2 light = vertexLightValues.xy;
       #ifdef COLORRAMP
           light.x = texture2D(m_ColorRamp, vec2(light.x, 0.0)).r;
           light.y = texture2D(m_ColorRamp, vec2(light.y, 0.0)).r;
       #endif

       gl_FragColor.rgb =  AmbientSum     * diffuseColor.rgb + 
                           DiffuseSum.rgb * diffuseColor.rgb  * vec3(light.x) +
                           SpecularSum    * specularColor.rgb * vec3(light.y);
    #else
       vec4 lightDir = vLightDir;
       lightDir.xyz = normalize(lightDir.xyz);
       vec3 viewDir = normalize(vViewDir);

       vec2   light = computeLighting(normal, viewDir, lightDir.xyz) * spotFallOff;
       #ifdef COLORRAMP
           diffuseColor.rgb  *= texture2D(m_ColorRamp, vec2(light.x, 0.0)).rgb;
           specularColor.rgb *= texture2D(m_ColorRamp, vec2(light.y, 0.0)).rgb;
       #endif

       // Workaround, since it is not possible to modify varying variables
       vec4 SpecularSum2 = vec4(SpecularSum, 1.0);
       #ifdef USE_REFLECTION
            vec4 refColor = Optics_GetEnvColor(m_EnvMap, refVec.xyz);

            // Interpolate light specularity toward reflection color
            // Multiply result by specular map
            specularColor = mix(SpecularSum2 * light.y, refColor, refVec.w) * specularColor;

            SpecularSum2 = vec4(1.0);
            light.y = 1.0;
       #endif

       gl_FragColor.rgb =  AmbientSum       * diffuseColor.rgb  +
                           DiffuseSum.rgb   * diffuseColor.rgb  * vec3(light.x) +
                           SpecularSum2.rgb * specularColor.rgb * vec3(light.y);
    #endif
    gl_FragColor.a = alpha;
	
	#if defined(USE_FADE)
		float linDistance1 = 2.0 * fadeDepth - 1.0;
		gl_FragColor.a = clamp(1.0-((linDistance1 - m_FadeStartDistance) / (m_FadeMaxDistance - m_FadeStartDistance)), 0.0, 1.0);
	#endif
}
