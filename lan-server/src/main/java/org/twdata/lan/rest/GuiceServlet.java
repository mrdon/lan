package org.twdata.lan.rest;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import javax.servlet.ServletConfig;

/**
 * Integrate Guice into Jersey-based servlets.
 *
 * @author Gili Tzabari
 */
@Singleton
public class GuiceServlet extends ServletContainer
{
	private static final long serialVersionUID = 0L;

    @Override
	protected void initiate(ResourceConfig config, WebApplication webapp)
	{
		try
		{
			ServletConfig servletConfig = getServletConfig();
			String moduleName = servletConfig.getInitParameter("moduleClass");
			String injectorContextAttribute = servletConfig.getInitParameter("injectorContextAttribute");
            Injector injector;
            if (moduleName != null)
			{
				Module module = (Module) Class.forName(moduleName).newInstance();
				injector = Guice.createInjector(module);
			}
			else if (injectorContextAttribute != null)
			{
				injector = (Injector) servletConfig.getServletContext().getAttribute(
					injectorContextAttribute);
			}
			else
				throw new IllegalArgumentException("GuiceServlet requires init parameter \"moduleClass\" that " +
					"specifies the fully-qualified path of the Guice module, or \"injectorContextAttribute\" that " +
					"specifies the name of a context attribute that holds an Injector instance.");
			webapp.initiate(config, new GuiceComponentProviderFactory(config, injector));
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}
}