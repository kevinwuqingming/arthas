package com.mechanist.configs;

import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.utils.PropertiesUtils;

import java.util.Properties;

public class Config
{
	public static void load(String arthasConfigDir)
	{
		try
		{
			// 覆盖配置
			Properties[] overrideProps = null;
			// 加载目录下的所有配置项
			Properties[] initialProperties = PropertiesUtils.loadAllFromDirectory(arthasConfigDir);
			// 替换配置项
			PropertiesUtils.overrideProperties(initialProperties, overrideProps);

			// 把值写入具体的配置类
			ConfigurableProcessor.process(ArthasConfig.class, initialProperties);
			System.out.println("[ArthasConfig] Loading: ArthasConfig.properties");
		}
		catch (Exception e)
		{
			System.out.println("ArthasConfig] Can't load ArthasConfig configuration: " + e.getMessage());
			throw new Error("[ArthasConfig] Can't load ArthasConfig configuration: ", e);
		}
	}
}
