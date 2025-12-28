import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public final class JPBuild
{
	private final Configuration options;
	public JPBuild(Configuration options)
	{
		this.options=options;
	}
	public void jpbuild()
	{
		Runtime rt=Runtime.getRuntime();
		String cp=String.join(":",options.classpath);
		String[]cmd=new String[options.options.size()+6];
		cmd[0]="javac";
		cmd[1]="-cp";
		cmd[2]=cp;
		cmd[3]="-d";
		cmd[4]=options.output;
		for(int i=0;i<options.options.size();i++)
		{
			cmd[i+5]=options.options.get(i);
		}
		for(String s:options.classpath)
		{
			File f=new File(s);
			if(f.isDirectory())
			{
				try
				{
					var files=Files.walk(Paths.get(s));
					files.map(Path::toFile).filter(File::isFile).forEach((file)->
					{
						String fileString=file.getPath();
						if(fileString.endsWith(".java"))
						{
							String classString=options.output+"/"+fileString.substring(s.length()+1,fileString.length()-5)+".class";
							File classFile=new File(classString);
							if(file.lastModified()>classFile.lastModified())
							{
								cmd[cmd.length-1]=fileString;
								System.out.println(String.join(" ",cmd));
								try
								{
									Process p=rt.exec(cmd);
									p.waitFor();
								}
								catch(InterruptedException|IOException e){}
							}
						}
					});
				}
				catch(IOException e){}
			}
		}
	}
	public void execute(String[]args)
	{
		options.classpath.add(options.output);
		String cp=String.join(":",options.classpath);
		String[]firstargs={"java","-cp",cp,options.main};
		String[]cmd=new String[args.length+firstargs.length];
        System.arraycopy(firstargs,0,cmd,0,firstargs.length);
		System.arraycopy(args,0,cmd,firstargs.length,args.length);
		try
		{
			ProcessBuilder builder=new ProcessBuilder(cmd);
			builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectError(ProcessBuilder.Redirect.INHERIT);
			Process p=builder.start();
			p.waitFor();
		}
		catch(InterruptedException|IOException e){}
	}
}
