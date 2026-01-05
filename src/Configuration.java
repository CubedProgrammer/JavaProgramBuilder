import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
public final class Configuration
{
	public static final String CURLIES="{}";
	public ArrayList<String>classpath;
	public ArrayList<String>options;
	public ArrayList<String>executeOptions;
	public String output;
	public String artifact;
	public String main;
	public void setOutput(String output)
	{
		this.output=output;
	}
	public void setArtifact(String artifact)
	{
        this.artifact=artifact;
    }
    public void setMain(String main)
	{
        this.main=main;
    }
	public void addToCP(String path)
	{
		this.classpath.add(path);
	}
	public void addToOptions(String option)
	{
		this.options.add(option);
	}
	public void addToExecuteOptions(String option)
	{
		this.executeOptions.add(option);
	}
	public static Configuration parseConfiguration(String[]args)
	{
		Configuration configuration=new Configuration();
		int depth=0;
		Optional<Consumer<String>>setter=Optional.empty();
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
					setter=Optional.empty();
				}
			}
			else if(s.charAt(0)=='-')
			{
				setter=switch(s.charAt(1))
				{
					case'a'->Optional.of(configuration::setArtifact);
					case'c'->Optional.of(configuration::addToOptions);
					case'e'->Optional.of(configuration::addToExecuteOptions);
					case'm'->Optional.of(configuration::setMain);
					case'o'->Optional.of(configuration::setOutput);
					case'p'->Optional.of(configuration::addToCP);
					default->Optional.empty();
				};
			}
			else if(!setter.isEmpty())
			{
				setter.get().accept(s);
				if(depth==0)
				{
					setter=Optional.empty();
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
		this.executeOptions=new ArrayList<String>();
		this.output="";
		this.artifact="";
	}
}
