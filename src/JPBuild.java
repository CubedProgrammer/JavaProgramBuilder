import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public final class JPBuild
{
	public static final String LAUNCHER_REALPATH="char jar[4096];realpath(\"/proc/self/exe\",jar);*strrchr(jar,'/')=0;strcat(jar,\"/%s\");";
	public static final String LAUNCHER_CODE="char*args[256]={\"java\",\"-jar\",jar};memcpy(args+3,a+1,(l-1)*sizeof(char*const));return execvp(\"java\",args);";
	public static final String LAUNCHER_BOILERPLATE="#include<stdlib.h>\n#include<string.h>\n#include<unistd.h>\nint main(int l,char**a){%s}\n";
	public static final void runCommand(String[]command)
	{
		try
		{
			ProcessBuilder builder=new ProcessBuilder(command);
			builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
			builder.redirectError(ProcessBuilder.Redirect.INHERIT);
			Process p=builder.start();
			p.waitFor();
		}
		catch(InterruptedException|IOException e){}
	}
	private final Configuration options;
	public JPBuild(Configuration options)
	{
		this.options=options;
	}
	public void jpbuild()
	{
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
								if(options.display)
								{
									System.out.println(String.join(" ",cmd));
								}
								runCommand(cmd);
							}
						}
					});
				}
				catch(IOException e){}
			}
		}
	}
	public void execute()
	{
		options.classpath.add(options.output);
		String cp=String.join(":",options.classpath);
		String[]firstargs={"java","-cp",cp,options.main};
		String[]cmd=new String[options.executeOptions.size()+firstargs.length];
        System.arraycopy(firstargs,0,cmd,0,firstargs.length);
		for(int i=0;i<options.executeOptions.size();i++)
		{
			cmd[i+firstargs.length]=options.executeOptions.get(i);
		}
		runCommand(cmd);
	}
	public void archive()
	{
		String[]cmd={"jar","cfe",options.artifact,options.main,"-C",options.output,"."};
		runCommand(cmd);
	}
	public void makeNativeLauncher()
	{
		String code=String.format(LAUNCHER_BOILERPLATE,String.format(LAUNCHER_REALPATH,options.artifact)+LAUNCHER_CODE);
		try(FileOutputStream out=new FileOutputStream(options.artifact+".c"))
		{
			out.write(code.getBytes());
		}
		catch(IOException e){}
	}
}
