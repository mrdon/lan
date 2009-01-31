package org.twdata.lan.rest;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCInstantiatedComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Integrates Guice into Jersey.
 *
 * @author Gili Tzabari
 */
public class GuiceComponentProviderFactory implements IoCComponentProviderFactory
{
	private final Logger log = Logger.getLogger(GuiceComponentProviderFactory.class);
	private final Map<Scope, ComponentScope> scopeMap = createScopeMap();
	private final Injector injector;

	/**
	 * Creates a new GuiceComponentProviderFactory.
	 *
	 * @param config the resource configuration
	 * @param injector the Guice injector
	 */
	public GuiceComponentProviderFactory(ResourceConfig config, Injector injector)
	{
		this.injector = injector;
		register(config, injector);
	}

	/**
	 * Registers any Guice-bound providers or root resources.
	 *
	 * @param config the resource config
	 * @param injector the Guice injector
	 */
	private void register(ResourceConfig config, Injector injector)
	{
		for (Key<?> key: injector.getBindings().keySet())
		{
			Type abstractType = key.getTypeLiteral().getType();
			Class<?> type;
			if (abstractType instanceof ParameterizedType)
			{
				ParameterizedType parameterized = (ParameterizedType) abstractType;
				type = (Class<?>) parameterized.getRawType();
			}
			else
			{
				// do nothing
				return;
			}
			if (ResourceConfig.isProviderClass(type))
			{
				if (log.isInfoEnabled())
					log.info("Registering " + type.getName() + " as a provider class");
				config.getClasses().add(type);
			}
			else if (ResourceConfig.isRootResourceClass(type))
			{
				if (log.isInfoEnabled())
					log.info("Registering " + type.getName() + " as a root resource class");
				config.getClasses().add(type);
			}
		}
	}

	public IoCComponentProvider getComponentProvider(Class c)
	{
		return getComponentProvider(null, c);
	}

	public IoCComponentProvider getComponentProvider(ComponentContext cc, Class clazz)
	{
		if (log.isTraceEnabled())
			log.trace("getComponentProvider(" + clazz.getName() + ")");
		@SuppressWarnings("unchecked")
		Class<Object> c = (Class<Object>) clazz;
		try
		{
			if (injector.getBinding(Key.get(c)) != null && !injector.getBindings().containsKey(Key.get(c)))
			{
				// There are no explicit bindings so we're not sure about the scope
				if (log.isInfoEnabled())
					log.info("Binding " + clazz.getName() + " to GuiceInstantiatedComponentProvider");
				return new GuiceInstantiatedComponentProvider(injector, clazz);
			}
		}
		catch (RuntimeException e)
		{
			// The class cannot be injected. For example, the constructor might be missing a @Inject annotation.
			if (log.isInfoEnabled())
				log.info("Cannot bind " + clazz.getName(), e);
			for (Constructor<?> constructor: c.getConstructors())
			{
				if (constructor.getAnnotation(Inject.class) != null)
				{
					// Guice should have picked this up. We fail-fast to prevent Jersey from trying to handle
					// injection.
					throw e;
				}
			}
			return null;
		}
		/*
		final Scope[] scope = new Scope[1];
		injector.getBinding(c).acceptScopingVisitor(new BindingScopingVisitor<Void>()
		{
			@Override
			public Void visitEagerSingleton()
			{
				scope[0] = Scopes.SINGLETON;
				return null;
			}

			@Override
			public Void visitScope(Scope theScope)
			{
				scope[0] = theScope;
				return null;
			}

			@Override
			public Void visitScopeAnnotation(Class scopeAnnotation)
			{
				// This method is not invoked for Injector bindings
				throw new UnsupportedOperationException();
			}

			@Override
			public Void visitNoScoping()
			{
				scope[0] = Scopes.NO_SCOPE;
				return null;
			}
		});
		assert (scope[0] != null);

		if (log.isInfoEnabled())
		{
			log.info("Binding " + clazz.getName() + " to GuiceManagedComponentProvider(" +
				getComponentScope(scope[0]) + ")");
		}
		*/
		return new GuiceManagedComponentProvider(injector, getComponentScope(Scopes.SINGLETON), clazz);
	}

	/**
	 * Converts a Guice scope to Jersey scope.
	 *
	 * @param scope the guice scope
	 * @return the Jersey scope
	 */
	private ComponentScope getComponentScope(Scope scope)
	{
		ComponentScope cs = scopeMap.get(scope);
		return (cs != null) ? cs : ComponentScope.Undefined;
	}

	/**
	 * Maps a Guice scope to a Jersey scope.
	 *
	 * @return the map
	 */
	private static Map<Scope, ComponentScope> createScopeMap()
	{
		Map<Scope, ComponentScope> result = new HashMap<Scope, ComponentScope>();
		result.put(Scopes.SINGLETON, ComponentScope.Singleton);
		//result.put(Scopes.NO_SCOPE, ComponentScope.PerRequest);
		return result;
	}

	/**
	 * Guice injects instances while Jersey manages their scope.
	 *
	 * @author Gili Tzabari
	 */
	private static class GuiceInstantiatedComponentProvider
		implements IoCInstantiatedComponentProvider
	{
		private final Injector injector;
		private final Class<?> clazz;

		/**
		 * Creates a new GuiceManagedComponentProvider.
		 *
		 * @param injector the injector
		 * @param clazz the class
		 */
		public GuiceInstantiatedComponentProvider(Injector injector, Class<?> clazz)
		{
			this.injector = injector;
			this.clazz = clazz;
		}

		@Override
		public Object getInjectableInstance(Object o)
		{
			return o;
		}

		public Class<?> getInjectableClass(Class<?> c)
		{
			return c.getSuperclass();
		}

		@Override
		public Object getInstance()
		{
			return injector.getInstance(clazz);
		}
	}

	/**
	 * Guice injects instances and manages their scope.
	 *
	 * @author Gili Tzabari
	 */
	private static class GuiceManagedComponentProvider extends GuiceInstantiatedComponentProvider
		implements IoCManagedComponentProvider
	{
		private final ComponentScope scope;

		/**
		 * Creates a new GuiceManagedComponentProvider.
		 *
		 * @param injector the injector
		 * @param scope the Jersey scope
		 * @param clazz the class
		 */
		public GuiceManagedComponentProvider(Injector injector, ComponentScope scope, Class<?> clazz)
		{
			super(injector, clazz);
			this.scope = scope;
		}

		public ComponentScope getScope()
		{
			return scope;
		}
	}
}
