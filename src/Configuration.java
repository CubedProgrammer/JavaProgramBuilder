import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;
public class Configuration
{
	public static final String CURLIES="{}";
	public ArrayList<String>classpath;
	public ArrayList<String>options;
	public String output;
	public String artifact;
	public String main;
	public static Optional<Configuration>parseConfigurationFile(File file)
	{
		Optional<Configuration>o=Optional.empty();
		Configuration configuration=new Configuration();
		int depth=0;
		try(Scanner scanner=new Scanner(new BufferedReader(new FileReader(file))))
		{
			Optional<Field>fieldO=Optional.empty();
			while(scanner.hasNextLine())
			{
				String s=scanner.nextLine().strip();
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
						fieldO.get().set(configuration,s);
					}
					else
					{
						@SuppressWarnings("unchecked")
						var array=(ArrayList<String>)fieldO.get().get(configuration);
						array.add(s);
					}
					if(depth==0)
					{
						fieldO=Optional.empty();
					}
				}
			}
			o=Optional.of(configuration);
		}
		catch(FileNotFoundException|IllegalAccessException e){}
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
