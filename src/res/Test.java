/**
 * Simple program that illustrates reading XML values.
 */

package res;

public class Test
{
	public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java Test [configuration file]");
            System.exit(0);
        }

		Configuration configutor = null;
	
		try {
			configutor = new Configuration(args[0]);

		    System.out.println("Logfile = " + configutor.getLogFile());
		    System.out.println("Document Root = " + configutor.getDocumentRoot());
		    System.out.println("Default Document = " + configutor.getDefaultDocument());
		    System.out.println("Server Name = " +  configutor.getServerName());
		}
		catch (ConfigurationException ce) {
			System.out.println(ce);
			System.exit(0);
		}
	}
}
