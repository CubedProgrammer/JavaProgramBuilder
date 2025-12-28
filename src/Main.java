import java.io.File;
import java.io.IOException;
import java.util.Arrays;
public final class Main
{
	public static final int BUILD=0;
	public static final int EXECUTE=1;
	public static final int getMode(String[]args)
	{
		int mode=BUILD;
		if(args.length>0)
		{
			mode=switch(args[0])
			{
				case"build"->BUILD;
				case"run"->EXECUTE;
				default->BUILD;
			};
		}
		return mode;
	}
	public static final void main(String[]args)throws IOException
	{
		final String current=new File(".").getCanonicalFile().getName();
		final File conffile=new File(current+".conf");
		final Configuration configuration=Configuration.parseConfigurationFile(conffile).orElse(new Configuration());
		final JPBuild builder=new JPBuild(configuration);
		switch(getMode(args))
		{
			case BUILD->builder.jpbuild();
			case EXECUTE->builder.execute(Arrays.copyOfRange(args,1,args.length));
		}
	}
}
