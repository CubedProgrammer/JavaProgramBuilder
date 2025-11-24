import java.io.File;
import java.io.IOException;
public class Main
{
	public static void main(String[]args)throws IOException
	{
		final String current=new File(".").getCanonicalFile().getName();
		final File conffile=new File(current+".conf");
		final Configuration configuration=Configuration.parseConfigurationFile(conffile).orElse(new Configuration());
		final JPBuild builder=new JPBuild(configuration);
		builder.jpbuild();
	}
}
