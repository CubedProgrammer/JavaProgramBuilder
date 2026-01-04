import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
public final class Configuration
{
	public static final String CURLIES="{}";
	public ArrayList<String>classpath;
	public ArrayList<String>options;
	public String output;
	public String artifact;
	public String main;
	public static Configuration parseConfiguration(String[]args)
	{
		Configuration configuration=new Configuration();
		int depth=0;
		Optional<Field>fieldO=Optional.empty();
		for(String s:args)
		{
			s=s.strip();
			if(CURLIES.substring(0,1).equals(s))
			{
				++depth;
			}
			else if(CURLIES.substring(1,2).equals(s))
			{
				--depth;
				if(depth==0)
				{
					fieldO=Optional.empty();
				}
			}
			else if(fieldO.isEmpty())
			{
				Field field=null;
				try
				{
					field=Configuration.class.getField(s.toLowerCase());
				}
				catch(NoSuchFieldException e){}
				fieldO=Optional.ofNullable(field);
			}
			else
			{
				if(String.class.equals(fieldO.get().getType()))
				{
					try
					{
						fieldO.get().set(configuration,s);
					}
					catch(IllegalAccessException e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					try
					{
						@SuppressWarnings("unchecked")
						var array=(ArrayList<String>)fieldO.get().get(configuration);
						array.add(s);
					}
					catch(IllegalAccessException e)
					{
						e.printStackTrace();
					}
				}
				if(depth==0)
				{
					fieldO=Optional.empty();
				}
			}
		}
		return configuration;
	}
	public static Optional<Configuration>parseConfigurationFile(File file)
	{
		Optional<Configuration>o=Optional.empty();
		try(BufferedReader reader=new BufferedReader(new FileReader(file)))
		{
			char[]buf=new char[1024];
			StringBuilder builder=new StringBuilder();
			for(int b=reader.read(buf);b>0;b=reader.read(buf))
			{
				builder.append(buf,0,b);
			}
			o=Optional.of(parseConfiguration(builder.toString().split("\n")));
		}
		catch(IOException e){}
		return o;
	}
    @SuppressWarnings("Convert2Diamond")
	public Configuration()
	{
		this.classpath=new ArrayList<String>();
		this.options=new ArrayList<String>();
		this.output="";
		this.artifact="";
	}
}
